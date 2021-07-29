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
package com.jeequan.jeepay.pay.exception;

import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import lombok.Getter;

/*
* 请求渠道侧异常 exception
* 抛出此异常： 仅支持：  未知状态（需查单） 和 系统内异常
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:28
*/
@Getter
public class ChannelException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	private ChannelRetMsg channelRetMsg;

	/** 业务自定义异常 **/
	private ChannelException(ChannelRetMsg channelRetMsg) {
		super(channelRetMsg != null ? channelRetMsg.getChannelErrMsg() : null);
		this.channelRetMsg = channelRetMsg;
	}

	/** 未知状态 **/
	public static ChannelException unknown(String channelErrMsg){
		return new ChannelException(ChannelRetMsg.unknown(channelErrMsg));
	}

	/** 系统内异常 **/
	public static ChannelException sysError(String channelErrMsg){
		return new ChannelException(ChannelRetMsg.sysError(channelErrMsg));
	}


}
