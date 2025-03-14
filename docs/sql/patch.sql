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

## -- ++++ [v1.6.0] ===> [v1.7.0] ++++

-- 订单页的支付方式筛选项添加权限并可分配： 避免API权限导致页面出现异常
insert into t_sys_entitlement values('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MGR', now(), now());
insert into t_sys_entitlement values('ENT_PAY_ORDER_SEARCH_PAY_WAY', '筛选项：支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());


-- 插入表结构，并插入默认数据（默认费率 0）
alter table `t_pay_order` add column `mch_fee_rate` decimal(20,6) NOT NULL COMMENT '商户手续费费率快照' after `amount`;
alter table `t_pay_order` add column `mch_fee_amount` BIGINT(20) NOT NULL COMMENT '商户手续费,单位分' after `mch_fee_rate`;
update `t_pay_order` set mch_fee_rate = 0;
update `t_pay_order` set mch_fee_amount = 0;

alter table `t_pay_order` drop column `division_flag`;
alter table `t_pay_order` drop column `division_time`;

alter table `t_pay_order` add column `division_mode` TINYINT(6) DEFAULT 0 COMMENT '订单分账模式：0-该笔订单不允许分账, 1-支付成功按配置自动完成分账, 2-商户手动分账(解冻商户金额)' after `refund_amount`;
alter table `t_pay_order` add column `division_state` TINYINT(6) DEFAULT 0 COMMENT '订单分账状态：0-未发生分账, 1-等待分账任务处理, 2-分账处理中, 3-分账任务已结束(不体现状态)' after `division_mode`;
alter table `t_pay_order` add column `division_last_time` DATETIME COMMENT '最新分账时间' after `division_state`;


-- 商户分账接收者账号组
DROP TABLE IF EXISTS `t_mch_division_receiver_group`;
CREATE TABLE `t_mch_division_receiver_group` (
                                                 `receiver_group_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '组ID',
                                                 `receiver_group_name` VARCHAR(64) NOT NULL COMMENT '组名称',
                                                 `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                                 `auto_division_flag` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '自动分账组（当订单分账模式为自动分账，改组将完成分账逻辑） 0-否 1-是',
                                                 `created_uid` BIGINT(20) NOT NULL COMMENT '创建者用户ID',
                                                 `created_by` VARCHAR(64) NOT NULL COMMENT '创建者姓名',
                                                 `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                                 `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                                 PRIMARY KEY (`receiver_group_id`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COMMENT='分账账号组';

-- 商户分账接收者账号绑定关系表
DROP TABLE IF EXISTS `t_mch_division_receiver`;
CREATE TABLE `t_mch_division_receiver` (
                                           `receiver_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '分账接收者ID',
                                           `receiver_alias` VARCHAR(64) NOT NULL COMMENT '接收者账号别名',
                                           `receiver_group_id` BIGINT(20) COMMENT '组ID（便于商户接口使用）',
                                           `receiver_group_name` VARCHAR(64) COMMENT '组名称',
                                           `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                           `isv_no` VARCHAR(64) COMMENT '服务商号',
                                           `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
                                           `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口代码',
                                           `acc_type` TINYINT(6) NOT NULL COMMENT '分账接收账号类型: 0-个人(对私) 1-商户(对公)',
                                           `acc_no` VARCHAR(50) NOT NULL COMMENT '分账接收账号',
                                           `acc_name` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '分账接收账号名称',
                                           `relation_type` VARCHAR(30) NOT NULL COMMENT '分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等',
                                           `relation_type_name` VARCHAR(30) NOT NULL COMMENT '当选择自定义时，需要录入该字段。 否则为对应的名称',
                                           `division_profit` DECIMAL(20,6) COMMENT '分账比例',
                                           `state` TINYINT(6) NOT NULL COMMENT '分账状态（本系统状态，并不调用上游关联关系）: 1-正常分账, 0-暂停分账',
                                           `channel_bind_result` TEXT COMMENT '上游绑定返回信息，一般用作查询账号异常时的记录',
                                           `channel_ext_info` TEXT COMMENT '渠道特殊信息',
                                           `bind_success_time` DATETIME DEFAULT NULL COMMENT '绑定成功时间',
                                           `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                           `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                           PRIMARY KEY (`receiver_id`)
) ENGINE=InnoDB AUTO_INCREMENT=800001 DEFAULT CHARSET=utf8mb4 COMMENT='商户分账接收者账号绑定关系表';

-- 分账记录表
DROP TABLE IF EXISTS `t_pay_order_division_record`;
CREATE TABLE `t_pay_order_division_record` (
                                               `record_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分账记录ID',
                                               `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
                                               `isv_no` VARCHAR(64) COMMENT '服务商号',
                                               `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
                                               `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
                                               `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
                                               `if_code` VARCHAR(20)  NOT NULL COMMENT '支付接口代码',
                                               `pay_order_id` VARCHAR(30) NOT NULL COMMENT '系统支付订单号',
                                               `pay_order_channel_order_no` VARCHAR(64) COMMENT '支付订单渠道支付订单号',
                                               `pay_order_amount` BIGINT(20) NOT NULL COMMENT '订单金额,单位分',
                                               `pay_order_division_amount` BIGINT(20) NOT NULL COMMENT '订单实际分账金额, 单位：分（订单金额 - 商户手续费 - 已退款金额）',
                                               `batch_order_id` VARCHAR(30) NOT NULL COMMENT '系统分账批次号',
                                               `channel_batch_order_id` VARCHAR(64) COMMENT '上游分账批次号',
                                               `state` TINYINT(6) NOT NULL COMMENT '状态: 0-待分账 1-分账成功, 2-分账失败',
                                               `channel_resp_result` TEXT COMMENT '上游返回数据包',
                                               `receiver_id` BIGINT(20) NOT NULL COMMENT '账号快照》 分账接收者ID',
                                               `receiver_group_id` BIGINT(20) COMMENT '账号快照》 组ID（便于商户接口使用）',
                                               `receiver_alias` VARCHAR(64) COMMENT '接收者账号别名',
                                               `acc_type` TINYINT(6) NOT NULL COMMENT '账号快照》 分账接收账号类型: 0-个人 1-商户',
                                               `acc_no` VARCHAR(50) NOT NULL COMMENT '账号快照》 分账接收账号',
                                               `acc_name` VARCHAR(30) NOT NULL DEFAULT '' COMMENT '账号快照》 分账接收账号名称',
                                               `relation_type` VARCHAR(30) NOT NULL COMMENT '账号快照》 分账关系类型（参考微信）， 如： SERVICE_PROVIDER 服务商等',
                                               `relation_type_name` VARCHAR(30) NOT NULL COMMENT '账号快照》 当选择自定义时，需要录入该字段。 否则为对应的名称',
                                               `division_profit` DECIMAL(20,6) NOT NULL COMMENT '账号快照》 配置的实际分账比例',
                                               `cal_division_amount` BIGINT(20) NOT NULL COMMENT '计算该接收方的分账金额,单位分',
                                               `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
                                               `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
                                               PRIMARY KEY (`record_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='分账记录表';

-- 权限表扩容
alter table `t_sys_entitlement` modify column `ent_id` VARCHAR(64) NOT NULL COMMENT '权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD';

-- 【商户系统】 分账管理
insert into t_sys_entitlement values('ENT_DIVISION', '分账管理', 'apartment', '', 'RouteView', 'ML', 0, 1,  'ROOT', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP', '账号组管理', 'team', '/divisionReceiverGroup', 'DivisionReceiverGroupPage', 'ML', 0, 1,  'ENT_DIVISION', '10', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_LIST', '页面：数据列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_GROUP_DELETE', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER_GROUP', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER', '收款账号管理', 'trademark', '/divisionReceiver', 'DivisionReceiverPage', 'ML', 0, 1,  'ENT_DIVISION', '20', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_LIST', '页面：数据列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_ADD', '按钮：新增收款账号', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_DELETE', '按钮：删除收款账号', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECEIVER_EDIT', '按钮：修改账号信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECEIVER', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECORD', '分账记录', 'unordered-list', '/divisionRecord', 'DivisionRecordPage', 'ML', 0, 1,  'ENT_DIVISION', '30', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECORD_LIST', '页面：数据列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECORD', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_DIVISION_RECORD_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECORD', '0', 'MCH', now(), now());

## -- ++++ ++++

## -- ++++ [v1.7.0] ===> [v1.8.0] ++++
-- 添加商户系统的退款功能权限配置项
insert into t_sys_entitlement values('ENT_PAY_ORDER_REFUND', '按钮：订单退款', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER', '0', 'MCH', now(), now());

## -- ++++ [v1.8.0] ===> [v1.9.0] ++++
-- 增加小新支付通道
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark, created_at, updated_at) VALUES ('xxpay', '小新支付', 1, 0, 1, null, null, '[{"name":"mchId","desc":"商户号","type":"text","verify":"required"},{"name":"key","desc":"私钥","type":"text","verify":"required","star":"1"},{"name":"payUrl","desc":"支付网关地址","type":"text","verify":"required"}]', '[{"wayCode": "ALI_BAR"}, {"wayCode": "ALI_JSAPI"}, {"wayCode": "WX_BAR"}, {"wayCode": "WX_JSAPI"}]', 'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/xxpay.png', '#2E4360', 1, null, '2021-09-20 15:21:04', '2021-09-30 14:55:32.907325');

## -- ++++ [v1.9.0] ===> [v1.10.0] ++++
alter table t_refund_order modify err_msg varchar(2048) null comment '渠道错误描述';

-- 增加角色权限字段长度
alter table `t_sys_role_ent_rela` MODIFY `ent_id` VARCHAR(64) NOT NULL COMMENT '权限ID' after `role_id`;

## -- ++++ [v1.10.0] ===> [v1.11.0] ++++
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_LITE', '支付宝小程序');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('PP_PC', 'PayPal支付');
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('pppay', 'PayPal支付', 1, 0, 1,
        NULL,
        NULL,
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"required","values":"1,0","titles":"沙箱环境, 生产环境"},{"name":"clientId","desc":"Client ID（客户端ID）","type":"text","verify":"required"},{"name":"secret","desc":"Secret（密钥）","type":"text","verify":"required","star":"1"},{"name":"refundWebhook","desc":"退款 Webhook id","type":"text","verify":"required"},{"name":"notifyWebhook","desc":"支付 Webhook id","type":"text","verify":"required"}]',
        '[{"wayCode": "PP_PC"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/paypal.png', '#005ea6', 1, 'PayPal官方通道');

## -- ++++ [v1.11.0] ===> [v1.12.0] ++++
-- 分账重试
insert into t_sys_entitlement values('ENT_DIVISION_RECORD_RESEND', '按钮：重试', 'no-icon', '', '', 'PB', 0, 1,  'ENT_DIVISION_RECORD', '0', 'MCH', now(), now());

## -- ++++ [v1.12.0] ===> [v1.13.0] ++++
DELETE FROM t_pay_interface_define WHERE if_code = 'wxpay';
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('wxpay', '微信支付官方', 1, 1, 2,
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required","star":"1"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"key", "desc":"APIv2密钥", "type": "textarea","verify":"required","star":"1"},{"name":"apiV3Key", "desc":"APIv3密钥（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"cert", "desc":"API证书(apiclient_cert.p12)", "type": "file","verify":""},{"name":"apiClientCert", "desc":"证书文件(apiclient_cert.pem) ", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(apiclient_key.pem)", "type": "file","verify":""}]',
        '[{"name":"subMchId","desc":"子商户ID","type":"text","verify":"required"},{"name":"subMchAppId","desc":"子账户appID(线上支付必填)","type":"text","verify":""}]',
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required","star":"1"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"key", "desc":"APIv2密钥", "type": "textarea","verify":"required","star":"1"},{"name":"apiV3Key", "desc":"APIv3密钥（V3接口必填）", "type": "textarea","verify":"","star":"1"},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":"","star":"1" },{"name":"cert", "desc":"API证书(apiclient_cert.p12)", "type": "file","verify":""},{"name":"apiClientCert", "desc":"证书文件(apiclient_cert.pem) ", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(apiclient_key.pem)", "type": "file","verify":""}]',
        '[{"wayCode": "WX_APP"}, {"wayCode": "WX_H5"}, {"wayCode": "WX_NATIVE"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "WX_BAR"}, {"wayCode": "WX_LITE"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/wxpay.png', '#04BE02', 1, '微信官方通道');

## -- ++++ [v1.13.0] ===> [v1.14.0] ++++
-- 日志请求参数、响应参数长度修改
alter table t_sys_log modify `opt_req_param` TEXT DEFAULT NULL COMMENT '操作请求参数';
alter table t_sys_log modify `opt_res_info` TEXT DEFAULT NULL COMMENT '操作响应结果';

## -- ++++ [v1.14.0] ===> [v1.15.0] ++++
-- 增加计全付支付通道
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('plspay', '计全付', 1, 0, 1,
        NULL,
        NULL,
        '[{"name":"signType","desc":"签名方式","type":"radio","verify":"required","values":"MD5,RSA2","titles":"MD5,RSA2"},{"name":"merchantNo","desc":"计全付商户号","type":"text","verify":"required"},{"name":"appId","desc":"应用ID","type":"text","verify":"required"},{"name":"appSecret","desc":"md5秘钥","type":"textarea","verify":"required","star":"1"},{"name":"rsa2AppPrivateKey","desc":"RSA2: 应用私钥","type":"textarea","verify":"required","star":"1"},{"name":"rsa2PayPublicKey","desc":"RSA2: 支付网关公钥","type":"textarea","verify":"required","star":"1"}]',
        '[{"wayCode": "ALI_APP"}, {"wayCode": "ALI_BAR"}, {"wayCode": "ALI_JSAPI"}, {"wayCode": "ALI_LITE"}, {"wayCode": "ALI_PC"}, {"wayCode": "ALI_QR"}, {"wayCode": "ALI_WAP"}, {"wayCode": "WX_APP"}, {"wayCode": "WX_BAR"}, {"wayCode": "WX_H5"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "WX_LITE"}, {"wayCode": "WX_NATIVE"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/plspay.svg', '#0CACFF', 1, '计全付');

## -- ++++ [v1.15.0] ===>

-- 增加银联支付
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_APP', '银联App支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_WAP', '银联手机网站支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_QR', '银联二维码(主扫)');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_BAR', '银联二维码(被扫)');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_B2B', '银联企业网银支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_PC', '银联网关支付');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('UP_JSAPI', '银联Js支付');


## -- ++++ [v2.1.0] ===>

-- 分账状态新增： 已受理
alter table t_pay_order_division_record modify column `state` TINYINT(6) NOT NULL COMMENT '状态: 0-待分账 1-分账成功（明确成功）, 2-分账失败（明确失败）, 3-分账已受理（上游受理）';

##
-- 支付方式新增支付宝订单码
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_OC', '支付宝订单码');
DELETE FROM t_pay_interface_define WHERE if_code = 'alipay';
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, config_page_type, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('alipay', '支付宝官方', 1, 1, 1,
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"pid","desc":"合作伙伴身份（PID）","type":"text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"privateKey", "desc":"应用私钥", "type": "textarea","verify":"required","star":"1"},{"name":"alipayPublicKey", "desc":"支付宝公钥(不使用证书时必填)", "type": "textarea","star":"1"},{"name":"signType","desc":"接口签名方式(推荐使用RSA2)","type":"radio","verify":"","values":"RSA,RSA2","titles":"RSA,RSA2","verify":"required"},{"name":"useCert","desc":"公钥证书","type":"radio","verify":"","values":"1,0","titles":"使用证书（请使用RSA2私钥）,不使用证书"},{"name":"appPublicCert","desc":"应用公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayPublicCert","desc":"支付宝公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayRootCert","desc":"支付宝根证书（.crt格式）","type":"file","verify":""}]',
        '[{"name":"appAuthToken", "desc":"子商户app_auth_token", "type": "text","readonly":"readonly"},{"name":"refreshToken", "desc":"子商户刷新token", "type": "hidden","readonly":"readonly"},{"name":"expireTimestamp", "desc":"authToken有效期（13位时间戳）", "type": "hidden","readonly":"readonly"}]',
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"privateKey", "desc":"应用私钥", "type": "textarea","verify":"required","star":"1"},{"name":"alipayPublicKey", "desc":"支付宝公钥(不使用证书时必填)", "type": "textarea","star":"1"},{"name":"signType","desc":"接口签名方式(推荐使用RSA2)","type":"radio","verify":"","values":"RSA,RSA2","titles":"RSA,RSA2","verify":"required"},{"name":"useCert","desc":"公钥证书","type":"radio","verify":"","values":"1,0","titles":"使用证书（请使用RSA2私钥）,不使用证书"},{"name":"appPublicCert","desc":"应用公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayPublicCert","desc":"支付宝公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayRootCert","desc":"支付宝根证书（.crt格式）","type":"file","verify":""}]',
        '[{"wayCode": "ALI_JSAPI"}, {"wayCode": "ALI_WAP"}, {"wayCode": "ALI_BAR"}, {"wayCode": "ALI_APP"}, {"wayCode": "ALI_PC"}, {"wayCode": "ALI_QR"}, {"wayCode": "ALI_OC"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/alipay.png', '#1779FF', 1, '支付宝官方通道');


## -- ++++ [v3.0.0] ===> [v3.1.0]  ====

-- 增加转账渠道响应数据字段
alter table t_transfer_order add column `channel_res_data` TEXT DEFAULT NULL COMMENT '渠道响应数据（如微信确认数据包）' after `channel_order_no`;


## -- ++++ [v3.1.0] ===> NEXT
