package com.bulgat.codesandbox.containerpool;

import cn.hutool.core.io.FileUtil;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.CompileMessage;
import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@Slf4j
@Data
@Component
public class ContainerPoolExecutor {
//    private Integer corePoolSize=2;
//    private Integer maximumPoolSize=3;
    private Integer corePoolSize=Runtime.getRuntime().availableProcessors()*10;
    private Integer maximumPoolSize=Runtime.getRuntime().availableProcessors()*20;
    private Integer waitQueueSize=2000;
    private Integer keepAliveTime=5;
    private TimeUnit timeUnit=TimeUnit.SECONDS;

    private BlockingDeque<ContainerInfo> containerPool;
    private AtomicInteger blockingThreadCount;
    private AtomicInteger expandCount;

    @Resource
    private DockerDao dockerDao;

    @PostConstruct
    public void initPool(){
        this.containerPool=new LinkedBlockingDeque<>(maximumPoolSize);
        this.blockingThreadCount=new AtomicInteger(0);
        this.expandCount=new AtomicInteger(maximumPoolSize-corePoolSize);
        for (int i = 0; i < corePoolSize; i++) {
            createNewPool();
        }
        ScheduledExecutorService scheduledExecutorService= Executors.newScheduledThreadPool(1);
        scheduledExpirationCleanUp(scheduledExecutorService);
    }
    private void cleanExpiredContainers(){
        long currentTime=System.currentTimeMillis();
        int needCleanCount=containerPool.size()-corePoolSize;
        if (needCleanCount<=0){
            return;
        }
        containerPool.stream().filter(containerInfo -> {
            long lastActivityTime = containerInfo.getLastActivityTime();
            lastActivityTime+=timeUnit.toMillis(keepAliveTime);
            return lastActivityTime<currentTime;
        }).forEach(containerInfo -> {
            boolean remove = containerPool.remove(containerInfo);
            if (remove){
                String containerId = containerInfo.getContainerId();
                expandCount.incrementAndGet();
                if (StringUtils.isNotBlank(containerId)){
                    dockerDao.cleanContainer(containerId);
                }
            }
        });
    }
    private void createNewPool(){
        String userDir=System.getProperty("user.dir");
        String userCodePathName=userDir+ File.separator+"tempCode";
        userCodePathName+=File.separator+UUID.randomUUID();
        if (!FileUtil.exist(userCodePathName)){
            File userCodePath = FileUtil.mkdir(userCodePathName);
            if (userCodePath==null||!userCodePath.exists()){
                log.info("创建代码目录失败");
            }
        }
        ContainerInfo containerInfo=dockerDao.startContainer(userCodePathName);
        boolean result = containerPool.offer(containerInfo);
        if (!result){
            log.error("current capacity: {}, the capacity limit is exceeded..",containerPool.size());
        }
    }
    private ContainerInfo getContainer() throws InterruptedException {
        if (containerPool.isEmpty()){
            try {
                if (blockingThreadCount.incrementAndGet()>=waitQueueSize&&!expandPool()){
                    log.error("扩容失败");
                    return null;
                }
                log.info("没有数据，等待数据，当前等待长度：{}",blockingThreadCount.get());
                return containerPool.take();
            } finally {
                log.info("减少阻塞线程计数");
                blockingThreadCount.decrementAndGet();
            }
        }
        return containerPool.take();
    }

    private boolean expandPool(){
        log.info("超过指定数量，扩容");
        if (expandCount.decrementAndGet()<0){
            log.error("不能再扩容了");
            return false;
        }
        log.info("扩容了");
        createNewPool();
        return true;
    }

    private void scheduledExpirationCleanUp(ScheduledExecutorService scheduledExecutorService){
        scheduledExecutorService.scheduleAtFixedRate(()->{
            cleanExpiredContainers();
        },0,20,TimeUnit.SECONDS);
    }

    public ExecuteCodeResponse run(Function<ContainerInfo,ExecuteCodeResponse> function){
        ContainerInfo containerInfo=null;
        try{
            containerInfo=getContainer();
            if (containerInfo==null){
                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
                CompileMessage compileMessage=new CompileMessage();
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_ERROR);
                compileMessage.setMessage("System Error");
                executeCodeResponse.setCompileMessage(compileMessage);
                return executeCodeResponse;
            }
            ExecuteCodeResponse executeCodeResponse=function.apply(containerInfo);
            log.info("executeCodeResponse= "+executeCodeResponse);
            return executeCodeResponse;
        } catch (InterruptedException e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (containerInfo!=null){
                ContainerInfo finalContainerInfo=containerInfo;
                dockerDao.execCmd(containerInfo.getContainerId(),new String[]{"rm","-rf","/box"});
                CompletableFuture.runAsync(()->{
                    try{
                        finalContainerInfo.setLastActivityTime(System.currentTimeMillis());
                        containerPool.put(finalContainerInfo);
                    }catch (InterruptedException e){
                        log.error("无法放入"+e.getMessage());
                    }
                });
            }
        }
    }
}

