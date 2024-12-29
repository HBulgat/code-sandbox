package com.bulgat.codesandbox.listener;

import cn.hutool.core.io.FileUtil;
import com.bulgat.codesandbox.containerpool.ContainerPoolExecutor;
import com.bulgat.codesandbox.containerpool.DockerDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class CleanContainerListener implements ApplicationListener<ContextClosedEvent> {
    @Resource
    private DockerDao dockerDao;
    @Resource
    private ContainerPoolExecutor containerPoolExecutor;
    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        containerPoolExecutor
                .getContainerPool()
                .forEach(containerInfo -> {
                    FileUtil.del(containerInfo.getUserCodePathName());
                    dockerDao.cleanContainer(containerInfo.getContainerId());
                });
        log.info("container clean end ...");
    }
}
