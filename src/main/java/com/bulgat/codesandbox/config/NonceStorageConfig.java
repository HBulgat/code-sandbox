package com.bulgat.codesandbox.config;

import com.bulgat.codesandbox.common.NonceStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NonceStorageConfig {
    @Value(value = "${api-security.nonce.expireTime:600000}")
    private long expireTime;

    @Bean
    public NonceStorage nonceStorage(){
        return new NonceStorage(expireTime);
    }

}
