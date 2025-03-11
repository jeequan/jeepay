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
package com.jeequan.jeepay.pay.channel.wxpay.ctrl;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

/**
* 渠道侧自定义业务ctrl - 微信用户确认API
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2025/3/11 15:59
*/
@Slf4j
@Controller
@RequestMapping("/api/channelbiz/wxpay")
public class wxpayBizController extends AbstractCtrl {

    @Autowired private TransferOrderService transferOrderService;

    @RequestMapping("/transferUserConfirm/{transerNoAES}")
    public String transferUserConfirm(@PathVariable("transerNoAES") String transerNoAES) throws IOException {

        try {
            TransferOrder transferOrder = transferOrderService.getById(JeepayKit.aesDecode(transerNoAES));

            if(transferOrder == null || transferOrder.getState() != TransferOrder.STATE_ING){
                throw new BizException("转账订单不存在或状态不正确");
            }

            if(!CS.IF_CODE.WXPAY.equals(transferOrder.getIfCode())){
                throw new BizException("渠道有误，仅支持微信转账");
            }

            if(StringUtils.isEmpty(transferOrder.getChannelResData())){
                throw new BizException("转账订单数据格式不存在，请重新下单");
            }

            request.setAttribute("channelResData", transferOrder.getChannelResData());
        } catch (Exception e) {
            log.error("微信用户确认异常", e);
            request.setAttribute("errMsg", e.getMessage());
        }

        return "channel/wxpay/wxTransferUserConfirm";
    }


}
