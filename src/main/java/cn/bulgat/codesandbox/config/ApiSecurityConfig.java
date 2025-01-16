package cn.bulgat.codesandbox.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;


@Slf4j
@Configuration
@ConfigurationProperties("api-security")
@Data
public class ApiSecurityConfig {

    private boolean enable;

    private Nonce nonce;

    @Data
    @Configuration
    @ConfigurationProperties("api-security.nonce")
    public static class Nonce{
        private long expireTime=120000L;
        private long maximumSize=1_000_000_00L;
        @Bean
        public Cache<Integer,Long> nonceCache(){
            Cache<Integer,Long> nonceCache= Caffeine.newBuilder()
                    .expireAfterWrite(expireTime, TimeUnit.MILLISECONDS)
                    .maximumSize(maximumSize)
                    .build();
            return nonceCache;
        }
    }


}
