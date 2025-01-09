package com.bulgat.codesandbox.common;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@AllArgsConstructor
@Slf4j
public class NonceStorage {
    private long expireTime;
    private ConcurrentMap<Integer,Long> nonceMap;

    public NonceStorage(long expireTime){
        this(expireTime,new ConcurrentHashMap<>());
    }
    public void add(Integer nonce,Long timestamp){
        nonceMap.put(nonce,timestamp);
    }

    public int size(){
        return nonceMap.size();
    }
    public boolean isExpire(Long timestamp){
        long currentTimeMillis=System.currentTimeMillis();
        return currentTimeMillis-timestamp>expireTime;
    }
    public boolean exists(Integer nonce){
        return nonceMap.containsKey(nonce);
    }
    public void clearExpireNonceStorage(){
        if(nonceMap.size()==0) {
            return;
        }
        long currentTimeMillis=System.currentTimeMillis();
        nonceMap.forEach((nonce,timestamp)->{
            if (currentTimeMillis-timestamp>expireTime){
                nonceMap.remove(nonce);
            }
        });
    }
}
