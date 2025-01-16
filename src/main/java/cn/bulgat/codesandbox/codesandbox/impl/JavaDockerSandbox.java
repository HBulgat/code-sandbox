package cn.bulgat.codesandbox.codesandbox.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.model.ExecuteMessage;
import cn.bulgat.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.*;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StopWatch;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

//@Component
@Slf4j
public class JavaDockerSandbox extends JavaCodeSandboxTemplate{
//    private static final String JAVA_8_IMAGE_NAME="openjdk:8-alpine";
    private static final String CODE_SANDBOX_IMAGE="codesandbox:latest";
    private static final String[] EXECUTE_JAVA_FILE_CMD_ARRAY_WITH_NO_INPUT=new String[]{"/bin/sh", "-c","java","-cp","/app","Main"};
    private static final String INPUT_FILE_NAME="input.txt";
    private static final long TIME_OUT=3*1000L;
    private static final String[] EXECUTE_JAVA_FILE_CMD_ARRAY_WITH_INPUT=new String[]{"/bin/sh", "-c","java","-cp","/app","Main","<","/app/"+INPUT_FILE_NAME};
    @Override
    protected List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList) {
        String userCodeParentPath= userCodeFile.getParentFile().getAbsolutePath();
        DockerClient dockerClient= DockerClientBuilder.getInstance().build();
        //创建容器
//        CreateContainerCmd containerCmd=dockerClient.createContainerCmd(JAVA_8_IMAGE_NAME);
        CreateContainerCmd containerCmd=dockerClient.createContainerCmd(CODE_SANDBOX_IMAGE);
        HostConfig hostConfig=new HostConfig();
        hostConfig.withMemory(100*1000*1000L);
        hostConfig.withMemorySwap(0L);
        hostConfig.withCpuCount(1L);
        hostConfig.setBinds(new Bind(userCodeParentPath,new Volume("/app")));
        CreateContainerResponse createContainerResponse=containerCmd
                .withHostConfig(hostConfig)
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        String containerId=createContainerResponse.getId();
        //启动容器
        dockerClient.startContainerCmd(containerId).exec();
        List<ExecuteMessage> executeMessageList=new ArrayList<>();
        String[] cmdArray=null;
        //没有输入
        if (CollectionUtil.isEmpty(inputList)){
            cmdArray=EXECUTE_JAVA_FILE_CMD_ARRAY_WITH_NO_INPUT;
            inputList=new ArrayList<>();//add一个null
            inputList.add(null);
        }else{//有输入
            cmdArray=EXECUTE_JAVA_FILE_CMD_ARRAY_WITH_INPUT;
        }
        ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                .withCmd(cmdArray)
                .withAttachStderr(true)
                .withAttachStdin(true)
                .withAttachStdout(true)
                .exec();
        String execId = execCreateCmdResponse.getId();
        for (String input : inputList) {
            ExecuteMessage executeMessage = new ExecuteMessage();
//            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
//                    .withCmd(cmdArray)
//                    .withAttachStderr(true)
//                    .withAttachStdin(true)
//                    .withAttachStdout(true)
//                    .exec();
//            String execId = execCreateCmdResponse.getId();
            if (input!=null&&!input.isEmpty()){
                //保存输入到文件
                saveInputToFile(input,userCodeParentPath);
            }
            StringBuilder successMessageStringBuilder=new StringBuilder();
            StringBuilder errorMessageStringBuilder=new StringBuilder();
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback(){

                @Override
                public void onNext(Frame frame) {
                    System.out.println("===============================");
                    StreamType streamType = frame.getStreamType();
                    System.out.println(streamType);
                    System.out.println(new String(frame.getPayload()));
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

            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            final long[] memory = {0L};
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存占用：" + statistics.getMemoryStats().getUsage()+"bytes");
                    memory[0] =Math.max(statistics.getMemoryStats().getUsage(), memory[0]);
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {
                }

                @Override
                public void onComplete() {
                }

                @Override
                public void close() throws IOException {

                }
            });
            statsCmd.exec(statisticsResultCallback);
            try {
                StopWatch stopWatch=new StopWatch();
                stopWatch.start();
                ExecStartCmd execStartCmd = dockerClient.execStartCmd(execId);
                ExecStartResultCallback exec = execStartCmd.exec(execStartResultCallback);
                // todo 超时控制
                exec.awaitCompletion();
                stopWatch.stop();
                long time=stopWatch.getLastTaskTimeMillis();
                executeMessage.setTime(time);
                executeMessage.setMemory(memory[0]);
                executeMessage.setMessage(errorMessageStringBuilder.toString());
                executeMessage.setOutput(successMessageStringBuilder.toString());
                executeMessageList.add(executeMessage);
            } catch (InterruptedException e) {
                log.error("运行出错，"+e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
            //删除输入文件
            boolean del = FileUtil.del(userCodeParentPath + File.separator + INPUT_FILE_NAME);
            if (!del){
                log.error("输入文件删除失败");
                throw new BusinessException(ErrorCode.SYSTEM_ERROR);
            }
        }
        //关闭并删除容器
        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        return executeMessageList;
    }

    /***
     * 保存输入到文件内
     * @param input
     * @param userCodeParentPath
     */
    public void saveInputToFile(String input,String userCodeParentPath){
        String inputFilePath=userCodeParentPath+File.separator+INPUT_FILE_NAME;
        if (FileUtil.exist(inputFilePath)){
            //文件存在说明有问题
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"输入用例保存错误");
        }
        FileUtil.writeString(input, inputFilePath, StandardCharsets.UTF_8);
    }

}
