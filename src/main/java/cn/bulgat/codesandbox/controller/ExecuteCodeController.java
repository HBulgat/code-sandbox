package cn.bulgat.codesandbox.controller;

import cn.bulgat.codesandbox.codesandbox.CodeSandbox;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequestByFileOrText;
import cn.bulgat.codesandbox.model.vo.codesandbox.ExecuteCodeResponse;
import cn.bulgat.codesandbox.service.CodeSandboxService;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.bulgat.codesandbox.annotation.ApiAuthCheck;
import cn.bulgat.codesandbox.common.BaseResponse;
import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.common.ResultUtils;
import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.model.dto.codesandbox.ExecuteCodeRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/execute")
public class ExecuteCodeController {
    @Resource
    private CodeSandboxService codeSandboxService;

    @PostMapping("/execute")
    @ApiAuthCheck
    public BaseResponse<ExecuteCodeResponse> executeCode(@RequestPart(value = "files",required = false) MultipartFile[] files,
                                                         @RequestPart("executeCodeRequest") ExecuteCodeRequest executeCodeRequest,
                                                         HttpServletRequest request
                                                         ){
        if (executeCodeRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Map<String,MultipartFile> fileMap=new HashMap<>();

        if (files!=null&&files.length>0){
            for (MultipartFile file : files) {
                fileMap.put(file.getOriginalFilename(),file);
            }
        }
        return ResultUtils.success(codeSandboxService.executeCode(executeCodeRequest,fileMap));
    }

//    @PostMapping("/execute_code")
//    @ApiAuthCheck
//    public BaseResponse<String> executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest request){
//        if (executeCodeRequest==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        return ResultUtils.success(JSONUtil.toJsonStr(codeSandboxService.executeCode(executeCodeRequest)));
//    }

//    /**
//     * 通过文件或文本发送请求
//     * @param files
//     * @param request
//     * @return
//     */
//    @PostMapping("/execute_code/file")
//    @ApiAuthCheck
//    public BaseResponse<String> executeCodeByFile(@RequestPart(value = "files",required = false) MultipartFile[] files,
//                                                  @RequestPart("executeCodeRequest") ExecuteCodeRequestByFileOrText executeCodeRequest,
//                                                  HttpServletRequest request){
////        if (StrUtil.isBlank(executeCodeRequestJSON)){
////            throw new BusinessException(ErrorCode.PARAMS_ERROR);
////        }
//        System.out.println(executeCodeRequest);
////        ExecuteCodeRequestByFileOrText executeCodeRequestByFileOrText=JSONUtil.toBean(executeCodeRequestJSON,ExecuteCodeRequestByFileOrText.class);
//        if (executeCodeRequest==null){
//            throw new BusinessException(ErrorCode.PARAMS_ERROR);
//        }
//        Map<String,MultipartFile> fileMap=new HashMap<>();
//        if (files!=null&&files.length>0)
//            for (MultipartFile file : files) {
//                fileMap.put(file.getOriginalFilename(),file);
//            }
////        return ResultUtils.success("1111");
////        if (executeCodeRequestByFileOrText==null){
////            throw new BusinessException(ErrorCode.PARAMS_ERROR);
////        }
////        Map<String,MultipartFile> fileMap=new HashMap<>();
////        for (MultipartFile file : files) {
////            fileMap.put(file.getOriginalFilename(),file);
////        }
////        return ResultUtils.success(JSONUtil.toJsonStr(codeSandboxService.executeCode(executeCodeRequestByFileOrText,fileMap)));
//        return ResultUtils.success(executeCodeRequest.toString());
//    }
//
//    @PostMapping("/test")
//    @ApiAuthCheck
//    public BaseResponse<String> testFile(@RequestPart("files") MultipartFile[] files,
//                                         @RequestPart("executeCodeRequest") String executeCodeRequestJSON,
//                                         HttpServletRequest request) {
//        return ResultUtils.success("files.length=" + files.length + ",files[0].name=" + files[0].getOriginalFilename() + ",executeCodeRequest=" + executeCodeRequestJSON);
//    }
}
