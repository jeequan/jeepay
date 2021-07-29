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

import com.jeequan.jeepay.core.service.ISysConfigService;
import com.jeequan.jeepay.components.oss.config.OssYmlConfig;
import com.jeequan.jeepay.components.oss.constant.OssSavePlaceEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
* 本地存储 实现类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/7/12 18:19
*/
@Service
@Slf4j
@ConditionalOnProperty(name = "isys.oss.service-type", havingValue = "local")
public class LocalFileService implements IOssService{

    @Autowired private ISysConfigService sysConfigService;
    @Autowired private OssYmlConfig ossYmlConfig;

    @Override
    public String upload2PreviewUrl(OssSavePlaceEnum ossSavePlaceEnum, MultipartFile multipartFile, String saveDirAndFileName) {

        try {

            String savePath = ossSavePlaceEnum ==
                    OssSavePlaceEnum.PUBLIC ? ossYmlConfig.getOss().getFilePublicPath() : ossYmlConfig.getOss().getFilePrivatePath();

            File saveFile = new File(savePath + File.separator + saveDirAndFileName);

            //如果文件夹不存在则创建文件夹
            File dir = saveFile.getParentFile();
            if(!dir.exists()) {
                dir.mkdirs();
            }
            multipartFile.transferTo(saveFile);

        } catch (Exception e) {

            log.error("", e);
        }

        // 私有文件 不返回预览文件地址
        if(ossSavePlaceEnum == OssSavePlaceEnum.PRIVATE){
            return saveDirAndFileName;
        }

        return sysConfigService.getDBApplicationConfig().getOssPublicSiteUrl() + "/" + saveDirAndFileName;
    }

    @Override
    public boolean downloadFile(OssSavePlaceEnum ossSavePlaceEnum, String source, String target) {
        return false;
    }
}
