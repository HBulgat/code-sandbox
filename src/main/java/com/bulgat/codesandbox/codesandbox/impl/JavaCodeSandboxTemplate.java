package com.bulgat.codesandbox.codesandbox.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.bulgat.codesandbox.codesandbox.CodeSandbox;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.CompileMessage;
import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;
import com.bulgat.codesandbox.model.ExecuteMessage;
import com.bulgat.codesandbox.model.enums.CompileCodeStatusEnum;
import com.bulgat.codesandbox.model.enums.ExecuteCodeStatusEnum;
import com.bulgat.codesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
/***
 * 模板方法
 */
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {
    public static final String JAVA_LANGUAGE="java";
    private static final String GLOBAL_CODE_DIR_NAME="tempCode";
    private static final String GLOBAL_JAVA_CLASS_NAME="Main.java";
    /**
     * 1,保存code到.java文件
     * @param code
     * @return
     */
    protected File savaCodeToFile(String code){
        //获取项目根目录
        String userDir=System.getProperty("user.dir");
        String globalCodePathName=userDir+ File.separator+GLOBAL_CODE_DIR_NAME;
        if (!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }

        //用户代码隔离存放
        String userCodeParentPath=globalCodePathName+File.separator+ UUID.randomUUID();
        String userCodePath= userCodeParentPath+File.separator+GLOBAL_JAVA_CLASS_NAME;
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2,编译
     * @param userCodeFile
     * @return
     */
    protected CompileMessage compileFile(File userCodeFile){
        CompileMessage compileMessage;
        String compileCmd=String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess=Runtime.getRuntime().exec(compileCmd);
            compileMessage= ProcessUtils.getCompileMessage(compileProcess);
            return compileMessage;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"编译失败");
        }
    }

    /**
     * 执行编译后的代码
     * @param userCodeFile
     * @param inputList
     * @return
     */
    protected abstract List<ExecuteMessage> runFile(File userCodeFile, List<String> inputList);

    /**
     * 删除用户代码所在的文件夹
     * @param userCodeParentPath
     * @return
     */
    protected  boolean deleteFiles(File userCodeParentPath){
        if (FileUtil.exist(userCodeParentPath)){
            boolean del = FileUtil.del(userCodeParentPath);
            return del;
        }
        return true;
    }

    /**
     * 执行请求,如果用户给的测试输入是空，那么也需要运行代码给出输出信息
     * @param executeCodeRequest
     * @return
     */
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        ExecuteCodeResponse executeCodeResponse=new ExecuteCodeResponse();
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        if (!language.equals(JAVA_LANGUAGE)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"代码语言选择错误");
        }

        File userCodeFile = savaCodeToFile(code);

        CompileMessage compileMessage = compileFile(userCodeFile);
        executeCodeResponse.setCompileMessage(compileMessage);
        if (compileMessage.getCompileCodeStatus().equals(CompileCodeStatusEnum.COMPILE_ERROR)){
            return executeCodeResponse;
        }

        List<ExecuteMessage> executeMessageList = runFile(userCodeFile, inputList);
        executeCodeResponse.setExecuteMessageList(executeMessageList);
        boolean b = deleteFile(userCodeFile);
        if (!b){
            log.error("deleteFile error, userCodeFilePath = {}",userCodeFile.getAbsoluteFile());
        }
        return executeCodeResponse;
    }

    /**
     * 删除文件
     * @param userCodeFile
     * @return
     */
    public boolean deleteFile(File userCodeFile){
        if (userCodeFile.getParentFile()!=null){
            String userCodeParentPath= userCodeFile.getParentFile().getAbsolutePath();
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("文件删除"+(del?"成功":"失败"));
            return del;
        }
        return true;
    }
}
