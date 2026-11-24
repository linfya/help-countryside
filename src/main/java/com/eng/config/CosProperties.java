package com.eng.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 腾讯云对象存储配置，对应 application.yaml 中的 tencent.cos 节点。
 */
@Data
@Component
@ConfigurationProperties(prefix = "tencent.cos")
public class CosProperties {

    /** 访问密钥 Id */
    private String secretId;

    /** 访问密钥 Key */
    private String secretKey;

    /** 存储桶名称 */
    private String bucketName;

    /** 存储桶所在地域，例如 ap-shanghai */
    private String region;

    /** 存储桶访问域名，用于拼接文件的公网地址 */
    private String url;
}
