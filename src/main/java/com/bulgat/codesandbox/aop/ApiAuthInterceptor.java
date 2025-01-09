package com.bulgat.codesandbox.aop;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.bulgat.codesandbox.annotation.ApiAuthCheck;
import com.bulgat.codesandbox.common.NonceStorage;
import com.bulgat.codesandbox.constant.ApiAuthConstant;
import com.bulgat.codesandbox.common.ErrorCode;
import com.bulgat.codesandbox.exception.BusinessException;
import com.bulgat.codesandbox.model.entity.Auth;
import com.bulgat.codesandbox.service.AuthService;
import com.bulgat.codesandbox.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 权限校验 AOP
 *
 */
@Aspect
@Component
@Slf4j
public class ApiAuthInterceptor {
    @Resource
    private AuthService authService;

    @Resource
    private NonceStorage nonceStorage;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param apiAuthCheck
     * @return
     */
    @Around("@annotation(apiAuthCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, ApiAuthCheck apiAuthCheck) throws Throwable {
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String accessKey = request.getHeader(ApiAuthConstant.AUTH_HEADER_ACCESS_KEY);
        String nonce=request.getHeader(ApiAuthConstant.AUTH_HEADER_NONCE);
        String timestamp=request.getHeader(ApiAuthConstant.AUTH_HEADER_TIMESTAMP);
        String sign = request.getHeader(ApiAuthConstant.AUTH_HEADER_SIGN);
        String body=request.getHeader(ApiAuthConstant.AUTH_HEADER_BODY);
//        log.info("accessKey="+accessKey);
//        log.info("nonce="+nonce);
//        log.info("timestamp="+timestamp);
//        log.info("sign="+sign);
//        log.info("body="+body);
        if (StringUtils.isAnyBlank(nonce,timestamp,sign)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Integer nonceIntValue=Integer.valueOf(nonce);
        // 1. 先判断随机数有没有重复
        if (nonceStorage.exists(nonceIntValue)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long timestampLongValue = Long.valueOf(timestamp);
        // 2.再判断时间戳
        if (nonceStorage.isExpire(timestampLongValue)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        nonceStorage.add(nonceIntValue,timestampLongValue);
        //3. 从数据库中取数据
        QueryWrapper<Auth> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(ApiAuthConstant.AUTH_HEADER_ACCESS_KEY,accessKey);
        Auth auth = authService.getOne(queryWrapper);
        if(auth==null){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Map<String,String> headers=new HashMap<>();
        headers.put(ApiAuthConstant.AUTH_HEADER_ACCESS_KEY,accessKey);
        headers.put(ApiAuthConstant.AUTH_HEADER_NONCE, nonce);
        headers.put(ApiAuthConstant.AUTH_HEADER_BODY,body);
        headers.put(ApiAuthConstant.AUTH_HEADER_TIMESTAMP,timestamp);
        String serverSign = SignUtils.getSign(headers, auth.getSecretKey());
        if (!StringUtils.equals(serverSign,sign)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
