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
package com.jeequan.jeepay.pay.service;

import com.jeequan.jeepay.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import java.util.Set;

/*
* 通用 Validator
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:47
*/
@Service
public class ValidateService {

    @Autowired private Validator validator;

    public void validate(Object obj){

        Set<ConstraintViolation<Object>> resultSet = validator.validate(obj);
        if(resultSet == null || resultSet.isEmpty()){
            return ;
        }
        resultSet.stream().forEach(item -> {throw new BizException(item.getMessage());});
    }

}
