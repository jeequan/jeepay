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
package com.jeequan.jeepay.pay.rqrs.transfer;

import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.pay.rqrs.AbstractRS;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/*
* 查询转账订单 响应参数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/17 14:08
*/
@Data
public class QueryTransferOrderRS extends AbstractRS {

    /**
     * 转账订单号
     */
    private String transferId;

    /**
     * 商户号
     */
    private String mchNo;

    /**
     * 应用ID
     */
    private String appId;

    /**
     * 商户订单号
     */
    private String mchOrderNo;

    /**
     * 支付接口代码
     */
    private String ifCode;

    /**
     * 入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡
     */
    private String entryType;

    /**
     * 转账金额,单位分
     */
    private Long amount;

    /**
     * 三位货币代码,人民币:cny
     */
    private String currency;

    /**
     * 收款账号
     */
    private String accountNo;

    /**
     * 收款人姓名
     */
    private String accountName;

    /**
     * 收款人开户行名称
     */
    private String bankName;

    /**
     * 转账备注信息
     */
    private String transferDesc;

    /**
     * 支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭
     */
    private Byte state;

    /**
     * 特定渠道发起额外参数
     */
    private String channelExtra;

    /**
     * 渠道订单号
     */
    private String channelOrderNo;

    /** 渠道响应数据（如微信确认数据包）   **/
    private String channelResData;

    /**
     * 渠道支付错误码
     */
    private String errCode;

    /**
     * 渠道支付错误描述
     */
    private String errMsg;

    /**
     * 商户扩展参数
     */
    private String extParam;

    /**
     * 转账成功时间
     */
    private Long successTime;

    /**
     * 创建时间
     */
    private Long createdAt;


    public static QueryTransferOrderRS buildByRecord(TransferOrder record){

        if(record == null){
            return null;
        }

        QueryTransferOrderRS result = new QueryTransferOrderRS();
        BeanUtils.copyProperties(record, result);
        result.setSuccessTime(record.getSuccessTime() == null ? null : record.getSuccessTime().getTime());
        result.setCreatedAt(record.getCreatedAt() == null ? null : record.getCreatedAt().getTime());
        return result;
    }


}
