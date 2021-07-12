package com.jeequan.jeepay.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.jeequan.jeepay.oss.config.AliyunOssYmlConfig;
import com.jeequan.jeepay.oss.constant.OssSavePlaceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@Service
@Slf4j
@ConditionalOnProperty(name = "isys.oss.service-type", havingValue = "aliyun-oss")
public class AliyunOssService implements IOssService{

    @Autowired private AliyunOssYmlConfig aliyunOssYmlConfig;

    @Override
    public String upload2PreviewUrl(OssSavePlaceEnum ossSavePlaceEnum, MultipartFile multipartFile, String saveDirAndFileName) {

        try {
            // 创建OSSClient实例。
            OSS client = new OSSClientBuilder().build(aliyunOssYmlConfig.getEndpoint(), aliyunOssYmlConfig.getAccessKeyId(), aliyunOssYmlConfig.getAccessKeySecret());
            client.putObject(aliyunOssYmlConfig.getPublicBucketName(), saveDirAndFileName, multipartFile.getInputStream());

            if(ossSavePlaceEnum == OssSavePlaceEnum.PUBLIC){
                // 文档：https://www.alibabacloud.com/help/zh/doc-detail/39607.htm  example: https://BucketName.Endpoint/ObjectName
                return "https://" + aliyunOssYmlConfig.getPublicBucketName() + "." + aliyunOssYmlConfig.getEndpoint() + "/" + saveDirAndFileName;
            }

            return saveDirAndFileName;

        } catch (Exception e) {
           log.error("error", e);
            return null;
        }
    }

    @Override
    public boolean downloadFile(OssSavePlaceEnum ossSavePlaceEnum, String source, String target) {

        try {
            // 创建OSSClient实例。
            OSS client = new OSSClientBuilder().build(aliyunOssYmlConfig.getEndpoint(), aliyunOssYmlConfig.getAccessKeyId(), aliyunOssYmlConfig.getAccessKeySecret());

            String bucket = ossSavePlaceEnum == OssSavePlaceEnum.PRIVATE ? aliyunOssYmlConfig.getPrivateBucketName() : aliyunOssYmlConfig.getPublicBucketName();

            client.getObject(new GetObjectRequest(bucket, source), new File(target));

            return true;
        } catch (Exception e) {
            log.error("error", e);
            return false;
        }
    }

}
