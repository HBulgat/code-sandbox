package com.bulgat.codesandbox.job.cycle;

import com.bulgat.codesandbox.common.NonceStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CleanExpireNonceStorage {
    @Resource
    private NonceStorage nonceStorage;
    @Scheduled(fixedRate = 2*60*1000)
    public void run(){
        log.info("current nonce storage size is {}",nonceStorage.size());
        log.info("cleaning expire nonce ...");
        nonceStorage.clearExpireNonceStorage();
    }
}
