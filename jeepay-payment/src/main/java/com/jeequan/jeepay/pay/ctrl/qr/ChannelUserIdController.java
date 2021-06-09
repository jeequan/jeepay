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
package com.jeequan.jeepay.pay.ctrl.qr;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.utils.JeepayKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IChannelUserService;
import com.jeequan.jeepay.pay.ctrl.payorder.AbstractPayOrderController;
import com.jeequan.jeepay.pay.rqrs.ChannelUserIdRQ;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.model.MchConfigContext;
import com.jeequan.jeepay.service.impl.SysConfigService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;

/*
* 商户获取渠道用户ID接口
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 17:27
*/
@RestController
@RequestMapping("/api/channelUserId")
public class ChannelUserIdController extends AbstractPayOrderController {

    @Autowired private ConfigContextService configContextService;
    @Autowired private SysConfigService sysConfigService;

    /**  重定向到微信地址  **/
    @RequestMapping("/jump")
    public void jump() throws Exception {

        //获取请求数据
        ChannelUserIdRQ rq = getRQByWithMchSign(ChannelUserIdRQ.class);

        String ifCode = "AUTO".equalsIgnoreCase(rq.getIfCode()) ? getIfCodeByUA() : rq.getIfCode();

        // 获取接口
        IChannelUserService channelUserService = SpringBeansUtil.getBean(ifCode + "ChannelUserService", IChannelUserService.class);

        if(channelUserService == null){
            throw new BizException("不支持的客户端");
        }

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("mchNo", rq.getMchNo());
        jsonObject.put("ifCode", ifCode);
        jsonObject.put("redirectUrl", rq.getRedirectUrl());

        //回调地址
        String callbackUrl = sysConfigService.getDBApplicationConfig().genMchChannelUserIdApiOauth2RedirectUrlEncode(jsonObject);

        //获取商户配置信息
        MchConfigContext mchConfigContext = configContextService.getMchConfigContext(rq.getMchNo());
        String redirectUrl = channelUserService.buildUserRedirectUrl(callbackUrl, mchConfigContext);
        response.sendRedirect(redirectUrl);

    }


    /**  回调地址  **/
    @RequestMapping("/oauth2Callback/{aesData}")
    public void oauth2Callback(@PathVariable("aesData") String aesData) throws Exception {

        JSONObject callbackData = JSON.parseObject(JeepayKit.aesDecode(aesData));

        String mchNo = callbackData.getString("mchNo");
        String ifCode = callbackData.getString("ifCode");
        String redirectUrl = callbackData.getString("redirectUrl");

        // 获取接口
        IChannelUserService channelUserService = SpringBeansUtil.getBean(ifCode + "ChannelUserService", IChannelUserService.class);

        if(channelUserService == null){
            throw new BizException("不支持的客户端");
        }

        //获取商户配置信息
        MchConfigContext mchConfigContext = configContextService.getMchConfigContext(mchNo);

        String channelUserId = channelUserService.getChannelUserId(getReqParamJSON(), mchConfigContext);

        response.sendRedirect(redirectUrl + "?channelId=" + URLEncoder.encode(channelUserId));
    }


    /** 根据UA获取支付接口 */
    private String getIfCodeByUA() {

        String ua = request.getHeader("User-Agent");

        // 无法识别扫码客户端
        if (StringUtils.isBlank(ua)) return null;

        if(ua.contains("Alipay")) {
            return CS.IF_CODE.ALIPAY;  //支付宝服务窗支付
        }else if(ua.contains("MicroMessenger")) {
            return CS.IF_CODE.WXPAY;
        }
        return null;
    }

}
