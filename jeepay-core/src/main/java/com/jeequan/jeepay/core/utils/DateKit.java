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

import cn.hutool.core.date.DateUtil;
import com.jeequan.jeepay.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/*
* 时间工具类
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 16:58
*/
public class DateKit {

	/** 获取参数时间当天的开始时间  **/
	public static Date getBegin(Date date){

		if(date == null) {
            return null;
        }
		return DateUtil.beginOfDay(date).toJdkDate();
	}

	/** 获取参数时间当天的结束时间 **/
	public static Date getEnd(Date date){
		if(date == null) {
            return null;
        }
		return DateUtil.endOfDay(date).toJdkDate();
	}


	/**
	 * 获取自定义查询时间
	 * today|0  -- 今天
	 * yesterday|0  -- 昨天
	 * near2now|7  -- 近xx天， 到今天
	 * near2yesterday|30   -- 近xx天， 到昨天
	 * customDate|2020-01-01,N  -- 自定义日期格式  N表示为空， 占位使用
	 * customDateTime|2020-01-01 23:00:00,2020-01-01 23:00:00 -- 自定义日期时间格式
	 *
	 * @return
	 */
	public static Date[] getQueryDateRange(String queryParamVal){

		//查询全部
		if(StringUtils.isEmpty(queryParamVal)){
			return new Date[]{null, null};
		}

		//根据 | 分割
		String[] valArray = queryParamVal.split("\\|");
		if(valArray.length != 2){ //参数有误
			throw new BizException("查询时间参数有误");
		}
		String dateType = valArray[0];  //时间类型
		String dateVal = valArray[1];  //搜索时间值

		Date nowDateTime = new Date();  //当前时间

		if("today".equals(dateType)){ //今天

			return new Date[]{getBegin(nowDateTime), getEnd(nowDateTime)};

		}else if("yesterday".equals(dateType)){  //昨天

			Date yesterdayDateTime = DateUtil.offsetDay(nowDateTime, -1).toJdkDate(); //昨天
			return new Date[]{getBegin(yesterdayDateTime), getEnd(yesterdayDateTime)};

		}else if("near2now".equals(dateType)){  //近xx天， xx天之前 ~ 当前时间

			Integer offsetDay = 1 - Integer.parseInt(dateVal);  //获取时间偏移量
			Date offsetDayDate = DateUtil.offsetDay(nowDateTime, offsetDay).toJdkDate();
			return new Date[]{getBegin(offsetDayDate), getEnd(nowDateTime)};

		}else if("near2yesterday".equals(dateType)){  //近xx天， xx天之前 ~ 昨天

			Date yesterdayDateTime = DateUtil.offsetDay(nowDateTime, -1).toJdkDate(); //昨天

			Integer offsetDay = 1 - Integer.parseInt(dateVal);  //获取时间偏移量
			Date offsetDayDate = DateUtil.offsetDay(yesterdayDateTime, offsetDay).toJdkDate();
			return new Date[]{getBegin(offsetDayDate), getEnd(yesterdayDateTime)};

		}else if("customDate".equals(dateType) || "customDateTime".equals(dateType)){ //自定义格式

			String[] timeArray = dateVal.split(","); //以逗号分割
			if(timeArray.length != 2) {
                throw new BizException("查询自定义时间参数有误");
            }

			String timeStr1 = "N".equalsIgnoreCase(timeArray[0]) ? null : timeArray[0] ;  //开始时间，
			String timeStr2 = "N".equalsIgnoreCase(timeArray[1]) ? null : timeArray[1];  //结束时间， N表示为空， 占位使用

			Date time1 = null;
			Date time2 = null;

			if(StringUtils.isNotEmpty(timeStr1)){
				time1 = DateUtil.parseDateTime("customDate".equals(dateType) ? (timeStr1 + " 00:00:00" ) : timeStr1);
			}
			if(StringUtils.isNotEmpty(timeStr2)){
				time2 = DateUtil.parseDateTime("customDate".equals(dateType) ? (timeStr2 + " 23:59:59" ) : timeStr2);
			}
			return new Date[]{time1, time2};

		}else{
			throw new BizException("查询时间参数有误");
		}
	}

}
