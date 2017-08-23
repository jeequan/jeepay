
/* 支付中心相关表结构 */

CREATE TABLE `t_mch_info` (
  `MchId` varchar(30) NOT NULL COMMENT '商户ID',
  `Name` varchar(30) NOT NULL COMMENT '名称',
  `Type` varchar(24) NOT NULL COMMENT '类型',
  `ReqKey` varchar(128) NOT NULL COMMENT '请求私钥',
  `ResKey` varchar(128) NOT NULL COMMENT '响应私钥',
  `State` tinyint(6) NOT NULL DEFAULT '1' COMMENT '商户状态,0-停止使用,1-使用中',
  `CreateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UpdateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`MchId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户信息表';

CREATE TABLE `t_pay_channel` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '渠道主键ID',
  `ChannelId` varchar(24) NOT NULL COMMENT '渠道ID',
  `ChannelName` varchar(30) NOT NULL COMMENT '渠道名称,如:alipay,wechat',
  `ChannelMchId` varchar(32) NOT NULL COMMENT '渠道商户ID',
  `MchId` varchar(30) NOT NULL COMMENT '商户ID',
  `State` tinyint(6) NOT NULL DEFAULT '1' COMMENT '渠道状态,0-停止使用,1-使用中',
  `Param` varchar(4096) NOT NULL COMMENT '配置参数,json字符串',
  `Remark` varchar(128) DEFAULT NULL COMMENT '备注',
  `CreateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UpdateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `IDX_MchId_MchOrderNo` (`ChannelId`, `MchId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付渠道表';

CREATE TABLE `t_pay_order` (
  `PayOrderId` varchar(30) NOT NULL COMMENT '支付订单号',
  `MchId` varchar(30) NOT NULL COMMENT '商户ID',
  `MchOrderNo` varchar(30) NOT NULL COMMENT '商户订单号',
  `ChannelId` varchar(24) NOT NULL COMMENT '渠道ID',
  `Amount` bigint(20) NOT NULL COMMENT '支付金额,单位分',
  `Currency` varchar(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
  `Status` tinyint(6) NOT NULL DEFAULT '0' COMMENT '支付状态,0-订单生成,1-支付中(目前未使用),2-支付成功,3-业务处理完成',
  `ClientIp` varchar(32) DEFAULT NULL COMMENT '客户端IP',
  `Device` varchar(64) DEFAULT NULL COMMENT '设备',
  `Subject` varchar(64) NOT NULL COMMENT '商品标题',
  `Body` varchar(256) NOT NULL COMMENT '商品描述信息',
  `Extra` varchar(512) DEFAULT NULL COMMENT '特定渠道发起时额外参数',
  `ChannelMchId` varchar(32) NOT NULL COMMENT '渠道商户ID',
  `ChannelOrderNo` varchar(64) DEFAULT NULL COMMENT '渠道订单号',
  `ErrCode` varchar(64) DEFAULT NULL COMMENT '渠道支付错误码',
  `ErrMsg` varchar(128) DEFAULT NULL COMMENT '渠道支付错误描述',
  `Param1` varchar(64) DEFAULT NULL COMMENT '扩展参数1',
  `Param2` varchar(64) DEFAULT NULL COMMENT '扩展参数2',
  `NotifyUrl` varchar(128) NOT NULL COMMENT '通知地址',
  `NotifyCount` tinyint(6) NOT NULL DEFAULT 0 COMMENT '通知次数',
  `LastNotifyTime` bigint(20) DEFAULT NULL COMMENT '最后一次通知时间',
  `ExpireTime` bigint(20) DEFAULT NULL COMMENT '订单失效时间',
  `PaySuccTime` bigint(20) DEFAULT NULL COMMENT '订单支付成功时间',
  `CreateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UpdateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`PayOrderId`),
  UNIQUE KEY `IDX_MchId_MchOrderNo` (`MchId`, MchOrderNo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';

CREATE TABLE `t_iap_receipt` (
  `PayOrderId` varchar(30) NOT NULL COMMENT '支付订单号',
  `MchId` varchar(30) NOT NULL COMMENT '商户ID',
  `TransactionId` varchar(24) NOT NULL COMMENT 'IAP业务号',
  `ReceiptData` TEXT NOT NULL COMMENT '渠道ID',
  `Status` tinyint(6) NOT NULL DEFAULT '0' COMMENT '处理状态:0-未处理,1-处理成功,-1-处理失败',
  `HandleCount` tinyint(6) NOT NULL DEFAULT 0 COMMENT '处理次数',
  `CreateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UpdateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`PayOrderId`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='苹果支付凭据表';

CREATE TABLE `t_trans_order` (
  `TransOrderId` varchar(30) NOT NULL COMMENT '转账订单号',
  `MchId` varchar(30) NOT NULL COMMENT '商户ID',
  `MchOrderNo` varchar(30) NOT NULL COMMENT '商户订单号',
  `ChannelId` varchar(24) NOT NULL COMMENT '渠道ID',
  `Amount` bigint(20) NOT NULL COMMENT '转账金额,单位分',
  `Currency` varchar(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
  `Status` tinyint(6) NOT NULL DEFAULT '0' COMMENT '支付状态:0-订单生成,1-转账中,2-转账成功,3-转账失败,4-业务处理完成,5-确认失败,6-不确认结果,7-等待手动处理,8-手动处理提现成功,9-手动处理提现失败',
  `ClientIp` varchar(32) DEFAULT NULL COMMENT '客户端IP',
  `Device` varchar(64) DEFAULT NULL COMMENT '设备',
  `RemarkInfo` varchar(256) DEFAULT NULL COMMENT '备注',
  `OpenId` varchar(32) DEFAULT NULL COMMENT '渠道用户标识,如微信openId',
  `CheckName` tinyint(6) NOT NULL DEFAULT '0' COMMENT '校验姓名:0-不校验真实姓名,1-强校验真实姓名,2-针对已实名认证的用户才校验真实姓名',
  `UserName` varchar(24) DEFAULT NULL COMMENT '用户姓名',
  `Extra` varchar(512) DEFAULT NULL COMMENT '特定渠道发起时额外参数',
  `ChannelMchId` varchar(32) NOT NULL COMMENT '渠道商户ID',
  `ChannelOrderNo` varchar(32) DEFAULT NULL COMMENT '渠道订单号',
  `ErrCode` varchar(64) DEFAULT NULL COMMENT '渠道支付错误码',
  `ErrMsg` varchar(128) DEFAULT NULL COMMENT '渠道支付错误描述',
  `Param1` varchar(64) DEFAULT NULL COMMENT '扩展参数1',
  `Param2` varchar(64) DEFAULT NULL COMMENT '扩展参数2',
  `NotifyUrl` varchar(128) NOT NULL COMMENT '通知地址',
  `NotifyCount` tinyint(6) NOT NULL DEFAULT 0 COMMENT '通知次数',
  `LastNotifyTime` bigint(20) DEFAULT NULL COMMENT '最后一次通知时间',
  `ExpireTime` bigint(20) DEFAULT NULL COMMENT '订单失效时间',
  `TransSuccTime` bigint(20) DEFAULT NULL COMMENT '订单转账成功时间',
  `CreateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UpdateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`TransOrderId`),
  UNIQUE KEY `IDX_MchId_MchOrderNo` (`MchId`, MchOrderNo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账订单表';


/* 支付演示商城相关表 */

CREATE TABLE `t_goods_order` (
  `GoodsOrderId` varchar(30) NOT NULL COMMENT '商品订单ID',
  `GoodsId` varchar(30) NOT NULL COMMENT '商品ID',
  `GoodsName` varchar(64) NOT NULL DEFAULT '' COMMENT '商品名称',
  `Amount` bigint(20) NOT NULL COMMENT '金额,单位分',
  `UserId` varchar(30) NOT NULL COMMENT '用户ID',
  `Status` tinyint(6) NOT NULL DEFAULT '0' COMMENT '订单状态,订单生成(0),支付成功(1),处理完成(2),处理失败(-1)',
  `PayOrderId` varchar(30) DEFAULT NULL COMMENT '支付订单号',
  `ChannelId` varchar(24) DEFAULT NULL COMMENT '渠道ID',
  `ChannelUserId` varchar(64) DEFAULT NULL COMMENT '支付渠道用户ID(微信openID或支付宝账号等第三方支付账号)',
  `CreateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `UpdateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`GoodsOrderId`),
  UNIQUE KEY `IDX_PayOrderId` (PayOrderId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品订单表';