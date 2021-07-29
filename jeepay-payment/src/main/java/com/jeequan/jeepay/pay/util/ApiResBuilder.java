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


import com.jeequan.jeepay.pay.rqrs.AbstractRS;

/*
* api响应结果构造器
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:45
*/
public class ApiResBuilder {

    /** 构建自定义响应对象, 默认响应成功 **/
    public static <T extends AbstractRS> T buildSuccess(Class<? extends AbstractRS> T){

        try {
            T result = (T)T.newInstance();
            return result;

        } catch (Exception e) { return null; }
    }

}
