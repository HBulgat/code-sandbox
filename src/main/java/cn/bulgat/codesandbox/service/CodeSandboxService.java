package cn.bulgat.codesandbox.service;

import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequest;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequestByFileOrText;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CodeSandboxService {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest,Map<String,MultipartFile> fileMap);

//    ExecuteCodeResponse executeCode(ExecuteCodeRequestByFileOrText executeCodeRequestByFileOrText, Map<String, MultipartFile> fileMap);
}

