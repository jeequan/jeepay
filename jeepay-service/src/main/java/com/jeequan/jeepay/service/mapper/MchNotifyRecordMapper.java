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
package com.jeequan.jeepay.service.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jeequan.jeepay.core.entity.MchNotifyRecord;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 商户通知表 Mapper 接口
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
public interface MchNotifyRecordMapper extends BaseMapper<MchNotifyRecord> {

    Integer updateNotifyResult(@Param("notifyId") Long notifyId, @Param("state") Byte state, @Param("resResult") String resResult);

    /*
     * 功能描述: 更改为通知中 & 增加允许重发通知次数
     * @param notifyId
     * @Author: terrfly
     * @Date: 2021/6/21 17:38
     */
    Integer updateIngAndAddNotifyCountLimit(@Param("notifyId") Long notifyId);

}
