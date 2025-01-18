//package cn.bulgat.codesandbox;
//
//import cn.hutool.core.collection.ListUtil;
//import cn.hutool.core.io.resource.ResourceUtil;
//import cn.bulgat.codesandbox.codesandbox.impl.DockerSandbox;
//import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequest;
//import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
//import cn.bulgat.codesandbox.model.enums.LanguageCmdEnum;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//
//import javax.annotation.Resource;
//import java.nio.charset.StandardCharsets;
//
//
//@SpringBootTest
//class DockerSandboxTest {
//
//    @Resource
//    private DockerSandbox dockerSandbox;
//    @Test
//    void testJava() throws InterruptedException {
//        String code = ResourceUtil.readStr("testCode/Main.java", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.JAVA.getLanguage());
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testCpp() {
//        String code = ResourceUtil.readStr("testCode/main.cpp", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//        executeCodeRequest.setInputList(ListUtil.toList("1 2","3 4"));
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.CPP.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testCppWithInput(){
//        String code = ResourceUtil.readStr("testCode/main_with_input.cpp", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//        executeCodeRequest.setInputList(ListUtil.toList("1 2","3 4"));
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.CPP.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//    @Test
//    void testC(){
//        String code = ResourceUtil.readStr("testCode/main.c", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.C.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testCWithInput() {
//        String code = ResourceUtil.readStr("testCode/main_with_input.c", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setInputList(ListUtil.toList("1 2","3 4"));
//        executeCodeRequest.setLanguage(LanguageCmdEnum.C.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testPython() {
//        String code = ResourceUtil.readStr("testCode/main.py", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.PYTHON3.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testPythonWithInput() {
//        String code = ResourceUtil.readStr("testCode/main_with_input.py", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setInputList(ListUtil.toList("1\n2","3\n4"));
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.PYTHON3.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testJs() {
//        String code = ResourceUtil.readStr("testCode/main.js", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.JAVASCRIPT.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
//    @Test
//    void testTs() {
//        String code = ResourceUtil.readStr("testCode/main.ts", StandardCharsets.UTF_8);
//        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//
//        executeCodeRequest.setCode(code);
//        executeCodeRequest.setLanguage(LanguageCmdEnum.TYPESCRIPT.getLanguage());
//
//        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
//        System.out.println(executeCodeResponse);
//    }
//
////    @Test
////    void testGo() {
////        for (int i = 0; i < 5; i++) {
////            String code = ResourceUtil.readStr("testCode/main.go", StandardCharsets.UTF_8);
////            ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
////
////            executeCodeRequest.setCode(code);
////            executeCodeRequest.setLanguage(LanguageCmdEnum.GO.getLanguage());
////
////            ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
////            System.out.println(executeCodeResponse);
////        }
////    }
////
////    @Test
////    void testGoWithInput() {
////        String code = ResourceUtil.readStr("testCode/main_with_input.go", StandardCharsets.UTF_8);
////        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
////
////        executeCodeRequest.setCode(code);
////        executeCodeRequest.setLanguage(LanguageCmdEnum.GO.getLanguage());
////
////        executeCodeRequest.setInputList(ListUtil.toList("1 2"));
////        ExecuteCodeResponse executeCodeResponse = dockerSandbox.executeCode(executeCodeRequest);
////        System.out.println(executeCodeResponse);
////    }
//
//}
