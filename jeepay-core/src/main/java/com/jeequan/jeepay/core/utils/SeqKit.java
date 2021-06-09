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
package com.jeequan.jeepay.core.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/*
* 序列号生成 工具类
*
* @author terrfly
* @site https://www.jeepay.vip
* @date 2021/6/8 16:56
*/
public class SeqKit {

	private static final AtomicLong BUY_ORDER_SEQ = new AtomicLong(0L);
	private static final String BUY_ORDER_SEQ_PREFIX = "P";

	/** 生成购买订单ID **/
	public static String genPayOrderId() {

		return String.format("%s%s%04d",BUY_ORDER_SEQ_PREFIX,
				DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
				(int) BUY_ORDER_SEQ.getAndIncrement() % 10000);
	}

}
