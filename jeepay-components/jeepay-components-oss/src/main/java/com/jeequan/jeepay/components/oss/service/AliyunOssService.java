/*
 * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
 * <p>
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.gnu.org/licenses/lgpl.html
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeequan.jeepay.components.oss.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GetObjectRequest;
import com.jeequan.jeepay.components.oss.config.AliyunOssYmlConfig;
import com.jeequan.jeepay.components.oss.config.OssYmlConfig;
import com.jeequan.jeepay.components.oss.constant.OssSavePlaceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 阿里云OSS 实现类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/7/12 18:20
 */
@Service
@Slf4j
@ConditionalOnProperty(name = "isys.oss.service-type", havingValue = "aliyun-oss")
public class AliyunOssService implements IOssService {

    @Autowired
    private AliyunOssYmlConfig aliyunOssYmlConfig;

    @Autowired
    private OssYmlConfig ossYmlConfig;

    // ossClient 初始化
    private OSS ossClient = null;

    @PostConstruct
    public void init() {
        ossClient = new OSSClientBuilder().build(aliyunOssYmlConfig.getEndpoint(), aliyunOssYmlConfig.getAccessKeyId(),
                aliyunOssYmlConfig.getAccessKeySecret());
    }

    /**
     * 处理文件保存路径
     *
     * @param ossSavePlaceEnum 保存位置
     * @param filePath         文件路径
     * @return 完整路径
     */
    private String getFileKey(OssSavePlaceEnum ossSavePlaceEnum, String filePath) {
        // 上传的时候需要考虑 OSS 存储空间的访问权限，并拼接路径前缀
        String filePrefix = ossSavePlaceEnum == OssSavePlaceEnum.PUBLIC ? ossYmlConfig.getOss()
                .getFilePublicPath() : ossYmlConfig.getOss().getFilePrivatePath();

        // 如果路径包含设置的路径前缀，则跳过
        if (filePath.startsWith(filePrefix)) {
            // OSS 不允许路径第一个字符为 /
            if (filePath.indexOf("/") == 0) {
                filePath = filePath.replaceFirst("/", "");
            }
            return filePath;
        }

        String fullPath = (filePrefix + "/" + filePath);

        // OSS 不允许路径第一个字符为 /
        if (fullPath.indexOf("/") == 0) {
            fullPath = fullPath.replaceFirst("/", "");
        }

        return fullPath;
    }

    @Override
    public String upload2PreviewUrl(OssSavePlaceEnum ossSavePlaceEnum, MultipartFile multipartFile,
                                    String saveDirAndFileName) {

        try {

            String fullPath = getFileKey(ossSavePlaceEnum, saveDirAndFileName);

            this.ossClient.putObject(
                    ossSavePlaceEnum == OssSavePlaceEnum.PUBLIC ? aliyunOssYmlConfig.getPublicBucketName() : aliyunOssYmlConfig.getPrivateBucketName()
                    , fullPath, multipartFile.getInputStream());

            if (ossSavePlaceEnum == OssSavePlaceEnum.PUBLIC) {
                // 文档：https://www.alibabacloud.com/help/zh/doc-detail/39607.htm  example: https://BucketName.Endpoint/ObjectName
                return "https://" + aliyunOssYmlConfig.getPublicBucketName() + "." + aliyunOssYmlConfig.getEndpoint() + "/" + fullPath;
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
            String fullPath = getFileKey(ossSavePlaceEnum, source);

            File downloadFile = new File(target);

            // 当本地路径的上层目录不存在时，自动创建
            // OSS SDK 在 Docker 内部可能出现 UnknownHost 错误具体表现为
            // com.aliyun.oss.ClientException: Cannot read the content input stream.
            if (!downloadFile.getParentFile().exists()) {
                log.info("downloadFile parent dir not exists create it: {}",
                        downloadFile.getParentFile().getAbsolutePath());
                downloadFile.getParentFile().mkdirs();
            }

            String bucket = ossSavePlaceEnum == OssSavePlaceEnum.PRIVATE ? aliyunOssYmlConfig.getPrivateBucketName() : aliyunOssYmlConfig.getPublicBucketName();
            this.ossClient.getObject(new GetObjectRequest(bucket, source), downloadFile);

            return true;
        } catch (Exception e) {
            log.error("error", e);
            return false;
        }
    }

}
