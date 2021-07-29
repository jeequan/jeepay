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
package com.jeequan.jeepay.core.exception;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

/*
 * Spring Security 框架自定义异常类
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/15 11:23
 */
@Getter
@Setter
public class JeepayAuthenticationException extends InternalAuthenticationServiceException {

    private BizException bizException;

    public JeepayAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public JeepayAuthenticationException(String msg) {
        super(msg);
    }

    public static JeepayAuthenticationException build(String msg){
        return build(new BizException(msg));
    }

    public static JeepayAuthenticationException build(BizException ex){

        JeepayAuthenticationException result = new JeepayAuthenticationException(ex.getMessage());
        result.setBizException(ex);
        return result;
    }

}
