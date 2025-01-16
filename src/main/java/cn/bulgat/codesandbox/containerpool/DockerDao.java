package cn.bulgat.codesandbox.containerpool;

import cn.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import cn.bulgat.codesandbox.model.enums.LanguageCmdEnum;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.bulgat.codesandbox.constant.ApiAuthConstant;
import cn.bulgat.codesandbox.model.vo.codesandbox.CompileMessage;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteMessage;
import cn.bulgat.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "codesandbox.docker-dao")
public class DockerDao {
    private static String CODE_SANDBOX_IMAGE ="codesandbox:latest";
    private static final DockerClient DOCKER_CLIENT = DockerClientBuilder.getInstance().build();
    private long memoryLimit= 60*1024*1024L;
    private long memorySwap=0;
    private long cpuCount=1;
    private long executeTimeoutLimit=10;
    private TimeUnit executeTimeUnit=TimeUnit.SECONDS;
    private long compileTimeoutLimit=3;
    private TimeUnit compileTimeUnit=TimeUnit.SECONDS;
    private long outputLengthLimit=128*1024L;
    private boolean isDebug=false;

    public CompileMessage compileFile(String[] compileCmd, String containerId) throws InterruptedException {
        CompileMessage compileMessage=new CompileMessage();
        ExecCreateCmdResponse execCreateCmdResponse = DOCKER_CLIENT.execCreateCmd(containerId)
                .withCmd(compileCmd)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
        StringBuffer resultStringBuffer=new StringBuffer();
        ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {
            @Override
            public void onNext(Frame frame) {
                StreamType streamType = frame.getStreamType();
                if (StreamType.STDERR.equals(streamType)){
                    if(compileMessage.getCompileCodeStatus().equals(CompileCodeStatusEnum.COMPILE_SUCCESS)){
                        compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_ERROR);
                    }
                }
                resultStringBuffer.append(new String(frame.getPayload()));
                super.onNext(frame);
            }
        };
        StopWatch stopWatch=new StopWatch();
        stopWatch.start();
        if (isDebug){
            DOCKER_CLIENT.execStartCmd(execId).exec(frameAdapter).awaitCompletion();
        }else{
            DOCKER_CLIENT.execStartCmd(execId).exec(frameAdapter).awaitCompletion(
                    compileTimeoutLimit,compileTimeUnit
            );
        }
        stopWatch.stop();
        long compileTime=stopWatch.getLastTaskTimeMillis();
        if (!isDebug){
            if (compileTime>=compileTimeUnit.toMillis(compileTimeoutLimit)){
                log.error("Compile time out, compile time is {} ms",compileTime);
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_TIME_OUT);
            }
        }
        compileMessage.setMessage(resultStringBuffer.toString());
        return compileMessage;
    }



    /***
     * 保存输入到文件内
     * @param input
     * @param userCodePath
     */
    private void saveInputToFile(String input,String userCodePath,String inputFileName){
        String inputFilePath=userCodePath+File.separator+ inputFileName;
        if (FileUtil.exist(inputFilePath)){
            log.info("The input case file is existed.");
            FileUtil.del(inputFilePath);
        }
        FileUtil.writeString(input, inputFilePath, StandardCharsets.UTF_8);
    }

    /**
     * 执行代码
     * @param userCodeFile
     * @param languageCmdEnum
     * @param inputList
     * @param containerId
     * @return
     */
    public List<ExecuteMessage> executeFile(File userCodeFile, LanguageCmdEnum languageCmdEnum, List<String> inputList, String containerId){
        //1. 获取运行命令
        String[] runCmdPart= languageCmdEnum.getRunCmdWithInput();
        if (CollectionUtil.isEmpty(inputList)){
            runCmdPart= languageCmdEnum.getRunCmdWithNoInput();
            inputList=new ArrayList<>();
            inputList.add(null);
        }

        List<ExecuteMessage> executeMessageList=new ArrayList<>();
        int inputCnt=0;
        //遍历输入列表
        for (String input : inputList) {
            inputCnt++;
            String[] runCmd=runCmdPart.clone();
            ExecuteMessage executeMessage=new ExecuteMessage();
            String inputFileName=null;
            if (StrUtil.isNotBlank(input)){
                inputFileName="input"+inputCnt+".txt";
                //输入写入文件
                saveInputToFile(input, userCodeFile.getAbsolutePath(), inputFileName);
                runCmd[runCmd.length-1]=runCmd[runCmd.length-1]+" "+ ApiAuthConstant.REMOTE_PARENT_PATH+File.separator+inputFileName;
            }
            ExecCreateCmdResponse execCreateCmdResponse = DOCKER_CLIENT.execCreateCmd(containerId)
                    .withCmd(runCmd)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();

            //存储输出
            //正常输出
            StringBuilder commonMessageStringBuilder=new StringBuilder();
            //异常输出
            StringBuilder errorMessageStringBuilder=new StringBuilder();
            //是否输出导致OOM
            final boolean[] isOOM = {false};
            ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                if (errorMessageStringBuilder.length()+commonMessageStringBuilder.length()>=outputLengthLimit){
                    if (!isOOM[0]){
                        isOOM[0] =true;
                        log.error("Out of memory!");
                        executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.EXECUTE_OUTPUT_EXCEEDED);
                    }
                }else{
                    StreamType streamType = frame.getStreamType();
                    if(StreamType.STDERR.equals(streamType)){
                        if(executeMessage.getExecuteCodeStatus()== ExecuteCodeStatusEnum.EXECUTE_SUCCESS){
                            executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.EXECUTE_ERROR);
                        }
                        log.info("Execute process has error happening");
                        errorMessageStringBuilder.append(new String(frame.getPayload()));
                    }else{
                        commonMessageStringBuilder.append(new String(frame.getPayload()));
                    }
                }
                super.onNext(frame);
                }
            };
            StatsCmd statsCmd = DOCKER_CLIENT.statsCmd(containerId);
            final long[] memory = {0L};
            ResultCallback<Statistics> statisticsResultCallback = new ResultCallback<Statistics>() {
                @Override
                public void close() throws IOException {
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onNext(Statistics statistics) {
                    memory[0] =Math.max(statistics.getMemoryStats().getUsage(), memory[0]);
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {
                }
            };

            //时间监控
            StopWatch stopWatch=new StopWatch();
            stopWatch.start();
            ExecStartCmd execStartCmd = DOCKER_CLIENT.execStartCmd(execId);
            statsCmd.exec(statisticsResultCallback);
            statsCmd.withContainerId(containerId);
            statsCmd.withNoStream(true);
            try {
                if (isDebug){
                    execStartCmd.exec(frameAdapter).awaitCompletion();
                }else{
                    execStartCmd.exec(frameAdapter).awaitCompletion(
                            executeTimeoutLimit,executeTimeUnit
                    );
                }
            } catch (InterruptedException e) {
                log.error("Execute code failed , message = {}",e.getMessage());
                //执行失败，说明应该是系统错误
                executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.SYSTEM_ERROR);
            }
            stopWatch.stop();
            //运行时间获取
            long executeTime=stopWatch.getLastTaskTimeMillis();
            if (!isDebug){
                if (executeTime>=executeTimeUnit.toMillis(executeTimeoutLimit)){
                    log.info("Execute time out, the time is {} ms",executeTime);
                    executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.EXECUTE_TIME_OUT);
                }
            }
            executeMessage.setTime(executeTime);
            executeMessage.setMemory(memory[0]);

            executeMessage.setMessage(errorMessageStringBuilder.toString());

            executeMessage.setOutput(commonMessageStringBuilder.toString());
            executeMessageList.add(executeMessage);
            //删除输入文件
            if (inputFileName!=null){
                boolean del = FileUtil.del(userCodeFile.getAbsolutePath() + File.separator + inputFileName);
                if (!del){
                    log.error("The input file delete failed");
                }
            }
        }
        return executeMessageList;
    }


    public ContainerInfo startContainer(String userCodePathName) {
        CreateContainerCmd containerCmd = DOCKER_CLIENT.createContainerCmd(CODE_SANDBOX_IMAGE);
        HostConfig hostConfig=new HostConfig();
        hostConfig.withMemory(memoryLimit)
                .withMemorySwap(memorySwap)
                .withCpuCount(cpuCount)
                .setBinds(new Bind(userCodePathName,new Volume(ApiAuthConstant.REMOTE_PARENT_PATH)));
        CreateContainerResponse createContainerResponse = containerCmd.withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withTty(true)
                .exec();
        String containerId = createContainerResponse.getId();
        log.info("Create container success , containerId = {}",containerId);
        //启动容器
        DOCKER_CLIENT.startContainerCmd(containerId).exec();
        ContainerInfo containerInfo=new ContainerInfo();
        containerInfo.setContainerId(containerId);
        containerInfo.setUserCodePathName(userCodePathName);
        containerInfo.setLastActivityTime(System.currentTimeMillis());
        return containerInfo;
    }

    public void cleanContainer(String containerId) {
        DOCKER_CLIENT.stopContainerCmd(containerId).exec();
        DOCKER_CLIENT.removeContainerCmd(containerId).exec();
    }

    public void execCmd(String containerId, String[] cmd) {
        ExecCreateCmdResponse execCreateCmdResponse = DOCKER_CLIENT.execCreateCmd(containerId)
                .withCmd(cmd)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
        try {
            DOCKER_CLIENT.execStartCmd(execId).exec(new ResultCallback.Adapter<>()).awaitCompletion();
        } catch (InterruptedException e) {
            log.error("The cmd execute failed, message is {}",e.getMessage());
        }
    }
}
