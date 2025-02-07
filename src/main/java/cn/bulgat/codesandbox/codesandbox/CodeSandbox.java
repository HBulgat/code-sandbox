package cn.bulgat.codesandbox.codesandbox;

import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequestByFileOrText;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest,Map<String,MultipartFile> fileMap);

//    ExecuteCodeResponse executeCode(ExecuteCodeRequestByFileOrText executeCodeRequest);
}
