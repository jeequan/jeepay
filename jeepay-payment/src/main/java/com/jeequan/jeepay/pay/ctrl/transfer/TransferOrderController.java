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
package com.jeequan.jeepay.pay.ctrl.transfer;

import com.jeequan.jeepay.core.entity.MchApp;
import com.jeequan.jeepay.core.entity.MchInfo;
import com.jeequan.jeepay.core.entity.TransferOrder;
import com.jeequan.jeepay.core.exception.BizException;
import com.jeequan.jeepay.core.model.ApiRes;
import com.jeequan.jeepay.core.utils.SeqKit;
import com.jeequan.jeepay.core.utils.SpringBeansUtil;
import com.jeequan.jeepay.core.utils.StringKit;
import com.jeequan.jeepay.pay.channel.ITransferService;
import com.jeequan.jeepay.pay.ctrl.ApiController;
import com.jeequan.jeepay.pay.exception.ChannelException;
import com.jeequan.jeepay.pay.model.MchAppConfigContext;
import com.jeequan.jeepay.pay.rqrs.msg.ChannelRetMsg;
import com.jeequan.jeepay.pay.rqrs.transfer.TransferOrderRQ;
import com.jeequan.jeepay.pay.rqrs.transfer.TransferOrderRS;
import com.jeequan.jeepay.pay.service.ConfigContextQueryService;
import com.jeequan.jeepay.pay.service.ConfigContextService;
import com.jeequan.jeepay.pay.service.PayMchNotifyService;
import com.jeequan.jeepay.service.impl.PayInterfaceConfigService;
import com.jeequan.jeepay.service.impl.TransferOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
* 转账接口
*
* @author terrfly
* @site https://www.jeequan.com
* @date 2021/8/11 11:07
*/
@Slf4j
@RestController
public class TransferOrderController extends ApiController {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private TransferOrderService transferOrderService;
    @Autowired private PayInterfaceConfigService payInterfaceConfigService;
    @Autowired private PayMchNotifyService payMchNotifyService;

    /**
     * 转账
     * **/
    @PostMapping("/api/transferOrder")
    public ApiRes transferOrder(){

        TransferOrder transferOrder = null;

        //获取参数 & 验签
        TransferOrderRQ bizRQ = getRQByWithMchSign(TransferOrderRQ.class);

        try {


            String mchNo = bizRQ.getMchNo();
            String appId = bizRQ.getAppId();
            String ifCode = bizRQ.getIfCode();

            // 商户订单号是否重复
            if(transferOrderService.count(TransferOrder.gw().eq(TransferOrder::getMchNo, mchNo).eq(TransferOrder::getMchOrderNo, bizRQ.getMchOrderNo())) > 0){
                throw new BizException("商户订单["+bizRQ.getMchOrderNo()+"]已存在");
            }

            if(StringUtils.isNotEmpty(bizRQ.getNotifyUrl()) && !StringKit.isAvailableUrl(bizRQ.getNotifyUrl())){
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }

            // 商户配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(mchNo, appId);
            if(mchAppConfigContext == null){
                throw new BizException("获取商户应用信息失败");
            }

            MchInfo mchInfo = mchAppConfigContext.getMchInfo();
            MchApp mchApp = mchAppConfigContext.getMchApp();

            // 是否已正确配置
            if(!payInterfaceConfigService.mchAppHasAvailableIfCode(appId, ifCode)){
                throw new BizException("应用未开通此接口配置!");
            }


            ITransferService transferService = SpringBeansUtil.getBean(ifCode + "TransferService", ITransferService.class);
            if(transferService == null){
                throw new BizException("无此转账通道接口");
            }

            if(!transferService.isSupport(bizRQ.getEntryType())){
                throw new BizException("该接口不支持该入账方式");
            }

            transferOrder = genTransferOrder(bizRQ, mchInfo, mchApp, ifCode);

            //预先校验
            String errMsg = transferService.preCheck(bizRQ, transferOrder);
            if(StringUtils.isNotEmpty(errMsg)){
                throw new BizException(errMsg);
            }

            // 入库
            transferOrderService.save(transferOrder);

            // 调起上游接口
            ChannelRetMsg channelRetMsg = transferService.transfer(bizRQ, transferOrder, mchAppConfigContext);

            //处理退款单状态
            this.processChannelMsg(channelRetMsg, transferOrder);

            // 如果是系统异常，需要响应错误信息
            if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR){
                transferOrder.setErrMsg(channelRetMsg.getChannelErrMsg());
            }

            TransferOrderRS bizRes = TransferOrderRS.buildByRecord(transferOrder);
            return ApiRes.okWithSign(bizRes, mchApp.getAppSecret());

        }  catch (BizException e) {
            return ApiRes.customFail(e.getMessage());

        } catch (ChannelException e) {

            //处理上游返回数据
            this.processChannelMsg(e.getChannelRetMsg(), transferOrder);

            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR ){
                return ApiRes.customFail(e.getMessage());
            }

            TransferOrderRS bizRes = TransferOrderRS.buildByRecord(transferOrder);
            return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(bizRQ.getMchNo(), bizRQ.getAppId()).getAppSecret());

        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }
    }


    private TransferOrder genTransferOrder(TransferOrderRQ rq, MchInfo mchInfo, MchApp mchApp, String ifCode){

        TransferOrder transferOrder = new TransferOrder();
        transferOrder.setTransferId(SeqKit.genTransferId()); //生成转账订单号
        transferOrder.setMchNo(mchInfo.getMchNo()); //商户号
        transferOrder.setIsvNo(mchInfo.getIsvNo()); //服务商号
        transferOrder.setAppId(mchApp.getAppId()); //商户应用appId
        transferOrder.setMchName(mchInfo.getMchShortName()); //商户名称（简称）
        transferOrder.setMchType(mchInfo.getType()); //商户类型
        transferOrder.setMchOrderNo(rq.getMchOrderNo()); //商户订单号
        transferOrder.setIfCode(ifCode); //接口代码
        transferOrder.setEntryType(rq.getEntryType()); //入账方式
        transferOrder.setAmount(rq.getAmount()); //订单金额
        transferOrder.setCurrency(rq.getCurrency()); //币种
        transferOrder.setClientIp(StringUtils.defaultIfEmpty(rq.getClientIp(), getClientIp())); //客户端IP
        transferOrder.setState(TransferOrder.STATE_INIT); //订单状态, 默认订单生成状态
        transferOrder.setAccountNo(rq.getAccountNo()); //收款账号
        transferOrder.setAccountName(rq.getAccountName()); //账户姓名
        transferOrder.setBankName(rq.getBankName()); //银行名称
        transferOrder.setTransferDesc(rq.getTransferDesc()); //转账备注
        transferOrder.setExtParam(rq.getExtParam()); //商户扩展参数
        transferOrder.setNotifyUrl(rq.getNotifyUrl()); //异步通知地址
        transferOrder.setCreatedAt(new Date()); //订单创建时间
        return transferOrder;

    }


    /**
     * 处理返回的渠道信息，并更新订单状态
     *  TransferOrder将对部分信息进行 赋值操作。
     * **/
    private void processChannelMsg(ChannelRetMsg channelRetMsg, TransferOrder transferOrder){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        String transferId = transferOrder.getTransferId();

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(TransferOrder.STATE_SUCCESS, transferOrder, channelRetMsg);
            payMchNotifyService.transferOrderNotify(transferOrder);

            //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(TransferOrder.STATE_FAIL, transferOrder, channelRetMsg);
            payMchNotifyService.transferOrderNotify(transferOrder);

            // 上游处理中 || 未知 || 上游接口返回异常  订单为支付中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()

        ){
            this.updateInitOrderStateThrowException(TransferOrder.STATE_ING, transferOrder, channelRetMsg);

            // 系统异常：  订单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState()){

        }else{

            throw new BizException("ChannelState 返回异常！");
        }

    }


    /** 更新订单状态 --》 订单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(byte orderState, TransferOrder transferOrder, ChannelRetMsg channelRetMsg){

        transferOrder.setState(orderState);
        transferOrder.setChannelOrderNo(channelRetMsg.getChannelOrderId());
        transferOrder.setChannelResData(channelRetMsg.getChannelAttach());
        transferOrder.setErrCode(channelRetMsg.getChannelErrCode());
        transferOrder.setErrMsg(channelRetMsg.getChannelErrMsg());


        boolean isSuccess = transferOrderService.updateInit2Ing(transferOrder.getTransferId(), transferOrder.getChannelResData());
        if(!isSuccess){
            throw new BizException("更新转账订单异常!");
        }

        isSuccess = transferOrderService.updateIng2SuccessOrFail(transferOrder.getTransferId(), transferOrder.getState(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            throw new BizException("更新转账订单异常!");
        }
    }


}
