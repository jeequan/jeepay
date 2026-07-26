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
package com.jeequan.jeepay.pay.rqrs.payorder.payway;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.rqrs.payorder.CommonPayDataRQ;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/*
 * 支付方式：STARPOS_QR
 */
@Data
public class StarposQrOrderRQ extends CommonPayDataRQ {

    /** 构造函数 **/
    public StarposQrOrderRQ() {
        this.setWayCode(CS.PAY_WAY_CODE.STARPOS_QR);
    }

    /** 首期仅支持返回支付链接 **/
    @Override
    @NotBlank(message = "payDataType不能为空")
    @Pattern(regexp = "payUrl", message = "payDataType仅支持payUrl")
    public String getPayDataType() {
        return super.getPayDataType();
    }
}
