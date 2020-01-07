package com.jeecg.p3.baseApi.util;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.PutObjectResult;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.apache.commons.fileupload.FileItemStream;
import org.jeecgframework.p3.core.util.oConvertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Minio OOS 上传工具类
 */
public class OSSMinioUtil {

    private static final Logger logger = LoggerFactory.getLogger(OSSMinioUtil.class);

    private static String endpoint;
    private static String accessKey;
    private static String secretKey;
    private static String bucketName;
    private static String fileDomain;

    public static void setEndpoint(String endpoint) {
        OSSMinioUtil.endpoint = endpoint;
    }

    public static void setAccessKey(String accessKey) {
        OSSMinioUtil.accessKey = accessKey;
    }

    public static void setSecretKey(String secretKey) {
        OSSMinioUtil.secretKey = secretKey;
    }

    public static void setBucketName(String bucketName) {
        OSSMinioUtil.bucketName = bucketName;
    }

    public static void setFileDomain(String fileDomain) {
        OSSMinioUtil.fileDomain = fileDomain;
    }

    /**
     * oss 工具客户端
     */
    private static MinioClient minioClient = null;
    private static String FILE_URL;

    /**
     * 上传文件至阿里云 OSS
     * 文件上传成功,返回文件完整访问路径
     * 文件上传失败,返回 null
     *
     * @param file
     *         待上传文件
     * @param fileDir
     *         文件保存目录
     * @return oss 中的相对文件路径
     */
    public static String upload(MultipartFile file, String fileDir) {
        initOSS(endpoint, accessKey, secretKey);
        StringBuilder fileUrl = new StringBuilder();
        String suffix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        if (!fileDir.endsWith("/")) {
            fileDir = fileDir.concat("/");
        }
        fileUrl = fileUrl.append(fileDir + fileName);

        if (oConvertUtils.isNotEmpty(fileDomain)) {
            FILE_URL = fileDomain + "?filePath=" + fileUrl;
        } else {
            FILE_URL = "https://" + bucketName + "." + endpoint + "/" + fileUrl;
        }

        try {
            minioClient.putObject(bucketName, fileUrl.toString(), file.getInputStream(), file.getSize(), null, null, "application/octet-stream");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("------OSS文件上传失败------", e);
        }
        logger.info("------OSS文件上传成功------ {}", fileUrl);
        return FILE_URL;
    }


    /**
     * 上传文件至阿里云 OSS
     * 文件上传成功,返回文件完整访问路径
     * 文件上传失败,返回 null
     *
     * @param file
     *         待上传文件
     * @param fileDir
     *         文件保存目录
     * @return oss 中的相对文件路径
     */
    public static String upload(FileItemStream file, String fileDir) {
        initOSS(endpoint, accessKey, secretKey);
        StringBuilder fileUrl = new StringBuilder();
        String suffix = file.getName().substring(file.getName().lastIndexOf('.'));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;
        if (!fileDir.endsWith("/")) {
            fileDir = fileDir.concat("/");
        }
        fileUrl = fileUrl.append(fileDir + fileName);

        if (oConvertUtils.isNotEmpty(fileDomain)) {
            FILE_URL = fileDomain + "/" + fileUrl;
        } else {
            FILE_URL = "https://" + bucketName + "." + endpoint + "/" + fileUrl;
        }

        try {
            minioClient.putObject(bucketName, fileName, file.openStream(), null, null, null, "application/octet-stream");
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("------OSS文件上传失败------", e);
        }
        logger.info("------OSS文件上传成功------ {}", fileUrl);
        return FILE_URL;
    }

    /**
     * 下载文件
     *
     * @param fileName
     *            存储文件名
     * @return
     */
    public static InputStream download(String fileName) {
        initOSS(endpoint, accessKey, secretKey);
        try {
            InputStream stream = minioClient.getObject(bucketName, fileName);
            return stream;
        } catch (Exception e) {
            logger.info("Minio client error occurred:", e);
        }
        return null;
    }

    /**
     * 移除文件
     *
     * @param fileName
     *            存储文件名
     * @return
     */
    public static boolean remove(String fileName) {
        boolean result;
        initOSS(endpoint, accessKey, secretKey);
        try {
            minioClient.removeObject(bucketName, fileName);
            result = true;
        } catch (Exception e) {
            logger.info("Minio remove object error occurred:", e);
            result = false;
        }
        return result;
    }

    /**
     * 初始化 oss 客户端
     *
     * @return
     */
    private static MinioClient initOSS(String endpoint, String accessKey, String secretKey) {
        if (minioClient == null) {
            try {
                minioClient = new MinioClient(endpoint, accessKey, secretKey);
            } catch (MinioException e) {
                e.printStackTrace();
            }
        }
        return minioClient;
    }

}