package com.bulgat.codesandbox.containerpool;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.bulgat.codesandbox.common.Constant;
import com.bulgat.codesandbox.model.CompileMessage;
import com.bulgat.codesandbox.model.ExecuteMessage;
import com.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import com.bulgat.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.bulgat.codesandbox.model.enums.LanguageCmdEnum;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
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
public class DockerDao {
    private String codeSandboxImage ="codesandbox:latest";
    private static final DockerClient DOCKER_CLIENT = DockerClientBuilder.getInstance().build();
    private long memoryLimit = 60*1024*1024L;
    private long memorySwap=0;
    private long cpuCount=1;
    private long executeTimeoutLimit=5;
    private TimeUnit executeTimeUnit=TimeUnit.SECONDS;
    private long compileTimeoutLimit=3;
    private TimeUnit compileTimeUnit=TimeUnit.SECONDS;


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
        DOCKER_CLIENT.execStartCmd(execId).exec(frameAdapter).awaitCompletion(
//                compileTimeoutLimit,compileTimeUnit
        );
        stopWatch.stop();
        long compileTime=stopWatch.getLastTaskTimeMillis();
//        if (compileTime>=compileTimeUnit.toMillis(compileTimeoutLimit)){
//            compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_ERROR);
//            resultStringBuffer.append("编译超时");
//        }
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
            log.info("输入用例文件已存在");
            FileUtil.del(inputFilePath);
        }
        FileUtil.writeString(input, inputFilePath, StandardCharsets.UTF_8);
    }

    public List<ExecuteMessage> executeFile(File userCodeFile, LanguageCmdEnum languageCmdEnum, List<String> inputList, String containerId){
        String[] runCmdPart= languageCmdEnum.getRunCmdWithInput();
        if (CollectionUtil.isEmpty(inputList)){
            runCmdPart= languageCmdEnum.getRunCmdWithNoInput();
            inputList=new ArrayList<>();
            inputList.add(null);
        }

        List<ExecuteMessage> executeMessageList=new ArrayList<>();
        int inputCnt=0;
        for (String input : inputList) {
            inputCnt++;
            String[] runCmd=runCmdPart.clone();
            ExecuteMessage executeMessage=new ExecuteMessage();
            String inputFileName=null;
            if (StrUtil.isNotBlank(input)){
                inputFileName="input"+inputCnt+".txt";
                saveInputToFile(input, userCodeFile.getAbsolutePath(), inputFileName);
                runCmd[runCmd.length-1]=runCmd[runCmd.length-1]+" "+ Constant.REMOTE_PARENT_PATH+File.separator+inputFileName;
            }
            ExecCreateCmdResponse execCreateCmdResponse = DOCKER_CLIENT.execCreateCmd(containerId)
                    .withCmd(runCmd)
                    .withAttachStderr(true)
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .exec();
            String execId = execCreateCmdResponse.getId();

            StringBuilder successMessageStringBuilder=new StringBuilder();
            StringBuilder errorMessageStringBuilder=new StringBuilder();
            ResultCallback.Adapter<Frame> frameAdapter = new ResultCallback.Adapter<Frame>() {
                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if(StreamType.STDERR.equals(streamType)){
                        if(executeMessage.getExecuteCodeStatus()== ExecuteCodeStatusEnum.EXECUTE_SUCCESS){
                            executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.EXECUTE_ERROR);
                        }
                        errorMessageStringBuilder.append(new String(frame.getPayload()));
                    }else{
                        successMessageStringBuilder.append(new String(frame.getPayload()));
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
//                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage()+"bytes");
                    memory[0] =Math.max(statistics.getMemoryStats().getUsage(), memory[0]);
                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {
                }
            };
            StopWatch stopWatch=new StopWatch();
            stopWatch.start();
            ExecStartCmd execStartCmd = DOCKER_CLIENT.execStartCmd(execId);
            statsCmd.exec(statisticsResultCallback);
            try {
                execStartCmd.exec(frameAdapter).awaitCompletion(
//                        executeTimeoutLimit,executeTimeUnit
                );
            } catch (InterruptedException e) {
                log.error("运行出错，"+e.getMessage());
                executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.EXECUTE_ERROR);
                errorMessageStringBuilder.append("运行出错");
            }
            stopWatch.stop();
            long time=stopWatch.getLastTaskTimeMillis();
//            if (time>=executeTimeUnit.toMillis(executeTimeoutLimit)){
//                errorMessageStringBuilder.append("运行超时");
//            }
            executeMessage.setTime(time);
            executeMessage.setMemory(memory[0]);

            executeMessage.setMessage(errorMessageStringBuilder.toString());
            if (!errorMessageStringBuilder.toString().isEmpty()){
                executeMessage.setExecuteCodeStatus(ExecuteCodeStatusEnum.EXECUTE_ERROR);
            }
            executeMessage.setOutput(successMessageStringBuilder.toString());
            executeMessageList.add(executeMessage);
            //删除输入文件
            if (inputFileName!=null){
                boolean del = FileUtil.del(userCodeFile.getAbsolutePath() + File.separator + inputFileName);
                if (!del){
                    log.error("输入文件删除失败");
                }
            }
        }
        return executeMessageList;
    }


    public ContainerInfo startContainer(String userCodePathName) {
        CreateContainerCmd containerCmd = DOCKER_CLIENT.createContainerCmd(codeSandboxImage);
        HostConfig hostConfig=new HostConfig();
        hostConfig.withMemory(memoryLimit)
                .withMemorySwap(memorySwap)
                .withCpuCount(cpuCount)
                .setBinds(new Bind(userCodePathName,new Volume(Constant.REMOTE_PARENT_PATH)));
        CreateContainerResponse createContainerResponse = containerCmd.withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withAttachStdout(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withTty(true)
                .exec();
        String containerId = createContainerResponse.getId();
        log.info("create container success , containerId = {}",containerId);
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
            log.error("命令执行失败"+e.getMessage());
        }
    }
}
