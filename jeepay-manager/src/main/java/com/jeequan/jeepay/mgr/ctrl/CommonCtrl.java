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
package com.jeequan.jeepay.mgr.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.core.ctrls.AbstractCtrl;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.model.BaseModel;
import com.jeequan.jeepay.core.model.security.JeeUserDetails;
import com.jeequan.jeepay.service.impl.MchInfoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

/*
* 定义通用CommonCtrl
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/6/8 17:09
*/
public abstract class CommonCtrl extends AbstractCtrl {

    @Autowired
    protected MchInfoService mchInfoService;

    /** 获取当前用户ID */
    protected JeeUserDetails getCurrentUser(){

        return (JeeUserDetails)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    /**
     * 获取当前用户登录IP
     * @return
     */
    protected String getIp() {
        return getClientIp();
    }

    /**
     * model 存入商户名称
     **/
    public void setMchName(List<? extends BaseModel> modeList) {

        if (modeList == null || modeList.isEmpty()) {
            return;
        }
        ArrayList<String> mchNoList = new ArrayList<>();
        for (BaseModel model : modeList) {
            JSONObject json = (JSONObject) JSONObject.toJSON(model);
            String mchNo = json.getString("mchNo");
            mchNoList.add(mchNo);
        }
        List<MchInfo> mchInfoList = mchInfoService.list(MchInfo.gw().select(MchInfo::getMchNo, MchInfo::getMchName).in(MchInfo::getMchNo, mchNoList));
        for (BaseModel model : modeList) {
            JSONObject json = (JSONObject) JSONObject.toJSON(model);
            String mchNo = json.getString("mchNo");

            if (StringUtils.isBlank(mchNo)) {
                continue;
            }

            for (MchInfo info : mchInfoList) {
                if (mchNo.equals(info.getMchNo())) {
                    model.addExt("mchName", info.getMchName());
                }
            }
        }
    }

}
