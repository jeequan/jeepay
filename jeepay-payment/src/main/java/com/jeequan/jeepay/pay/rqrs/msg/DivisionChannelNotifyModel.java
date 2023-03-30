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
package com.jeequan.jeepay.pay.rqrs.msg;

import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/***
* 封装响应结果的数据
 * 直接写：  MutablePair<ResponseEntity, Map<Long, ChannelRetMsg>>  太过复杂！
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2023/3/29 15:50
*/
@Data
public class DivisionChannelNotifyModel {

    /** 响应接口返回的数据 **/
    private ResponseEntity apiRes;

    /** 每一条记录的更新状态 <ID, 结果> **/
    private Map<Long, ChannelRetMsg> recordResultMap;

}
