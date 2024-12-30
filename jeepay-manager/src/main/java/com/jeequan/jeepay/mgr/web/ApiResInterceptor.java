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
package com.jeequan.jeepay.mgr.web;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
* 数据响应拦截器
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:12
*/
@Component
public class ApiResInterceptor implements HandlerInterceptor {

    /** postHandler是在请求结束之后, 视图渲染之前执行的,但只有preHandle方法返回true的时候才会执行
     * 如果ctrl使用了@RestController或者@ResponseBody注解 则ModelAndView = null, 因为不走视图转换器, 而是走的RequestResponseBodyMethodProcessor。
     * ————————————————
     *
     * **/
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        //do
    }

}
