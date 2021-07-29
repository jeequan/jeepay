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
package com.jeequan.jeepay.components.oss.model;

import com.jeequan.jeepay.components.oss.constant.OssSavePlaceEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/*
* 定义文件上传的配置信息
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:38
*/
@Data
@AllArgsConstructor
public class OssFileConfig {

    /** 用户头像 **/
    interface BIZ_TYPE {
        String AVATAR = "avatar"; /** 用户头像 **/
        String IF_BG = "ifBG"; /** 接口类型卡片背景图片 **/
        String CERT = "cert";  /** 接口参数 **/
    }

    /** 图片类型后缀格式 **/
    public static final Set IMG_SUFFIX = new HashSet(Arrays.asList("jpg", "png", "jpeg", "gif"));

    /** 全部后缀格式的文件标识符 **/
    public static final String ALL_SUFFIX_FLAG = "*";

    /** 不校验文件大小标识符 **/
    public static final Long ALL_MAX_SIZE = -1L;

    /** 允许上传的最大文件大小的默认值 **/
    public static final Long DEFAULT_MAX_SIZE = 5 * 1024 * 1024L;

    private static final Map<String, OssFileConfig> ALL_BIZ_TYPE_MAP = new HashMap<>();
    static{
        ALL_BIZ_TYPE_MAP.put(BIZ_TYPE.AVATAR, new OssFileConfig(OssSavePlaceEnum.PUBLIC, IMG_SUFFIX, DEFAULT_MAX_SIZE) );
        ALL_BIZ_TYPE_MAP.put(BIZ_TYPE.IF_BG, new OssFileConfig(OssSavePlaceEnum.PUBLIC, IMG_SUFFIX, DEFAULT_MAX_SIZE) );
        ALL_BIZ_TYPE_MAP.put(BIZ_TYPE.CERT, new OssFileConfig(OssSavePlaceEnum.PRIVATE, new HashSet<>(Arrays.asList(ALL_SUFFIX_FLAG)), DEFAULT_MAX_SIZE) );
    }

    /** 存储位置 **/
    private OssSavePlaceEnum ossSavePlaceEnum;

    /** 允许的文件后缀, 默认全部类型 **/
    private Set<String> allowFileSuffix = new HashSet<>(Arrays.asList(ALL_SUFFIX_FLAG));

    /** 允许的文件大小, 单位： Byte **/
    private Long maxSize = DEFAULT_MAX_SIZE;


    /** 是否在允许的文件类型后缀内 **/
    public boolean isAllowFileSuffix(String fixSuffix){

        if(this.allowFileSuffix.contains(ALL_SUFFIX_FLAG)){ //允许全部
            return true;
        }

        return this.allowFileSuffix.contains(StringUtils.defaultIfEmpty(fixSuffix, "").toLowerCase());
    }

    /** 是否在允许的大小范围内 **/
    public boolean isMaxSizeLimit(Long fileSize){

        if(ALL_MAX_SIZE.equals(this.maxSize)){ //允许全部大小
            return true;
        }

        return this.maxSize >= ( fileSize == null ? 0L : fileSize);
    }


    public static OssFileConfig getOssFileConfigByBizType(String bizType){
        return ALL_BIZ_TYPE_MAP.get(bizType);
    }

}
