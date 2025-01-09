package com.bulgat.codesandbox.codesandbox.impl;

import cn.hutool.core.io.FileUtil;
import com.bulgat.codesandbox.codesandbox.CodeSandbox;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.containerpool.ContainerPoolExecutor;
import com.bulgat.codesandbox.containerpool.DockerDao;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.CompileMessage;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;
import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.model.ExecuteMessage;
import com.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import com.bulgat.codesandbox.model.enums.LanguageCmdEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import static com.bulgat.codesandbox.constant.CmdExecuteStatusConstant.*;
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "codesandbox.config")
@Component
public class DockerSandbox implements CodeSandbox {
    @Resource
    private DockerDao dockerDao;

    @Resource
    private ContainerPoolExecutor containerPoolExecutor;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        LanguageCmdEnum languageCmdEnum=LanguageCmdEnum.getEnumByLanguage(language);
        log.info("current language= {}",language);
        if (languageCmdEnum==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"系统不支持该语言");
        }
        return containerPoolExecutor.run(containerInfo -> {
            try{
                String containerId = containerInfo.getContainerId();
                String userCodePathName = containerInfo.getUserCodePathName();
                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
                FileUtil.writeString(code,userCodePathName+File.separator+languageCmdEnum.getSaveFileName(),StandardCharsets.UTF_8);

                String[] compileCmd= languageCmdEnum.getCompileCmd();
                if (compileCmd!=null){
                    CompileMessage compileMessage=dockerDao.compileFile(compileCmd,containerId);
                    executeCodeResponse.setCompileMessage(compileMessage);
                    if (compileMessage.getCompileCodeStatus().getSuccess()==FAILED){
                        return executeCodeResponse;
                    }
                }else{
                    CompileMessage compileMessage=new CompileMessage();
                    compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_NO_NEEDED);
                    executeCodeResponse.setCompileMessage(compileMessage);
                }
                List<ExecuteMessage> executeMessageList = dockerDao.executeFile(new File(userCodePathName), languageCmdEnum, inputList, containerId);
                executeCodeResponse.setExecuteMessageList(executeMessageList);
                return executeCodeResponse;
            } catch (Exception e){
                log.error("编译失败，"+e.getMessage());
                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
                CompileMessage compileMessage=new CompileMessage();
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_ERROR);
                compileMessage.setMessage("编译失败");
                executeCodeResponse.setCompileMessage(compileMessage);
                return executeCodeResponse;
            }
        });
    }
}
