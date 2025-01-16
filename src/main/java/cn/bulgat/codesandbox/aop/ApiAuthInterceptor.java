package cn.bulgat.codesandbox.aop;

import cn.bulgat.codesandbox.common.ErrorCode;
import cn.bulgat.codesandbox.config.ApiSecurityConfig;
import cn.bulgat.codesandbox.constant.ApiAuthConstant;
import cn.bulgat.codesandbox.exception.BusinessException;
import cn.bulgat.codesandbox.model.entity.Auth;
import cn.bulgat.codesandbox.service.AuthService;
import cn.bulgat.codesandbox.utils.SignUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import cn.bulgat.codesandbox.annotation.ApiAuthCheck;
import com.github.benmanes.caffeine.cache.Cache;
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
 * api签名认证权限校验 AOP
 *
 */
@Aspect
@Component
@Slf4j
public class ApiAuthInterceptor {
    @Resource
    private AuthService authService;

    @Resource
    private ApiSecurityConfig apiSecurityConfig;

    @Resource
    private Cache<Integer,Long> nonceCache;

    /**
     * 执行拦截
     *
     * @param joinPoint
     * @param apiAuthCheck
     * @return
     */
    @Around("@annotation(apiAuthCheck)")
    public Object doInterceptor(ProceedingJoinPoint joinPoint, ApiAuthCheck apiAuthCheck) throws Throwable {
        boolean enable = apiSecurityConfig.isEnable();
        // 如果鉴权关闭，则不操作
        if (!enable){
            return joinPoint.proceed();
        }
        RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request==null){
            log.info("Request is null.");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String accessKey = request.getHeader(ApiAuthConstant.AUTH_HEADER_ACCESS_KEY);
        String nonce=request.getHeader(ApiAuthConstant.AUTH_HEADER_NONCE);
        String timestamp=request.getHeader(ApiAuthConstant.AUTH_HEADER_TIMESTAMP);
        String sign = request.getHeader(ApiAuthConstant.AUTH_HEADER_SIGN);
        String body=request.getHeader(ApiAuthConstant.AUTH_HEADER_BODY);
        if (StringUtils.isAnyBlank(nonce,timestamp,sign)){
            log.info("Api auth check: params have blank.");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Integer nonceIntValue=Integer.valueOf(nonce);
        // 1. 先判断随机数有没有重复
        if(nonceCache.getIfPresent(nonceIntValue)!=null){
            log.info("Nonce repeat.");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Long timestampLongValue = Long.valueOf(timestamp);
        // 2.再判断时间戳
        if (authService.isExpire(timestampLongValue)){
            log.info("Timestamp is expired.");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        nonceCache.put(nonceIntValue,timestampLongValue);
        //3. 从数据库中取数据
        QueryWrapper<Auth> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq(ApiAuthConstant.AUTH_HEADER_ACCESS_KEY,accessKey);
        Auth auth = authService.getOne(queryWrapper);
        if(auth==null){
            log.info("Can not find the auth record.");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Map<String,String> headers=new HashMap<>();
        headers.put(ApiAuthConstant.AUTH_HEADER_ACCESS_KEY,accessKey);
        headers.put(ApiAuthConstant.AUTH_HEADER_NONCE, nonce);
        headers.put(ApiAuthConstant.AUTH_HEADER_BODY,body);
        headers.put(ApiAuthConstant.AUTH_HEADER_TIMESTAMP,timestamp);
        String serverSign = SignUtils.getSign(headers, auth.getSecretKey());
        if (!StringUtils.equals(serverSign,sign)){
            log.info("Sign is invalid.");
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        log.info("The accessKey of auth is {}",auth.getAccessKey());
        // 通过权限校验，放行
        return joinPoint.proceed();
    }
}
