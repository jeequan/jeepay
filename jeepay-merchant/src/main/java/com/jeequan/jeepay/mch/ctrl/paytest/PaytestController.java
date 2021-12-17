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
package com.jeequan.jeepay.mch.ctrl.paytest;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.model.DBApplicationConfig;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.jeequan.jeepay.service.impl.MchAppService;
import com.jeequan.jeepay.service.impl.MchPayPassageService;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.Set;

/*
* 支付测试类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/22 9:43
*/
@RestController
@RequestMapping("/api/paytest")
public class PaytestController extends CommonCtrl {

    @Autowired private MchAppService mchAppService;
    @Autowired private MchPayPassageService mchPayPassageService;
    @Autowired private SysConfigService sysConfigService;

    /** 查询商户对应应用下支持的支付方式 **/
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_TEST_PAYWAY_LIST')")
    @GetMapping("/payways/{appId}")
    public ApiRes payWayList(@PathVariable("appId") String appId) {

        Set<String> payWaySet = new HashSet<>();
        mchPayPassageService.list(
                MchPayPassage.gw().select(MchPayPassage::getWayCode)
                        .eq(MchPayPassage::getMchNo, getCurrentMchNo())
                        .eq(MchPayPassage::getAppId, appId)
                        .eq(MchPayPassage::getState, CS.PUB_USABLE)
        ).stream().forEach(r -> payWaySet.add(r.getWayCode()));

        return ApiRes.ok(payWaySet);
    }


    /** 调起下单接口 **/
    @PreAuthorize("hasAuthority('ENT_MCH_PAY_TEST_DO')")
    @PostMapping("/payOrders")
    public ApiRes doPay() {

        //获取请求参数
        String appId = getValStringRequired("appId");
        Long amount = getRequiredAmountL("amount");
        String mchOrderNo = getValStringRequired("mchOrderNo");
        String wayCode = getValStringRequired("wayCode");

        Byte divisionMode = getValByteRequired("divisionMode");
        String orderTitle = getValStringRequired("orderTitle");

        if(StringUtils.isEmpty(orderTitle)){
            throw new BizException("订单标题不能为空");
        }

        // 前端明确了支付参数的类型 payDataType
        String payDataType = getValString("payDataType");
        String authCode = getValString("authCode");


        MchApp mchApp = mchAppService.getById(appId);
        if(mchApp == null || mchApp.getState() != CS.PUB_USABLE || !mchApp.getAppId().equals(appId)){
            throw new BizException("商户应用不存在或不可用");
        }

        PayOrderCreateRequest request = new PayOrderCreateRequest();
        PayOrderCreateReqModel model = new PayOrderCreateReqModel();
        request.setBizModel(model);

        model.setMchNo(getCurrentMchNo()); // 商户号
        model.setAppId(appId);
        model.setMchOrderNo(mchOrderNo);
        model.setWayCode(wayCode);
        model.setAmount(amount);
        // paypal通道使用USD类型货币
        if(wayCode.equalsIgnoreCase("pp_pc")) {
            model.setCurrency("USD");
        }else {
            model.setCurrency("CNY");
        }
        model.setClientIp(getClientIp());
        model.setSubject(orderTitle + "[" + getCurrentMchNo() + "商户联调]");
        model.setBody(orderTitle + "[" + getCurrentMchNo() + "商户联调]");

        DBApplicationConfig dbApplicationConfig = sysConfigService.getDBApplicationConfig();

        model.setNotifyUrl(dbApplicationConfig.getMchSiteUrl() + "/api/anon/paytestNotify/payOrder"); //回调地址
        model.setDivisionMode(divisionMode); //分账模式

        //设置扩展参数
        JSONObject extParams = new JSONObject();
        if(StringUtils.isNotEmpty(payDataType)) {
            extParams.put("payDataType", payDataType.trim());
        }
        if(StringUtils.isNotEmpty(authCode)) {
            extParams.put("authCode", authCode.trim());
        }
        model.setChannelExtra(extParams.toString());

        JeepayClient jeepayClient = new JeepayClient(dbApplicationConfig.getPaySiteUrl(), mchApp.getAppSecret());

        try {
            PayOrderCreateResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                throw new BizException(response.getMsg());
            }
            return ApiRes.ok(response.get());
        } catch (JeepayException e) {
            throw new BizException(e.getMessage());
        }
    }

}
