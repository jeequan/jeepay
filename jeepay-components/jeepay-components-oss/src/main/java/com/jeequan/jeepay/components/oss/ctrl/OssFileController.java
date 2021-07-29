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
package com.jeequan.jeepay.components.oss.ctrl;

import cn.hutool.core.lang.UUID;
import com.jeequan.jeepay.components.oss.service.IOssService;
import com.jeequan.jeepay.core.constants.ApiCodeEnum;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.FileKit;
import com.jeequan.jeepay.components.oss.model.OssFileConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/*
* 统一文件上传接口（ossFile）
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:07
*/
@RestController
@RequestMapping("/api/ossFiles")
public class OssFileController extends AbstractCtrl {

    @Autowired private IOssService ossService;

    /** 上传文件 （单文件上传） */
    @PostMapping("/{bizType}")
    public ApiRes singleFileUpload(@RequestParam("file") MultipartFile file, @PathVariable("bizType") String bizType) {

        if( file == null ) {
            return ApiRes.fail(ApiCodeEnum.SYSTEM_ERROR, "选择文件不存在");
        }
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

            // 新文件地址 (xxx/xxx.jpg 格式)
            String saveDirAndFileName = bizType + "/" + UUID.fastUUID() + "." + fileSuffix;
            String url = ossService.upload2PreviewUrl(ossFileConfig.getOssSavePlaceEnum(), file, saveDirAndFileName);
            return ApiRes.ok(url);

        } catch (BizException biz) {
            throw biz;
        } catch (Exception e) {
            logger.error("upload error, fileName = {}", file.getOriginalFilename(), e);
            throw new BizException(ApiCodeEnum.SYSTEM_ERROR);
        }
    }

}
