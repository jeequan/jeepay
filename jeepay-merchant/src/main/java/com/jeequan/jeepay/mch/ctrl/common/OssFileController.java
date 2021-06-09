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
package com.jeequan.jeepay.mch.ctrl.common;

import cn.hutool.core.lang.UUID;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.OssFileConfig;
import com.jeequan.jeepay.core.utils.FileKit;
import com.jeequan.jeepay.mch.config.SystemYmlConfig;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/*
 * 统一文件上传接口（ossFile）
 *
 * @author terrfly
 * @site https://www.jeepay.vip
 * @date 2021/6/8 17:07
 */
@RestController
@RequestMapping("/api/ossFiles")
public class OssFileController extends CommonCtrl {

    @Autowired private SystemYmlConfig systemYmlConfig;
    @Autowired private SysConfigService sysConfigService;

    /** 上传文件 （单文件上传） */
    @PostMapping("/{bizType}")
    public ApiRes singleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable("bizType") String bizType) {

        if( file == null ) return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "选择文件不存在");
        try {


            OssFileConfig ossFileConfig = OssFileConfig.getOssFileConfigByBizType(bizType);

            //1. 判断bizType 是否可用
            if(ossFileConfig == null){
                throw new BizException("类型有误");
            }

            // 2. 判断文件是否支持
            String fileSuffix = FileKit.getFileSuffix(file.getOriginalFilename(), false);
            if( !ossFileConfig.isAllowFileSuffix(fileSuffix) ){
                throw new BizException("上传文件格式不支持！");
            }

            // 3. 判断文件大小是否超限
            if( !ossFileConfig.isMaxSizeLimit(file.getSize()) ){
                throw new BizException("上传大小请限制在["+ossFileConfig.getMaxSize() / 1024 / 1024 +"M]以内！");
            }


            boolean isAllowPublicRead = ossFileConfig.isAllowPublicRead(); //是否允许公共读，  true：公共读，  false：私有文件

            //公共读 & 是否上传到oss
            boolean isYunOss = false; //TODO 暂时不支持云oss方式
            if(isAllowPublicRead && isYunOss){
                return null;
            }

            //以下为文件上传到本地

            // 新文件地址
            String newFileName = UUID.fastUUID() + "." + fileSuffix;

            // 保存的文件夹名称
            String saveFilePath = isAllowPublicRead ? systemYmlConfig.getOssFile().getPublicPath() : systemYmlConfig.getOssFile().getPrivatePath();
            saveFilePath = saveFilePath + File.separator + bizType + File.separator + newFileName;


            //保存文件
            saveFile(file, saveFilePath);

            //返回响应结果
            String resultUrl = bizType + "/" + newFileName;
            if(isAllowPublicRead){ //允许公共读取
                resultUrl = sysConfigService.getDBApplicationConfig().getOssPublicSiteUrl() + "/" + resultUrl;
            }

            return ApiRes.ok(resultUrl);

        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            logger.error("upload error, fileName = {}", file == null ? null :file.getOriginalFilename(), e);
            throw new BizException(ApiCodeEnum.SYSTEM_ERROR);
        }
    }

}