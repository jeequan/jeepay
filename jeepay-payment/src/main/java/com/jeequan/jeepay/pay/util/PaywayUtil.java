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
package com.jeequan.jeepay.pay.util;

import cn.hutool.core.util.StrUtil;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.pay.channel.IPaymentService;

/*
* 支付方式动态调用Utils
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:46
*/
public class PaywayUtil {

    private static final String PAYWAY_PACKAGE_NAME = "payway";
    private static final String PAYWAYV3_PACKAGE_NAME = "paywayV3";

    /** 获取真实的支付方式Service **/
    public static IPaymentService getRealPaywayService(Object obj, String wayCode){

        try {

            //下划线转换驼峰 & 首字母大写
            String clsName = StrUtil.upperFirst(StrUtil.toCamelCase(wayCode.toLowerCase()));
            return (IPaymentService) SpringBeansUtil.getBean(
                            Class.forName(obj.getClass().getPackage().getName()
                                + "." + PAYWAY_PACKAGE_NAME
                                + "." + clsName)
                    );

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /** 获取微信V3真实的支付方式Service **/
    public static IPaymentService getRealPaywayV3Service(Object obj, String wayCode){

        try {

            //下划线转换驼峰 & 首字母大写
            String clsName = StrUtil.upperFirst(StrUtil.toCamelCase(wayCode.toLowerCase()));
            return (IPaymentService) SpringBeansUtil.getBean(
                    Class.forName(obj.getClass().getPackage().getName()
                            + "." + PAYWAYV3_PACKAGE_NAME
                            + "." + clsName)
            );

        } catch (ClassNotFoundException e) {
            return null;
        }
    }

}
