package com.jeecg.p3.system.config;

import com.jeecg.p3.baseApi.util.OSSBootUtil;
import com.jeecg.p3.baseApi.util.OSSMinioUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssBootConfiguration {

    @Value("${oss.endpoint}")
    private String endpoint;
    @Value("${oss.accessKeyId}")
    private String accessKeyId;
    @Value("${oss.accessKeySecret}")
    private String accessKeySecret;
    @Value("${oss.bucketName}")
    private String bucketName;
    @Value("${oss.imgDomain}")
    private String imgDomain;

    @Value("${minio.endpoint}")
    private String minioEndpoint;

    @Value("${minio.accessKey}")
    private String minioAccessKey;

    @Value("${minio.secretKey}")
    private String minioSecretKey;

    @Value("${minio.bucketName}")
    private String minioBucketName;

    @Value("${minio.fileDomain}")
    private String fileDomain;


    @Bean
    public void initStatic() {
        OSSBootUtil.setEndPoint(endpoint);
        OSSBootUtil.setAccessKeyId(accessKeyId);
        OSSBootUtil.setAccessKeySecret(accessKeySecret);
        OSSBootUtil.setBucketName(bucketName);
        OSSBootUtil.setImgDomain(imgDomain);

        OSSMinioUtil.setEndpoint(minioEndpoint);
        OSSMinioUtil.setAccessKey(minioAccessKey);
        OSSMinioUtil.setSecretKey(minioSecretKey);
        OSSMinioUtil.setBucketName(minioBucketName);
        OSSMinioUtil.setFileDomain(fileDomain);
    }
}