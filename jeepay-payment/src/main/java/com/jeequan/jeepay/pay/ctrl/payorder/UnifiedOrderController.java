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
package com.jeequan.jeepay.pay.ctrl.payorder;

import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.PayOrder;
import com.jeequan.jeepay.core.entity.PayWay;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRQ;
import com.jeequan.jeepay.pay.rqrs.payorder.UnifiedOrderRS;
import com.jeequan.jeepay.pay.rqrs.payorder.payway.AutoBarOrderRQ;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.service.impl.PayWayService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 统一下单 controller
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:27
*/
@Slf4j
@RestController
public class UnifiedOrderController extends AbstractPayOrderController {

    @Autowired private PayWayService payWayService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/unifiedOrder")
    public ApiRes unifiedOrder(){

        //获取参数 & 验签
        UnifiedOrderRQ rq = getRQByWithMchSign(UnifiedOrderRQ.class);

        UnifiedOrderRQ bizRQ = buildBizRQ(rq);

        //实现子类的res
        ApiRes apiRes = unifiedOrder(bizRQ.getWayCode(), bizRQ);
        if(apiRes.getData() == null){
            return apiRes;
        }

        UnifiedOrderRS bizRes = (UnifiedOrderRS)apiRes.getData();

        //聚合接口，返回的参数
        UnifiedOrderRS res = new UnifiedOrderRS();
        BeanUtils.copyProperties(bizRes, res);

        //只有 订单生成（QR_CASHIER） || 支付中 || 支付成功返回该数据
        if(bizRes.getOrderState() != null && (bizRes.getOrderState() == PayOrder.STATE_INIT || bizRes.getOrderState() == PayOrder.STATE_ING || bizRes.getOrderState() == PayOrder.STATE_SUCCESS) ){
            res.setPayDataType(bizRes.buildPayDataType());
            res.setPayData(bizRes.buildPayData());
        }

        return ApiRes.okWithSign(res, configContextQueryService.queryMchApp(rq.getMchNo(), rq.getAppId()).getAppSecret());
    }


    private UnifiedOrderRQ buildBizRQ(UnifiedOrderRQ rq){

        //支付方式  比如： ali_bar
        String wayCode = rq.getWayCode();

        //jsapi 收银台聚合支付场景 (不校验是否存在payWayCode)
        if(CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)){
            return rq.buildBizRQ();
        }

        //如果是自动分类条码
        if(CS.PAY_WAY_CODE.AUTO_BAR.equals(wayCode)){

            AutoBarOrderRQ bizRQ = (AutoBarOrderRQ)rq.buildBizRQ();
            wayCode = JeepayKit.getPayWayCodeByBarCode(bizRQ.getAuthCode());
            rq.setWayCode(wayCode.trim());
        }

        if(payWayService.count(PayWay.gw().eq(PayWay::getWayCode, wayCode)) <= 0){
            throw new BizException("不支持的支付方式");
        }

        //转换为 bizRQ
        return rq.buildBizRQ();
    }


}
