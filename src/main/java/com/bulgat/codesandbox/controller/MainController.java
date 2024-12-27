package com.bulgat.codesandbox.controller;

import com.bulgat.codesandbox.codesandbox.CodeSandbox;
import com.bulgat.codesandbox.common.BaseResponse;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.common.ResultUtils;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.ExecuteCodeResponse;
import com.bulgat.codesandbox.model.ExecuteCodeRequest;
import com.bulgat.codesandbox.model.enums.LanguageCmdEnum;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Random;

@RestController
@RequestMapping("/execute_code")
public class MainController {
    @Resource
    private CodeSandbox codeSandbox;

    @PostMapping("/execute_code")
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest){
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(codeSandbox.executeCode(executeCodeRequest));
    }

    private final LanguageCmdEnum[] languageCmdEnums=new LanguageCmdEnum[]{
            LanguageCmdEnum.PYTHON3,
            LanguageCmdEnum.C,
            LanguageCmdEnum.CPP,
            LanguageCmdEnum.JAVA,
            LanguageCmdEnum.JAVASCRIPT,
            LanguageCmdEnum.TYPESCRIPT
    };

    private final String[] codes = new String[]{
            "print(\"Hello, Python!\")",
            "// hello.c\n" +
                    "#include <stdio.h>\n" +
                    "\n" +
                    "int main() {\n" +
                    "    printf(\"Hello, C!\\n\");\n" +
                    "    return 0;\n" +
                    "}\n",
            "#include <iostream>\n" +
                    "\n" +
                    "int main() {\n" +
                    "    std::cout << \"Hello, C++!\" << std::endl;\n" +
                    "    return 0;\n" +
                    "}\n",
            "public class Main {\n" +
                    "    public static void main(String[] args) {\n" +
                    "        int i = 1/1;\n" +
                    "        System.out.println(\"Hello, Java!!\");\n" +
                    "    }\n" +
                    "}\n",
            "console.log(\"Hello, JavaScript!\");",
            "console.log(\"Hello, TypeScript!\");",
    };
    @PostMapping("/execute_code/mix")
    public BaseResponse<ExecuteCodeResponse> executeCodeMix(){
        Random random=new Random();
        int i = random.nextInt(languageCmdEnums.length);
        ExecuteCodeRequest executeCodeRequest=new ExecuteCodeRequest();
//        executeCodeRequest.setInputList();
        executeCodeRequest.setCode(codes[i]);
        executeCodeRequest.setLanguage(languageCmdEnums[i].getLanguage());

        return ResultUtils.success(codeSandbox.executeCode(executeCodeRequest));
    }
}
