package cn.bulgat.codesandbox.service.impl;

import cn.bulgat.codesandbox.codesandbox.CodeSandbox;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequest;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequestByFileOrText;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import cn.bulgat.codesandbox.service.CodeSandboxService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class CodeSandboxServiceImpl implements CodeSandboxService {
    @Resource
    private CodeSandbox codeSandbox;
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest,Map<String,MultipartFile> fileMap) {
        return codeSandbox.executeCode(executeCodeRequest,fileMap);
    }



//    @Override
//    public ExecuteCodeResponse executeCode(ExecuteCodeRequestByFileOrText executeCodeRequestByFileOrText, Map<String, MultipartFile> fileMap) {
//        return codeSandbox.executeCode(executeCodeRequestByFileOrText,fileMap);
//    }
}
