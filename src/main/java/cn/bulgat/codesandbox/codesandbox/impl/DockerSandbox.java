package cn.bulgat.codesandbox.codesandbox.impl;

import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequestByFileOrText;
import cn.bulgat.codesandbox.model.dto.codesandbox.Input;
import cn.hutool.core.io.FileUtil;
import cn.bulgat.codesandbox.codesandbox.CodeSandbox;
import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.containerpool.ContainerPoolExecutor;
import cn.bulgat.codesandbox.containerpool.DockerDao;
import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.model.vo.codesandbox.CompileMessage;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequest;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteMessage;
import cn.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import cn.bulgat.codesandbox.model.enums.LanguageCmdEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static cn.bulgat.codesandbox.constant.CmdExecuteStatusConstant.*;
@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "codesandbox")
@Component
public class DockerSandbox implements CodeSandbox {
    @Resource
    private DockerDao dockerDao;

    @Resource
    private ContainerPoolExecutor containerPoolExecutor;

    /**
     * 输入都是字符串
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest, Map<String, MultipartFile> fileMap) {
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        List<Input> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        LanguageCmdEnum languageCmdEnum=LanguageCmdEnum.getEnumByLanguage(language);
        log.info("Current language= {}",language);
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
                        log.info("Compile code failed, compile status is {}",compileMessage.getCompileCodeStatus().getCode());
                        return executeCodeResponse;
                    }
                }else{
                    CompileMessage compileMessage=new CompileMessage();
                    log.info("The language is not need to compile.");
                    compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_NO_NEEDED);
                    executeCodeResponse.setCompileMessage(compileMessage);
                }
                List<ExecuteMessage> executeMessageList = dockerDao.executeFile(new File(userCodePathName), languageCmdEnum, inputList, containerId,fileMap);
                log.info("Execute message list size is {}",executeMessageList.size());
                executeCodeResponse.setExecuteMessageList(executeMessageList);
                return executeCodeResponse;
            } catch (Exception e){
                log.error("System error, "+e.getMessage());
                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
                CompileMessage compileMessage=new CompileMessage();
                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.SYSTEM_ERROR);
                executeCodeResponse.setCompileMessage(compileMessage);
                return executeCodeResponse;
            }
        });
    }

////    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequestByFileOrText executeCodeRequest){
//        if (executeCodeRequest==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        List<Input> inputList=executeCodeRequest.getInputList();
//        String code = executeCodeRequest.getCode();
//        String language = executeCodeRequest.getLanguage();
//        LanguageCmdEnum languageCmdEnum=LanguageCmdEnum.getEnumByLanguage(language);
//        log.info("Current language= {}",language);
//        if (languageCmdEnum==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"系统不支持该语言");
//        }
//        return containerPoolExecutor.run(containerInfo -> {
//            try{
//                String containerId = containerInfo.getContainerId();
//                String userCodePathName = containerInfo.getUserCodePathName();
//                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
//                FileUtil.writeString(code,userCodePathName+File.separator+languageCmdEnum.getSaveFileName(),StandardCharsets.UTF_8);
//
//                String[] compileCmd= languageCmdEnum.getCompileCmd();
//                if (compileCmd!=null){
//                    CompileMessage compileMessage=dockerDao.compileFile(compileCmd,containerId);
//                    executeCodeResponse.setCompileMessage(compileMessage);
//                    if (compileMessage.getCompileCodeStatus().getSuccess()==FAILED){
//                        log.info("Compile code failed, compile status is {}",compileMessage.getCompileCodeStatus().getCode());
//                        return executeCodeResponse;
//                    }
//                }else{
//                    CompileMessage compileMessage=new CompileMessage();
//                    log.info("The language is not need to compile.");
//                    compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_NO_NEEDED);
//                    executeCodeResponse.setCompileMessage(compileMessage);
//                }
//                List<ExecuteMessage> executeMessageList = dockerDao.executeFile(new File(userCodePathName), languageCmdEnum, inputList, containerId);
//                log.info("Execute message list size is {}",executeMessageList.size());
//                executeCodeResponse.setExecuteMessageList(executeMessageList);
//                return executeCodeResponse;
//            } catch (Exception e){
//                log.error("System error, "+e.getMessage());
//                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
//                CompileMessage compileMessage=new CompileMessage();
//                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.SYSTEM_ERROR);
//                executeCodeResponse.setCompileMessage(compileMessage);
//                return executeCodeResponse;
//            }
//        });
//    }

//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequestByFileOrText executeCodeRequest, Map<String , MultipartFile> fileMap) {
//        if (executeCodeRequest==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
//        }
//        List<Input> inputList = executeCodeRequest.getInputList();
//        String code = executeCodeRequest.getCode();
//        String language = executeCodeRequest.getLanguage();
//        LanguageCmdEnum languageCmdEnum=LanguageCmdEnum.getEnumByLanguage(language);
//        log.info("Current language= {}",language);
//        if (languageCmdEnum==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR,"系统不支持该语言");
//        }
//        return containerPoolExecutor.run(containerInfo -> {
//            try{
//                String containerId = containerInfo.getContainerId();
//                String userCodePathName = containerInfo.getUserCodePathName();
//                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
//                FileUtil.writeString(code,userCodePathName+File.separator+languageCmdEnum.getSaveFileName(),StandardCharsets.UTF_8);
//
//                String[] compileCmd= languageCmdEnum.getCompileCmd();
//                if (compileCmd!=null){
//                    CompileMessage compileMessage=dockerDao.compileFile(compileCmd,containerId);
//                    executeCodeResponse.setCompileMessage(compileMessage);
//                    if (compileMessage.getCompileCodeStatus().getSuccess()==FAILED){
//                        log.info("Compile code failed, compile status is {}",compileMessage.getCompileCodeStatus().getCode());
//                        return executeCodeResponse;
//                    }
//                }else{
//                    CompileMessage compileMessage=new CompileMessage();
//                    log.info("The language is not need to compile.");
//                    compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.COMPILE_NO_NEEDED);
//                    executeCodeResponse.setCompileMessage(compileMessage);
//                }
//                List<ExecuteMessage> executeMessageList = dockerDao.executeFile(new File(userCodePathName), languageCmdEnum, inputList, containerId,fileMap);
//                log.info("Execute message list size is {}",executeMessageList.size());
//                executeCodeResponse.setExecuteMessageList(executeMessageList);
//                return executeCodeResponse;
//            } catch (Exception e){
//                log.error("System error, "+e.getMessage());
//                ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
//                CompileMessage compileMessage=new CompileMessage();
//                compileMessage.setCompileCodeStatus(CompileCodeStatusEnum.SYSTEM_ERROR);
//                executeCodeResponse.setCompileMessage(compileMessage);
//                return executeCodeResponse;
//            }
//        });
//    }

}
