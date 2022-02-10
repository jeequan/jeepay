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
package com.jeequan.jeepay.service.impl;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jeequan.jeepay.core.constants.CS;
import com.jeequan.jeepay.core.entity.MchPayPassage;
import com.jeequan.jeepay.core.entity.PayInterfaceDefine;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.service.mapper.MchPayPassageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商户支付通道表 服务实现类
 * </p>
 *
 * @author [mybatis plus generator]
 * @since 2021-04-27
 */
@Service
public class MchPayPassageService extends ServiceImpl<MchPayPassageMapper, MchPayPassage> {

    @Autowired private PayInterfaceDefineService payInterfaceDefineService;

    /**
     * @Author: ZhuXiao
     * @Description: 根据支付方式查询可用的支付接口列表
     * @Date: 9:56 2021/5/10
    */
    public List<JSONObject> selectAvailablePayInterfaceList(String wayCode, String appId, Byte infoType, Byte mchType) {
        Map params = new HashMap();
        params.put("wayCode", wayCode);
        params.put("appId", appId);
        params.put("infoType", infoType);
        params.put("mchType", mchType);
        List<JSONObject> list = baseMapper.selectAvailablePayInterfaceList(params);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }

        // 添加通道状态
        for (JSONObject object : list) {
            MchPayPassage payPassage = baseMapper.selectOne(MchPayPassage.gw()
                    .eq(MchPayPassage::getAppId, appId)
                    .eq(MchPayPassage::getWayCode, wayCode)
                    .eq(MchPayPassage::getIfCode, object.getString("ifCode"))
            );
            if (payPassage != null) {
                object.put("passageId", payPassage.getId());
                if (payPassage.getRate() != null) {
                    object.put("rate", payPassage.getRate().multiply(new BigDecimal("100")));
                }
                object.put("state", payPassage.getState());
            }
            if(object.getBigDecimal("ifRate") != null) {
                object.put("ifRate", object.getBigDecimal("ifRate").multiply(new BigDecimal("100")));
            }
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateBatchSelf(List<MchPayPassage> mchPayPassageList, String mchNo) {
        for (MchPayPassage payPassage : mchPayPassageList) {
            if (payPassage.getState() == CS.NO && payPassage.getId() == null) {
                continue;
            }
            if (StrUtil.isNotBlank(mchNo)) { // 商户系统配置通道，添加商户号参数
                payPassage.setMchNo(mchNo);
            }
            if (payPassage.getRate() != null) {
                payPassage.setRate(payPassage.getRate().divide(new BigDecimal("100"), 6, BigDecimal.ROUND_HALF_UP));
            }
            if (!saveOrUpdate(payPassage)) {
                throw new BizException("操作失败");
            }
        }
    }


    /** 根据应用ID 和 支付方式， 查询出商户可用的支付接口 **/
    public MchPayPassage findMchPayPassage(String mchNo, String appId, String wayCode){

        List<MchPayPassage> list = list(MchPayPassage.gw()
                                    .eq(MchPayPassage::getMchNo, mchNo)
                                    .eq(MchPayPassage::getAppId, appId)
                                    .eq(MchPayPassage::getState, CS.YES)
                                    .eq(MchPayPassage::getWayCode, wayCode)
        );

        if (list.isEmpty()) {
            return null;
        }else { // 返回一个可用通道

            HashMap<String, MchPayPassage> mchPayPassageMap = new HashMap<>();

            for (MchPayPassage mchPayPassage:list) {
                mchPayPassageMap.put(mchPayPassage.getIfCode(), mchPayPassage);
            }
            // 查询ifCode所有接口
            PayInterfaceDefine interfaceDefine = payInterfaceDefineService
                    .getOne(PayInterfaceDefine.gw()
                            .select(PayInterfaceDefine::getIfCode, PayInterfaceDefine::getState)
                            .eq(PayInterfaceDefine::getState, CS.YES)
                            .in(PayInterfaceDefine::getIfCode, mchPayPassageMap.keySet()), false);

            if (interfaceDefine != null) {
                return mchPayPassageMap.get(interfaceDefine.getIfCode());
            }
        }
        return null;
    }


}
