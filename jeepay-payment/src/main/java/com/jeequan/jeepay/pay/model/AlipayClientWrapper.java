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
package com.jeequan.jeepay.pay.model;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayRequest;
import com.alipay.api.AlipayResponse;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.pay.exception.ChannelException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/*
* 支付宝Client 包装类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:28
*/
@Slf4j
@Data
@AllArgsConstructor
public class AlipayClientWrapper {


    //默认为 不使用证书方式
    private Byte useCert = CS.NO;

    /** 缓存支付宝client 对象 **/
    private AlipayClient alipayClient;

    /** 封装支付宝接口调用函数 **/
    public <T extends AlipayResponse> T execute(AlipayRequest<T> request){

        try {

            T alipayResp = null;

            if(useCert != null && useCert == CS.YES){ //证书加密方式
                alipayResp = alipayClient.certificateExecute(request);

            }else{ //key 或者 空都为默认普通加密方式
                alipayResp = alipayClient.execute(request);
            }

            //判断返回的值： // TODO

            return alipayResp;

        } catch (AlipayApiException e) { // 调起接口前出现异常，如私钥问题。  调起后出现验签异常等。

            log.error("调起支付宝execute[AlipayApiException]异常！", e);
            //如果数据返回出现验签异常，则需要抛出： UNKNOWN 异常。
            throw ChannelException.sysError(e.getMessage());

        } catch (Exception e) {
            log.error("调起支付宝execute[Exception]异常！", e);
            throw ChannelException.sysError("调用支付宝client服务异常");
        }
    }

}
