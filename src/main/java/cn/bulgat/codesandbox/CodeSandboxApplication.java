package cn.bulgat.codesandbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("cn.bulgat.codesandbox.mapper")
@EnableConfigurationProperties
@EnableAspectJAutoProxy(proxyTargetClass = true,exposeProxy = true)
public class CodeSandboxApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeSandboxApplication.class, args);
    }

}
