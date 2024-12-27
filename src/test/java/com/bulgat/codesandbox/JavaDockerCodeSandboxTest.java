//package com.bulgat.codesandbox;
//
//import cn.hutool.core.collection.ListUtil;
//import cn.hutool.core.io.resource.ResourceUtil;
//import com.bulgat.codesandbox.codesandbox.CodeSandbox;
//import com.bulgat.codesandbox.codesandbox.impl.JavaDockerSandbox;
//import com.bulgat.codesandbox.model.ExecuteCodeRequest;
//import com.bulgat.codesandbox.model.ExecuteCodeResponse;
//import org.junit.jupiter.api.Test;
//
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//
//public class JavaDockerCodeSandboxTest {
//    @Test
//    public void test(){
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setInputList(ListUtil.toList("1 2"));
//
//        String code= ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage("java");
//
//        CodeSandbox codeSandbox=new JavaDockerSandbox();
//        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//}
