#####    增量发布SQL   #####

## -- ++++ [v1.1.0] ===> [v1.1.1] ++++
## -- 新增： 支付测试， 重发通知， 通知最大次数保存到数据库
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST', '支付测试', 'transaction', '/paytest', 'PayTestPage', 'ML', 0, 1,  'ENT_MCH_CENTER', '20', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_PAYWAY_LIST', '页面：获取全部支付方式', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_PAY_TEST_DO', '按钮：支付测试', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_TEST', '0', 'MCH', now(), now());
insert into t_sys_entitlement values('ENT_MCH_NOTIFY_RESEND', '按钮：重发通知', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY', '0', 'MGR', now(), now());
ALTER TABLE `t_mch_notify_record` ADD COLUMN `notify_count_limit` INT(11) NOT NULL DEFAULT '6' COMMENT '最大通知次数, 默认6次' after `notify_count`;
## -- ++++ ++++




