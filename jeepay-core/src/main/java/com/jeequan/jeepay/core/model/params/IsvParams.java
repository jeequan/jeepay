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
package com.jeequan.jeepay.core.model.params;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;

/**
 * 抽象类 isv参数定义
 *
 * @author terrfly
 * @site https://www.jeequan.com
 * @date 2021/6/8 16:33
 * @modify ZhuXiao
 */
public abstract class IsvParams {

    public static IsvParams factory(String ifCode, String paramsStr){

        try {
            return (IsvParams)JSONObject.parseObject(paramsStr, Class.forName(IsvParams.class.getPackage().getName() +"."+ ifCode +"."+ StrUtil.upperFirst(ifCode) +"IsvParams"));
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     *  敏感数据脱敏
    */
    public abstract String deSenData();

}
