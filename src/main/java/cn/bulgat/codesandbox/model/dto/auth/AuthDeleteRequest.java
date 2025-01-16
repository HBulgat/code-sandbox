package cn.bulgat.codesandbox.model.dto.auth;

import lombok.Data;

import java.io.Serializable;

@Data
public class AuthDeleteRequest implements Serializable {
    private String accessKey;
}
