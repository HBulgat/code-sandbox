package cn.bulgat.codesandbox.containerpool;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 记录容器信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerInfo {
    private String containerId;
    private String userCodePathName;
    private long lastActivityTime;
}
