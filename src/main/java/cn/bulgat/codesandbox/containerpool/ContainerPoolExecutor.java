package cn.bulgat.codesandbox.containerpool;

import cn.bulgat.codesandbox.constant.CmdConstant;
import cn.hutool.core.io.FileUtil;
import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.model.vo.codesandbox.CompileMessage;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import cn.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
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
@Configuration
@ConfigurationProperties(prefix = "codesandbox.container-pool-executor")
public class ContainerPoolExecutor {
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
                log.info("Create code dir failed.");
            }
        }
        ContainerInfo containerInfo=dockerDao.startContainer(userCodePathName);
        boolean result = containerPool.offer(containerInfo);
        if (!result){
            log.error("Current capacity: {}, the capacity limit is exceeded..",containerPool.size());
        }
    }
    private ContainerInfo getContainer() throws InterruptedException {
        if (containerPool.isEmpty()){
            try {
                if (blockingThreadCount.incrementAndGet()>=waitQueueSize&&!expandPool()){
                    log.error("Expand container pool failed");
                    return null;
                }
                log.info("No date，waiting ,current waiting length：{}",blockingThreadCount.get());
                return containerPool.take();
            } finally {
                log.info("Decrease blocking thread count");
                blockingThreadCount.decrementAndGet();
            }
        }
        return containerPool.take();
    }

    private boolean expandPool(){
        log.info("Need expand");
        if (expandCount.decrementAndGet()<0){
            log.error("Can not expand");
            return false;
        }
        log.info("Expanding");
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
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.SYSTEM_ERROR);
                executeCodeResponse.setCompileMessage(compileMessage);
                return executeCodeResponse;
            }
            ExecuteCodeResponse executeCodeResponse=function.apply(containerInfo);
            return executeCodeResponse;
        } catch (InterruptedException e){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR);
        } finally {
            if (containerInfo!=null){
                ContainerInfo finalContainerInfo=containerInfo;
                //清理容器内的文件
                dockerDao.execCmd(containerInfo.getContainerId(), CmdConstant.REMOVE_BOX_FILE_CMD);
                CompletableFuture.runAsync(()->{
                    try{
                        finalContainerInfo.setLastActivityTime(System.currentTimeMillis());
                        containerPool.put(finalContainerInfo);
                    }catch (InterruptedException e){
                        log.error("Can not take in, message is {}",e.getMessage());
                    }
                });
            }
        }
    }
}

