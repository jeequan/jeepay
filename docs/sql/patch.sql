#####    增量发布SQL   #####

## -- ++++ [v1.1.0] ===> [v1.1.1] ++++
## -- 新增： 支付测试， 重发通知， 通知最大次数保存到数据库
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST', '支付测试', 'transaction', '/paytest', 'PayTestPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '20', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_PAYWAY_LIST', '页面：获取全部支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_DO', '按钮：支付测试', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_NOTIFY_RESEND', '按钮：重发通知', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY', '0', 'MGR', now(), now());
ALTER TABLE `t_mch_notify_record` ADD COLUMN `notify_count_limit` INT(11) NOT NULL DEFAULT '6' COMMENT '最大通知次数, 默认6次' after `notify_count`;
## -- ++++ ++++


## -- ++++ [v1.4.0] ++++
-- 支付接口定义表 新增支付参数配置页面是否为自定义
ALTER TABLE `t_pay_interface_define` ADD COLUMN `config_page_type` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '支付参数配置页面类型:1-JSON渲染,2-自定义' after `is_isv_mode`;

-- 优化支付接口定义初始化，新增是否为脱敏数据
DELETE FROM t_pay_interface_define WHERE if_code = 'alipay';
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('alipay', '支付宝官方', 1, 1, 1,
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"pid","desc":"合作伙伴身份（PID）","type":"text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"privateKey", "desc":"应用私钥", "type": "textarea","verify":"required","star":"1"},{"name":"alipayPublicKey", "desc":"支付宝公钥(不使用证书时必填)", "type": "textarea","star":"1"},{"name":"signType","desc":"接口签名方式(推荐使用RSA2)","type":"radio","verify":"","values":"RSA,RSA2","titles":"RSA,RSA2","verify":"required"},{"name":"useCert","desc":"公钥证书","type":"radio","verify":"","values":"1,0","titles":"使用证书（请使用RSA2私钥）,不使用证书"},{"name":"appPublicCert","desc":"应用公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayPublicCert","desc":"支付宝公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayRootCert","desc":"支付宝根证书（.crt格式）","type":"file","verify":""}]',
        '[{"name":"appAuthToken", "desc":"子商户app_auth_token", "type": "text","readonly":"readonly"},{"name":"refreshToken", "desc":"子商户刷新token", "type": "hidden","readonly":"readonly"},{"name":"expireTimestamp", "desc":"authToken有效期（13位时间戳）", "type": "hidden","readonly":"readonly"}]',
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"privateKey", "desc":"应用私钥", "type": "textarea","verify":"required","star":"1"},{"name":"alipayPublicKey", "desc":"支付宝公钥(不使用证书时必填)", "type": "textarea","star":"1"},{"name":"signType","desc":"接口签名方式(推荐使用RSA2)","type":"radio","verify":"","values":"RSA,RSA2","titles":"RSA,RSA2","verify":"required"},{"name":"useCert","desc":"公钥证书","type":"radio","verify":"","values":"1,0","titles":"使用证书（请使用RSA2私钥）,不使用证书"},{"name":"appPublicCert","desc":"应用公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayPublicCert","desc":"支付宝公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayRootCert","desc":"支付宝根证书（.crt格式）","type":"file","verify":""}]',
        '[{"wayCode": "ALI_JSAPI"}, {"wayCode": "ALI_WAP"}, {"wayCode": "ALI_BAR"}, {"wayCode": "ALI_APP"}, {"wayCode": "ALI_PC"}, {"wayCode": "ALI_QR"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/alipay.png', '#1779FF', 1, '支付宝官方通道');

DELETE FROM t_pay_interface_define WHERE if_code = 'wxpay';
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('wxpay', '微信支付官方', 1, 1, 1,
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required","star":"1"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"key", "desc":"API密钥", "type": "textarea","verify":"required","star":"1"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"apiV3Key", "desc":"API V3秘钥（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"cert", "desc":"API证书(.p12格式)", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(.pem格式)", "type": "file","verify":""}]',
        '[{"name":"subMchId","desc":"子商户ID","type":"text","verify":"required"},{"name":"subMchAppId","desc":"子账户appID(线上支付必填)","type":"text","verify":""}]',
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required","star":"1"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"key", "desc":"API密钥", "type": "textarea","verify":"required","star":"1"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"apiV3Key", "desc":"API V3秘钥（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":"","star":"1" },{"name":"cert", "desc":"API证书(.p12格式)", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(.pem格式)", "type": "file","verify":""}]',
        '[{"wayCode": "WX_APP"}, {"wayCode": "WX_H5"}, {"wayCode": "WX_NATIVE"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "WX_BAR"}, {"wayCode": "WX_LITE"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/wxpay.png', '#04BE02', 1, '微信官方通道');

DELETE FROM t_pay_interface_define WHERE if_code = 'ysfpay';
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('ysfpay', '云闪付官方', 0, 1, 1,
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"serProvId","desc":"服务商开发ID[serProvId]","type":"text","verify":"required"},{"name":"isvPrivateCertFile","desc":"服务商私钥文件（.pfx格式）","type":"file","verify":"required"},{"name":"isvPrivateCertPwd","desc":"服务商私钥文件密码","type":"text","verify":"required","star":"1"},{"name":"ysfpayPublicKey","desc":"云闪付开发公钥（证书管理页面可查询）","type":"textarea","verify":"required","star":"1"},{"name":"acqOrgCode","desc":"可用支付机构编号","type":"text","verify":"required"}]',
        '[{"name":"merId","desc":"商户编号","type":"text","verify":"required"}]',
        NULL,
        '[{"wayCode": "YSF_BAR"}, {"wayCode": "ALI_JSAPI"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "ALI_BAR"}, {"wayCode": "WX_BAR"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/ysfpay.png', 'red', 1, '云闪付官方通道');
## -- ++++ ++++


## -- ++++ [v1.5.1] ===> [v1.6.0] ++++
## -- 新增： 转账接口

-- 转账订单表
DROP TABLE IF EXISTS t_transfer_order;
CREATE TABLE `t_transfer_order` (
                                    `transfer_id` VARCHAR(32) NOT NULL COMMENT '转账订单号',
                                    `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                    `isv_no` VARCHAR(64) COMMENT '服务商号',
                                    `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
                                    `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
                                    `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
                                    `mch_order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
                                    `if_code` VARCHAR(20)  NOT NULL COMMENT '支付接口代码',
                                    `entry_type` VARCHAR(20) NOT NULL COMMENT '入账方式： WX_CASH-微信零钱; ALIPAY_CASH-支付宝转账; BANK_CARD-银行卡',
                                    `amount` BIGINT(20) NOT NULL COMMENT '转账金额,单位分',
                                    `currency` VARCHAR(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
                                    `account_no` VARCHAR(64) NOT NULL COMMENT '收款账号',
                                    `account_name` VARCHAR(64) COMMENT '收款人姓名',
                                    `bank_name` VARCHAR(32) COMMENT '收款人开户行名称',
                                    `transfer_desc` VARCHAR(128) NOT NULL DEFAULT '' COMMENT '转账备注信息',
                                    `client_ip` VARCHAR(32) DEFAULT NULL COMMENT '客户端IP',
                                    `state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '支付状态: 0-订单生成, 1-转账中, 2-转账成功, 3-转账失败, 4-订单关闭',
                                    `channel_extra` VARCHAR(512) DEFAULT NULL COMMENT '特定渠道发起额外参数',
                                    `channel_order_no` VARCHAR(64) DEFAULT NULL COMMENT '渠道订单号',
                                    `err_code` VARCHAR(128) DEFAULT NULL COMMENT '渠道支付错误码',
                                    `err_msg` VARCHAR(256) DEFAULT NULL COMMENT '渠道支付错误描述',
                                    `ext_param` VARCHAR(128) DEFAULT NULL COMMENT '商户扩展参数',
                                    `notify_url` VARCHAR(128) NOT NULL default '' COMMENT '异步通知地址',
                                    `success_time` DATETIME DEFAULT NULL COMMENT '转账成功时间',
                                    `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                    PRIMARY KEY (`transfer_id`),
                                    UNIQUE KEY `Uni_MchNo_MchOrderNo` (`mch_no`, `mch_order_no`),
                                    INDEX(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='转账订单表';

-- 菜单项
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '25', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER', '转账订单', 'property-safety', '/transfer', 'TransferOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_LIST', '页面：转账订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_TRANSFER_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_TRANSFER_ORDER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER', '转账', 'property-safety', '/doTransfer', 'MchTransferPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER_IF_CODE_LIST', '页面：获取全部代付通道', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER_CHANNEL_USER', '按钮：获取渠道用户', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_TRANSFER_DO', '按钮：发起转账', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_TRANSFER', '0', 'MCH', now(), now());

## -- ++++ ++++
