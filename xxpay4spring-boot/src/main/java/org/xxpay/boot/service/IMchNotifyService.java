package org.xxpay.boot.service;

import org.xxpay.dal.dao.model.MchNotify;

/**
 * @author: dingzhiwei
 * @date: 17/9/8
 * @description:
 */
public interface IMchNotifyService {

	MchNotify baseSelectMchNotify(String orderId);

}
