package com.bulgat.codesandbox.model.dto.auth;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 */
@Data
public class AuthAddRequest implements Serializable {
    private String accessKeyPrefix;
    private static final long serialVersionUID = 1L;
}