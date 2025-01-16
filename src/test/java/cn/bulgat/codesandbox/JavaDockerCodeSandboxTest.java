//package cn.bulgat.codesandbox;
//
//import cn.hutool.core.collection.ListUtil;
//import cn.hutool.core.io.resource.ResourceUtil;
//import cn.bulgat.codesandbox.codesandbox.CodeSandbox;
//import cn.bulgat.codesandbox.codesandbox.impl.JavaDockerSandbox;
//import cn.bulgat.codesandbox.model.ExecuteCodeRequest;
//import cn.bulgat.codesandbox.model.ExecuteCodeResponse;
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
