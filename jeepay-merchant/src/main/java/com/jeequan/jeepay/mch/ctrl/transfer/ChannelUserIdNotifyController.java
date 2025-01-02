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
package com.jeequan.jeepay.mch.ctrl.transfer;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.mch.ctrl.CommonCtrl;
import com.jeequan.jeepay.mch.websocket.server.WsChannelUserIdServer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
* 获取用户ID - 回调函数
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/13 17:54
*/
@Tag(name = "商户转账回调函数")
@Controller
@RequestMapping("/api/anon/channelUserIdCallback")
public class ChannelUserIdNotifyController extends CommonCtrl {

    @Operation(summary = "（转账）获取用户ID - 回调函数")
    @Parameters({
            @Parameter(name = "extParam", description = "扩展参数"),
            @Parameter(name = "channelUserId", description = "用户userId"),
            @Parameter(name = "appId", description = "应用ID")
    })
    @RequestMapping("")
    public String channelUserIdCallback() {

        try {
            //请求参数
            JSONObject params = getReqParamJSON();

            String extParam = params.getString("extParam");
            String channelUserId = params.getString("channelUserId");
            String appId = params.getString("appId");

            //推送到前端
            WsChannelUserIdServer.sendMsgByAppAndCid(appId, extParam, channelUserId);

        } catch (Exception e) {
            request.setAttribute("errMsg", e.getMessage());
        }

        return "channelUser/getChannelUserIdPage";
    }
}
