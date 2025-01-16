package cn.bulgat.codesandbox.model.vo;

import cn.bulgat.codesandbox.model.entity.Auth;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;

@Data
public class AuthVO implements Serializable {

    private Long id;

    /**
     * accessKey
     */
    private String accessKey;

    /**
     * secretKey
     */
    private String secretKey;

    /**
     * 创建者 id
     */
    private Long userId;

    private UserVO userVO;

    private static final long serialVersionUID = 1L;

    public static AuthVO objToVO(Auth auth){
        if (auth == null) {
            return null;
        }
        AuthVO authVO = new AuthVO();
        BeanUtils.copyProperties(auth, authVO);
        return authVO;
    }
}