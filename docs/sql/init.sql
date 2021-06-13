#
#  * Copyright (c) 2021-2031, 河北计全科技有限公司 (https://www.jeequan.com & jeequan@126.com).
#  * <p>
#  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE 3.0;
#  * you may not use this file except in compliance with the License.
#  * You may obtain a copy of the License at
#  * <p>
#  * http://www.gnu.org/licenses/lgpl.html
#  * <p>
#  * Unless required by applicable law or agreed to in writing, software
#  * distributed under the License is distributed on an "AS IS" BASIS,
#  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  * See the License for the specific language governing permissions and
#  * limitations under the License.
#
#####  ↓↓↓↓↓↓↓↓↓↓  表结构DDL  ↓↓↓↓↓↓↓↓↓↓  #####

--   RBAC设计思路：  [用户] 1<->N [角色] 1<->N [权限]

-- 权限表
DROP TABLE IF EXISTS `t_sys_entitlement`;
CREATE TABLE `t_sys_entitlement` (
  `ent_id` VARCHAR(32) NOT NULL COMMENT '权限ID[ENT_功能模块_子模块_操作], eg: ENT_ROLE_LIST_ADD',
  `ent_name` VARCHAR(32) NOT NULL COMMENT '权限名称',
  `menu_icon` VARCHAR(32) COMMENT '菜单图标',
  `menu_uri` VARCHAR(128) COMMENT '菜单uri/路由地址',
  `component_name` VARCHAR(32) COMMENT '组件Name（前后端分离使用）',
  `ent_type` CHAR(2) NOT NULL COMMENT '权限类型 ML-左侧显示菜单, MO-其他菜单, PB-页面/按钮',
  `quick_jump` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '快速开始菜单 0-否, 1-是',
  `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态 0-停用, 1-启用',
  `pid` VARCHAR(32) NOT NULL COMMENT '父ID',
  `ent_sort` INT(11) NOT NULL DEFAULT 0 COMMENT '排序字段, 规则：正序',
  `system` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`ent_id`, `system`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统权限表';

-- 角色表
DROP TABLE IF EXISTS `t_sys_role`;
CREATE TABLE `t_sys_role` (
  `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID, ROLE_开头',
  `role_name` VARCHAR(32) NOT NULL COMMENT '角色名称',
  `system` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `belong_info_id` VARCHAR(64) NOT NULL DEFAULT '0' COMMENT '所属商户ID / 0(平台)',
  `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
  PRIMARY KEY (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 角色<->权限 关联表
DROP TABLE IF EXISTS `t_sys_role_ent_rela`;
CREATE TABLE `t_sys_role_ent_rela` (
  `role_id` VARCHAR(32) NOT NULL COMMENT '角色ID',
  `ent_id` VARCHAR(32) NOT NULL COMMENT '权限ID' ,
  PRIMARY KEY (`role_id`, `ent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色权限关联表';

-- 系统用户表
DROP TABLE IF EXISTS `t_sys_user`;
CREATE TABLE `t_sys_user` (
	`sys_user_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '系统用户ID',
    `login_username` VARCHAR(32) NOT NULL COMMENT '登录用户名',
	`realname` VARCHAR(32) NOT NULL COMMENT '真实姓名',
	`telphone` VARCHAR(32) NOT NULL COMMENT '手机号',
	`sex` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '性别 0-未知, 1-男, 2-女',
	`avatar_url` VARCHAR(128) COMMENT '头像地址',
    `user_no` VARCHAR(32) COMMENT '员工编号',
    `is_admin` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '是否超管（超管拥有全部权限） 0-否 1-是',
    `state` TINYINT(6) NOT NULL DEFAULT 0 COMMENT '状态 0-停用 1-启用',
    `system` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
    `belong_info_id` VARCHAR(64) NOT NULL DEFAULT '0' COMMENT '所属商户ID / 0(平台)',
	`created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
	PRIMARY KEY (`sys_user_id`),
    UNIQUE KEY(`system`,`login_username`),
    UNIQUE KEY(`system`,`telphone`),
    UNIQUE KEY(`system`, `user_no`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 系统用户认证表
DROP TABLE IF EXISTS `t_sys_user_auth`;
CREATE TABLE `t_sys_user_auth` (
	`auth_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
	`user_id` BIGINT(20) NOT NULL COMMENT 'user_id',
	`identity_type` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '登录类型  1-登录账号 2-手机号 3-邮箱  10-微信  11-QQ 12-支付宝 13-微博',
	`identifier` VARCHAR(128) NOT NULL COMMENT '认证标识 ( 用户名 | open_id )',
	`credential` VARCHAR(128) NOT NULL COMMENT '密码凭证',
	`salt` VARCHAR(128) NOT NULL COMMENT 'salt',
    `system` VARCHAR(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
	PRIMARY KEY (`auth_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='系统用户认证表';

-- 操作员<->角色 关联表
DROP TABLE IF EXISTS `t_sys_user_role_rela`;
CREATE TABLE `t_sys_user_role_rela` (
  `user_id` BIGINT(20) NOT NULL COMMENT '用户ID',
  `role_id`VARCHAR(32) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作员<->角色 关联表';


-- 系统配置表
DROP TABLE IF EXISTS `t_sys_config`;
CREATE TABLE `t_sys_config` (
    `config_key` VARCHAR(50) NOT NULL COMMENT '配置KEY',
    `config_name` VARCHAR(50) NOT NULL COMMENT '配置名称',
    `config_desc` VARCHAR(200) NOT NULL COMMENT '描述信息',
    `group_key` VARCHAR(50) NOT NULL COMMENT '分组key',
    `group_name` VARCHAR(50) NOT NULL COMMENT '分组名称',
    `config_val` TEXT NOT NULL COMMENT '配置内容项',
    `type` VARCHAR(20) NOT NULL DEFAULT 'text' COMMENT '类型: text-输入框, textarea-多行文本, uploadImg-上传图片, switch-开关',
    `sort_num` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
    PRIMARY KEY (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 系统操作日志表
DROP TABLE IF EXISTS `t_sys_log`;
CREATE TABLE `t_sys_log` (
  `sys_log_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `user_id` bigint(20) DEFAULT NULL COMMENT '系统用户ID',
  `user_name` varchar(32) DEFAULT NULL COMMENT '用户姓名',
  `user_ip` varchar(128) NOT NULL DEFAULT '' COMMENT '用户IP',
  `system` varchar(8) NOT NULL COMMENT '所属系统： MGR-运营平台, MCH-商户中心',
  `method_name` varchar(128) NOT NULL DEFAULT '' COMMENT '方法名',
  `method_remark` varchar(128) NOT NULL DEFAULT '' COMMENT '方法描述',
  `req_url` varchar(256) NOT NULL DEFAULT '' COMMENT '请求地址',
  `opt_req_param` varchar(2048) NOT NULL DEFAULT '' COMMENT '操作请求参数',
  `opt_res_info` varchar(2048) NOT NULL DEFAULT '' COMMENT '操作响应结果',
  `created_at` timestamp(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
  PRIMARY KEY (`sys_log_id`)
) ENGINE = INNODB DEFAULT CHARSET = utf8mb4 COMMENT = '系统操作日志表';

-- 1.商户信息表
DROP TABLE IF EXISTS t_mch_info;
CREATE TABLE `t_mch_info` (
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `mch_name` VARCHAR(64) NOT NULL COMMENT '商户名称',
        `mch_short_name` VARCHAR(32) NOT NULL COMMENT '商户简称',
        `type` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
        `isv_no` VARCHAR(64) COMMENT '服务商号',
        `contact_name` VARCHAR(32) COMMENT '联系人姓名',
        `contact_tel` VARCHAR(32) COMMENT '联系人手机号',
        `contact_email` VARCHAR(32) COMMENT '联系人邮箱',
        `private_key` VARCHAR(128) COMMENT '私钥',
        `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '商户状态: 0-停用, 1-正常',
        `remark` VARCHAR(128) COMMENT '商户备注',
        `init_user_id` BIGINT(20) DEFAULT NULL COMMENT '初始用户ID（创建商户时，允许商户登录的用户）',
        `created_uid` BIGINT(20) COMMENT '创建者用户ID',
        `created_by` VARCHAR(64) COMMENT '创建者姓名',
        `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
        `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
        PRIMARY KEY (`mch_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户信息表';

-- 2.服务商信息表
DROP TABLE IF EXISTS t_isv_info;
CREATE TABLE `t_isv_info` (
        `isv_no` VARCHAR(64) NOT NULL COMMENT '服务商号',
        `isv_name` VARCHAR(64) NOT NULL COMMENT '服务商名称',
        `isv_short_name` VARCHAR(32) NOT NULL COMMENT '服务商简称',
        `contact_name` VARCHAR(32) COMMENT '联系人姓名',
        `contact_tel` VARCHAR(32) COMMENT '联系人手机号',
        `contact_email` VARCHAR(32) COMMENT '联系人邮箱',
        `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-正常',
        `remark` VARCHAR(128) DEFAULT NULL COMMENT '备注',
        `created_uid` BIGINT(20) COMMENT '创建者用户ID',
        `created_by` VARCHAR(64) COMMENT '创建者姓名',
        `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
        `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
        PRIMARY KEY (`isv_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务商信息表';

-- 3.支付方式表  pay_way
DROP TABLE IF EXISTS t_pay_way;
CREATE TABLE `t_pay_way` (
        `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式代码  例如： wxpay_jsapi',
        `way_name` VARCHAR(20) NOT NULL COMMENT '支付方式名称',
        `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
        `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
        PRIMARY KEY (`way_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付方式表';

-- 4.支付接口定义表
DROP TABLE IF EXISTS t_pay_interface_define;
CREATE TABLE `t_pay_interface_define` (
          `if_code` VARCHAR(20) NOT NULL COMMENT '接口代码 全小写  wxpay alipay ',
          `if_name` VARCHAR(20) NOT NULL COMMENT '接口名称',
          `is_mch_mode` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '是否支持普通商户模式: 0-不支持, 1-支持',
          `is_isv_mode` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '是否支持服务商子商户模式: 0-不支持, 1-支持',
          `isv_params` VARCHAR(4096) DEFAULT NULL COMMENT 'ISV接口配置定义描述,json字符串',
          `isvsub_mch_params` VARCHAR(4096) DEFAULT NULL COMMENT '特约商户接口配置定义描述,json字符串',
          `normal_mch_params` VARCHAR(4096) DEFAULT NULL COMMENT '普通商户接口配置定义描述,json字符串',
          `way_codes` JSON NOT NULL COMMENT '支持的支付方式 ["wxpay_jsapi", "wxpay_bar"]',
          `icon` VARCHAR(256) DEFAULT NULL COMMENT '页面展示：卡片-图标',
          `bg_color` VARCHAR(20) DEFAULT NULL COMMENT '页面展示：卡片-背景色',
          `state` TINYINT(6) NOT NULL DEFAULT 1 COMMENT '状态: 0-停用, 1-启用',
          `remark` VARCHAR(128) DEFAULT NULL COMMENT '备注',
          `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
          `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
          PRIMARY KEY (`if_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付接口定义表';

-- 5.支付接口配置参数表
DROP TABLE IF EXISTS t_pay_interface_config;
CREATE TABLE `t_pay_interface_config` (
          `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
          `info_type` TINYINT(6) NOT NULL COMMENT '账号类型:1-服务商 2-商户',
          `info_id` VARCHAR(64) NOT NULL COMMENT '服务商或商户No',
          `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口代码',
          `if_params` VARCHAR(4096) NOT NULL COMMENT '接口配置参数,json字符串',
          `if_rate` DECIMAL(20,6) DEFAULT NULL COMMENT '支付接口费率',
          `state` TINYINT(6) NOT NULL default 1 COMMENT '状态: 0-停用, 1-启用',
          `remark` VARCHAR(128) DEFAULT NULL COMMENT '备注',
          `created_uid` BIGINT(20) COMMENT '创建者用户ID',
          `created_by` VARCHAR(64) COMMENT '创建者姓名',
          `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
          `updated_uid` BIGINT(20) COMMENT '更新者用户ID',
          `updated_by` VARCHAR(64) COMMENT '更新者姓名',
          `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
          PRIMARY KEY (`id`),
          UNIQUE KEY `Uni_InfoType_InfoId_IfCode` (`info_type`, `info_id`, `if_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付接口配置参数表';


-- 6.商户支付通道表 (允许商户  支付方式 对应多个支付接口的配置)
DROP TABLE IF EXISTS t_mch_pay_passage;
CREATE TABLE `t_mch_pay_passage` (
         `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT 'ID',
         `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
         `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口',
         `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式',
         `rate` DECIMAL(20,6) NOT NULL COMMENT '支付方式费率',
         `risk_config` JSON DEFAULT NULL COMMENT '风控数据',
         `state` TINYINT(6) NOT NULL COMMENT '状态: 0-停用, 1-启用',
         `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
         `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
         PRIMARY KEY (`id`),
         UNIQUE KEY `Uni_MchNo_WayCode` (`mch_no`,`if_code`, `way_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户支付通道表';


-- 轮询表
-- mch_no, way_code, 轮询策略。


-- 7.支付订单表
DROP TABLE IF EXISTS t_pay_order;
CREATE TABLE `t_pay_order` (
        `pay_order_id` VARCHAR(30) NOT NULL COMMENT '支付订单号',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `isv_no` VARCHAR(64) DEFAULT NULL COMMENT '服务商号',
        `mch_name` VARCHAR(30) NOT NULL COMMENT '商户名称',
        `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
        `mch_order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
        `if_code` VARCHAR(20) COMMENT '支付接口代码',
        `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式代码',
        `amount` BIGINT(20) NOT NULL COMMENT '支付金额,单位分',
        `currency` VARCHAR(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
        `state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '支付状态: 0-订单生成, 1-支付中, 2-支付成功, 3-支付失败, 4-已撤销, 5-已退款, 6-订单关闭',
        `notify_state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '向下游回调状态, 0-未发送,  1-已发送',
        `client_ip` VARCHAR(32) DEFAULT NULL COMMENT '客户端IP',
        `subject` VARCHAR(64) NOT NULL COMMENT '商品标题',
        `body` VARCHAR(256) NOT NULL COMMENT '商品描述信息',
        `channel_extra` VARCHAR(512) DEFAULT NULL COMMENT '特定渠道发起额外参数',
        `channel_user` VARCHAR(64) DEFAULT NULL COMMENT '渠道用户标识,如微信openId,支付宝账号',
        `channel_order_no` VARCHAR(64) DEFAULT NULL COMMENT '渠道订单号',
        `refund_times` INT NOT NULL DEFAULT 0 COMMENT '退款次数',
        `refund_amount` BIGINT(20) NOT NULL DEFAULT 0 COMMENT '退款总金额,单位分',
        `division_flag` TINYINT(6) DEFAULT 0 COMMENT '订单分账标志：0-否  1-是',
        `division_time` DATETIME COMMENT '预计分账发起时间',
        `err_code` VARCHAR(64) DEFAULT NULL COMMENT '渠道支付错误码',
        `err_msg` VARCHAR(128) DEFAULT NULL COMMENT '渠道支付错误描述',
        `ext_param` VARCHAR(128) DEFAULT NULL COMMENT '商户扩展参数',
        `notify_url` VARCHAR(128) NOT NULL default '' COMMENT '异步通知地址',
        `return_url` VARCHAR(128) DEFAULT '' COMMENT '页面跳转地址',
        `expired_time` DATETIME DEFAULT NULL COMMENT '订单失效时间',
        `success_time` DATETIME DEFAULT NULL COMMENT '订单支付成功时间',
        `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
        `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
        PRIMARY KEY (`pay_order_id`),
        UNIQUE KEY `Uni_MchNo_MchOrderNo` (`mch_no`, `mch_order_no`),
        INDEX(`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付订单表';


-- 8.商户通知记录表
DROP TABLE IF EXISTS t_mch_notify_record;
CREATE TABLE `t_mch_notify_record` (
        `notify_id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '商户通知记录ID',
        `order_id` VARCHAR(64) NOT NULL COMMENT '订单ID',
        `order_type` TINYINT(6) NOT NULL COMMENT '订单类型:1-支付,2-退款',
        `mch_order_no` VARCHAR(64) NOT NULL COMMENT '商户订单号',
        `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
        `isv_no` VARCHAR(64) COMMENT '服务商号',
        `notify_url` TEXT NOT NULL COMMENT '通知地址',
        `res_result` TEXT DEFAULT NULL COMMENT '通知响应结果',
        `notify_count` INT(11) NOT NULL DEFAULT '0' COMMENT '通知次数',
        `state` TINYINT(6) NOT NULL DEFAULT '1' COMMENT '通知状态,1-通知中,2-通知成功,3-通知失败',
        `last_notify_time` DATETIME DEFAULT NULL COMMENT '最后一次通知时间',
        `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
        `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
        PRIMARY KEY (`notify_id`),
        UNIQUE KEY `Uni_OrderId_Type` (`order_id`, `order_type`)
) ENGINE=InnoDB AUTO_INCREMENT=1001 DEFAULT CHARSET=utf8mb4 COMMENT='商户通知记录表';


-- 9.订单接口数据快照（加密存储）
DROP TABLE IF EXISTS `t_order_snapshot`;
CREATE TABLE `t_order_snapshot` (
        `order_id` VARCHAR(64) NOT NULL COMMENT '订单ID',
        `order_type` TINYINT(6) NOT NULL COMMENT '订单类型: 1-支付, 2-退款',
        `mch_req_data` TEXT DEFAULT NULL COMMENT '下游请求数据',
        `mch_req_time` DATETIME DEFAULT NULL COMMENT '下游请求时间',
        `mch_resp_data` TEXT DEFAULT NULL COMMENT '向下游响应数据',
        `mch_resp_time` DATETIME DEFAULT NULL COMMENT '向下游响应时间',
        `channel_req_data` TEXT DEFAULT NULL COMMENT '向上游请求数据',
        `channel_req_time` DATETIME DEFAULT NULL COMMENT '向上游请求时间',
        `channel_resp_data` TEXT DEFAULT NULL COMMENT '上游响应数据',
        `channel_resp_time` DATETIME DEFAULT NULL COMMENT '上游响应时间',
        `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
        `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
        PRIMARY KEY (`order_id`, `order_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单接口数据快照';


-- 10.退款订单表
DROP TABLE IF EXISTS t_refund_order;
CREATE TABLE `t_refund_order` (
          `refund_order_id` VARCHAR(30) NOT NULL COMMENT '退款订单号',
          `pay_order_id` VARCHAR(30) NOT NULL COMMENT '支付订单号',
          `channel_pay_order_no` VARCHAR(64) DEFAULT NULL COMMENT '渠道支付单号',
          `mch_no` VARCHAR(64) NOT NULL COMMENT '商户号',
          `mch_type` TINYINT(6) NOT NULL COMMENT '类型: 1-普通商户, 2-特约商户(服务商模式)',
          `mch_refund_no` VARCHAR(64) NOT NULL COMMENT '商户退款单号',
          `isv_no` VARCHAR(64) DEFAULT NULL COMMENT '服务商号',
          `way_code` VARCHAR(20) NOT NULL COMMENT '支付方式代码',
          `if_code` VARCHAR(20) NOT NULL COMMENT '支付接口代码',
          `pay_amount` BIGINT(20) NOT NULL COMMENT '支付金额,单位分',
          `refund_amount` BIGINT(20) NOT NULL COMMENT '退款金额,单位分',
          `currency` VARCHAR(3) NOT NULL DEFAULT 'cny' COMMENT '三位货币代码,人民币:cny',
          `state` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败',
          `result` TINYINT(6) NOT NULL DEFAULT '0' COMMENT '退款结果:0-不确认结果,1-等待手动处理,2-确认成功,3-确认失败',
          `client_ip` VARCHAR(32) DEFAULT NULL COMMENT '客户端IP',
          `remark` VARCHAR(256) DEFAULT NULL COMMENT '备注',
          `channel_order_no` VARCHAR(32) DEFAULT NULL COMMENT '渠道订单号',
          `channel_err_code` VARCHAR(128) DEFAULT NULL COMMENT '渠道错误码',
          `channel_err_msg` VARCHAR(128) DEFAULT NULL COMMENT '渠道错误描述',
          `channel_extra` VARCHAR(512) DEFAULT NULL COMMENT '特定渠道发起时额外参数',
          `notify_url` VARCHAR(128) DEFAULT NULL COMMENT '通知地址',
          `ext_param` VARCHAR(64) DEFAULT NULL COMMENT '扩展参数',
          `success_time` DATETIME DEFAULT NULL COMMENT '订单退款成功时间',
          `created_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) COMMENT '创建时间',
          `updated_at` TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) COMMENT '更新时间',
          PRIMARY KEY (`refund_order_id`),
          UNIQUE KEY `Uni_MchNo_MchRefundNo` (`mch_no`, `mch_refund_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退款订单表';


#####  ↑↑↑↑↑↑↑↑↑↑  表结构DDL  ↑↑↑↑↑↑↑↑↑↑  #####

#####  ↓↓↓↓↓↓↓↓↓↓  初始化DML  ↓↓↓↓↓↓↓↓↓↓  #####

-- 权限表数据 （ 不包含根目录 ）
insert into t_sys_entitlement values('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1,  'ROOT', '-1', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1,  'ENT_COMMONS', '-1', 'MGR', now(), now());

insert into t_sys_entitlement values('ENT_C_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1,  'ROOT', '1', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_PAY_AMOUNT_WEEK', '主页周支付统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_NUMBER_COUNT', '主页数量总统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_PAY_COUNT', '主页交易统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_C_MAIN_PAY_TYPE_COUNT', '主页交易方式统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_C_MAIN', '0', 'MGR', now(), now());

-- 商户管理
insert into t_sys_entitlement values('ENT_MCH_INFO', '商户管理', 'shop', '', 'RouteView', 'ML', 0, 1,  'ROOT', '30', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_INFO_LIST', '商户列表', 'profile', '/mch', 'MchListPage', 'ML', 0, 1,  'ENT_MCH_INFO', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_LIST', '页面：商户列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_LIST', '商户支付参数配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_ADD', '商户支付参数配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_VIEW', '商户支付参数配置详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_LIST', '商户支付通道配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_CONFIG', '商户支付通道配置入口', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_ADD', '商户支付通道配置保存', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MGR', now(), now());

-- 服务商管理
insert into t_sys_entitlement values('ENT_ISV_INFO', '服务商管理', 'block', '', 'RouteView', 'ML', 0, 1,  'ROOT', '40', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_ISV_INFO_LIST', '服务商列表', 'profile', '/isv', 'IsvListPage', 'ML', 0, 1,  'ENT_ISV_INFO', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_LIST', '页面：服务商列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_EDIT', '按钮：编辑', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_INFO_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_PAY_CONFIG_LIST', '服务商支付参数配置列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_INFO_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_PAY_CONFIG_ADD', '服务商支付参数配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_PAY_CONFIG_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ISV_PAY_CONFIG_VIEW', '服务商支付参数配置详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_ISV_PAY_CONFIG_LIST', '0', 'MGR', now(), now());

-- 订单管理
insert into t_sys_entitlement values('ENT_ORDER', '订单管理', 'transaction', '', 'RouteView', 'ML', 0, 1,  'ROOT', '50', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PAY_ORDER_LIST', '支付订单', 'account-book', '/pay', 'PayOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_ORDER_LIST', '页面：订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER_LIST', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_REFUND_ORDER_LIST', '退款订单', 'exception', '/refund', 'RefundOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '20', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_LIST', '页面：退款订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER_LIST', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_NOTIFY_LIST', '商户通知', 'notification', '/notify', 'MchNotifyListPage', 'ML', 0, 1,  'ENT_ORDER', '30', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_NOTIFY_LIST', '页面：商户通知列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY_LIST', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_NOTIFY_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_NOTIFY_LIST', '0', 'MGR', now(), now());

-- 支付配置菜单
insert into t_sys_entitlement values('ENT_PC', '支付配置', 'file-done', '', 'RouteView', 'ML', 0, 1,  'ROOT', '60', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PC_IF_DEFINE', '支付接口', 'interaction', '/ifdefines', 'IfDefinePage', 'ML', 0, 1,  'ENT_PC', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_LIST', '页面：支付接口定义列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_IF_DEFINE_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_IF_DEFINE', '0', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_PC_WAY', '支付方式', 'appstore', '/payways', 'PayWayPage', 'ML', 0, 1,  'ENT_PC', '20', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_LIST', '页面：支付方式列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_ADD', '按钮：新增', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_EDIT', '按钮：修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_PC_WAY_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PC_WAY', '0', 'MGR', now(), now());

-- 系统管理
insert into t_sys_entitlement values('ENT_SYS_CONFIG', '系统管理', 'setting', '', 'RouteView', 'ML', 0, 1,  'ROOT', '200', 'MGR', now(), now());
    insert into t_sys_entitlement values('ENT_UR', '用户角色管理', 'team', '', 'RouteView', 'ML', 0, 1,  'ENT_SYS_CONFIG', '10', 'MGR', now(), now());
        insert into t_sys_entitlement values('ENT_UR_USER', '操作员管理', 'contacts', '/users', 'SysUserPage', 'ML', 0, 1,  'ENT_UR', '10', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_LIST', '页面：操作员列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_SEARCH', '按钮：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_ADD', '按钮：添加操作员', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_VIEW', '按钮： 详情', '', 'no-icon', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_UPD_ROLE', '按钮： 角色分配', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MGR', now(), now());

        insert into t_sys_entitlement values('ENT_UR_ROLE', '角色管理', 'user', '/roles', 'RolePage', 'ML', 0, 1,  'ENT_UR', '20', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_LIST', '页面：角色列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ADD', '按钮：添加角色', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DIST', '按钮： 分配权限', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DEL', '按钮： 删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MGR', now(), now());

        insert into t_sys_entitlement values('ENT_UR_ROLE_ENT', '权限管理', 'apartment', '/ents', 'EntPage', 'ML', 0, 1,  'ENT_UR', '30', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ENT_LIST', '页面： 权限列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE_ENT', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ENT_EDIT', '按钮： 权限变更', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE_ENT', '0', 'MGR', now(), now());

    insert into t_sys_entitlement values('ENT_SYS_CONFIG_INFO', '系统配置', 'setting', '/config', 'SysConfigPage', 'ML', 0, 1,  'ENT_SYS_CONFIG', '15', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_SYS_CONFIG_EDIT', '按钮： 修改', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_CONFIG_INFO', '0', 'MGR', now(), now());

    insert into t_sys_entitlement values('ENT_SYS_LOG_LIST', '系统日志', 'file-text', '/log', 'SysLogPage', 'ML', 0, 1,  'ENT_SYS_CONFIG', '20', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_LOG_LIST', '页面：系统日志列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_LOG_LIST', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_SYS_LOG_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_LOG_LIST', '0', 'MGR', now(), now());
            insert into t_sys_entitlement values('ENT_SYS_LOG_DEL', '按钮：删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_SYS_LOG_LIST', '0', 'MGR', now(), now());


-- 【商户系统】 主页
insert into t_sys_entitlement values('ENT_COMMONS', '系统通用菜单', 'no-icon', '', 'RouteView', 'MO', 0, 1,  'ROOT', '-1', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_C_USERINFO', '个人中心', 'no-icon', '/current/userinfo', 'CurrentUserInfo', 'MO', 0, 1,  'ENT_COMMONS', '-1', 'MCH', now(), now());

insert into t_sys_entitlement values('ENT_MCH_MAIN', '主页', 'home', '/main', 'MainPage', 'ML', 0, 1,  'ROOT', '1', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_PAY_AMOUNT_WEEK', '主页周支付统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_NUMBER_COUNT', '主页数量总统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_PAY_COUNT', '主页交易统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_PAY_TYPE_COUNT', '主页交易方式统计', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_MAIN_USER_INFO', '主页用户信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_MAIN', '0', 'MCH', now(), now());

-- 【商户系统】 商户中心
-- insert into t_sys_entitlement values('ENT_MCH_CENTER', '商户中心', 'team', '', 'RouteView', 'ML', 0, 'ROOT', '10', 'MCH', now(), now());
--     insert into t_sys_entitlement values('ENT_MCH_INFO', '商户信息', 'user', '/mch', 'MchInfoPage', 'ML', 0, 'ENT_MCH_CENTER', '10', 'MCH', now(), now());
--         insert into t_sys_entitlement values('ENT_MCH_INFO_EDIT', '按钮：修改商户信息', 'no-icon', '', '', 'PB', 0, 'ENT_MCH_INFO', '0', 'MCH', now(), now());

-- 【商户系统】 订单管理
insert into t_sys_entitlement values('ENT_ORDER', '订单中心', 'transaction', '', 'RouteView', 'ML', 0, 1,  'ROOT', '20', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_PAY_ORDER_LIST', '订单管理', 'account-book', '/pay', 'PayOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '10', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_ORDER_LIST', '页面：订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_PAY_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_PAY_ORDER_LIST', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_REFUND_ORDER_LIST', '退款记录', 'exception', '/refund', 'RefundOrderListPage', 'ML', 0, 1,  'ENT_ORDER', '20', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_LIST', '页面：退款订单列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_REFUND_ORDER_VIEW', '按钮：详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_REFUND_ORDER_LIST', '0', 'MCH', now(), now());

-- 【商户系统】 支付配置
insert into t_sys_entitlement values('ENT_MCH_PC', '支付配置', 'file-done', '', 'RouteView', 'ML', 0, 1,  'ROOT', '30', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_LIST', '支付参数', 'interaction', '/pay/config', 'PayConfigPage', 'ML', 0, 1,  'ENT_MCH_PC', '10', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_ADD', '商户支付参数配置', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_CONFIG_VIEW', '商户支付参数配置详情', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_CONFIG_LIST', '0', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_LIST', '支付通道', 'appstore', '/pay/passage', 'PayPassagePage', 'ML', 0, 1,  'ENT_MCH_PC', '20', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_CONFIG', '商户支付通道配置入口', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_MCH_PAY_PASSAGE_ADD', '商户支付通道配置保存', 'no-icon', '', '', 'PB', 0, 1,  'ENT_MCH_PAY_PASSAGE_LIST', '0', 'MCH', now(), now());


-- 【商户系统】 系统管理
insert into t_sys_entitlement values('ENT_SYS_CONFIG', '系统管理', 'setting', '', 'RouteView', 'ML', 0, 1,  'ROOT', '200', 'MCH', now(), now());
    insert into t_sys_entitlement values('ENT_UR', '用户角色管理', 'team', '', 'RouteView', 'ML', 0, 1,  'ENT_SYS_CONFIG', '10', 'MCH', now(), now());
        insert into t_sys_entitlement values('ENT_UR_USER', '操作员管理', 'contacts', '/users', 'SysUserPage', 'ML', 0, 1,  'ENT_UR', '10', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_LIST', '页面：操作员列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_SEARCH', '按钮：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_ADD', '按钮：添加操作员', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_VIEW', '按钮： 详情', '', 'no-icon', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_EDIT', '按钮： 修改基本信息', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_USER_UPD_ROLE', '按钮： 角色分配', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_USER', '0', 'MCH', now(), now());

        insert into t_sys_entitlement values('ENT_UR_ROLE', '角色管理', 'user', '/roles', 'RolePage', 'ML', 0, 1,  'ENT_UR', '20', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_LIST', '页面：角色列表', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_SEARCH', '页面：搜索', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_ADD', '按钮：添加角色', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DIST', '按钮： 分配权限', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_EDIT', '按钮： 修改名称', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());
            insert into t_sys_entitlement values('ENT_UR_ROLE_DEL', '按钮： 删除', 'no-icon', '', '', 'PB', 0, 1,  'ENT_UR_ROLE', '0', 'MCH', now(), now());

-- 默认角色
insert into t_sys_role values ('ROLE_ADMIN', '系统管理员', 'MGR', '0', '2021-05-01');
insert into t_sys_role values ('ROLE_OP', '普通操作员', 'MGR', '0', '2021-05-01');
-- 角色权限关联， [超管]用户 拥有所有权限
-- insert into t_sys_role_ent_rela select '801', ent_id from t_sys_entitlement;

-- 超管用户： jeepay / jeepay123
insert into t_sys_user values (801, 'jeepay', '超管', '13000000001', '1', 'https://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/defava_m.png', 'D0001', 1, 1, 'MGR', '0', '2020-06-13', '2020-06-13');
insert into t_sys_user_auth values (801, '801', '1', 'jeepay', '$2a$10$WKuPJKE1XhX15ibqDM745eOCaZZVUiRitUjEyX6zVNd9k.cQXfzGa', 'testkey', 'MGR');

-- insert into t_sys_user_role_rela values (801, 801);

INSERT INTO `t_sys_config` VALUES ('mgrSiteUrl', '运营平台网址(不包含结尾/)', '运营平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9217', 'text', 0, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('mchSiteUrl', '商户平台网址(不包含结尾/)', '商户平台网址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9218', 'text', 0, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('paySiteUrl', '支付网关地址(不包含结尾/)', '支付网关地址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9216', 'text', 0, '2021-5-18 14:46:10');
INSERT INTO `t_sys_config` VALUES ('ossPublicSiteUrl', '公共oss访问地址(不包含结尾/)', '公共oss访问地址(不包含结尾/)', 'applicationConfig', '系统应用配置', 'http://127.0.0.1:9217/api/anon/localOssFiles', 'text', 0, '2021-5-18 14:46:10');


-- 初始化支付方式
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_BAR', '支付宝条码');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_JSAPI', '支付宝生活号');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_APP', '支付宝APP');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_WAP', '支付宝WAP');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_PC', '支付宝PC网站');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('ALI_QR', '支付宝二维码');

INSERT INTO t_pay_way (way_code, way_name) VALUES ('WX_BAR', '微信条码');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('WX_JSAPI', '微信公众号');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('WX_APP', '微信APP');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('WX_H5', '微信H5');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('WX_NATIVE', '微信扫码');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('WX_LITE', '微信小程序');

INSERT INTO t_pay_way (way_code, way_name) VALUES ('YSF_BAR', '云闪付条码');
INSERT INTO t_pay_way (way_code, way_name) VALUES ('YSF_JSAPI', '云闪付jsapi');

-- 初始化支付接口定义
INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('alipay', '支付宝官方', 1, 1,
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"pid","desc":"合作伙伴身份（PID）","type":"text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"privateKey", "desc":"应用私钥", "type": "textarea","verify":"required"},{"name":"alipayPublicKey", "desc":"支付宝公钥(不使用证书时必填)", "type": "textarea"},{"name":"signType","desc":"接口签名方式(推荐使用RSA2)","type":"radio","verify":"","values":"RSA,RSA2","titles":"RSA,RSA2","verify":"required"},{"name":"useCert","desc":"公钥证书","type":"radio","verify":"","values":"1,0","titles":"使用证书（请使用RSA2私钥）,不使用证书"},{"name":"appPublicCert","desc":"应用公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayPublicCert","desc":"支付宝公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayRootCert","desc":"支付宝根证书（.crt格式）","type":"file","verify":""}]',
        '[{"name":"appAuthToken", "desc":"子商户app_auth_token", "type": "text","readonly":"readonly"},{"name":"refreshToken", "desc":"子商户刷新token", "type": "hidden","readonly":"readonly"},{"name":"expireTimestamp", "desc":"authToken有效期（13位时间戳）", "type": "hidden","readonly":"readonly"}]',
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"privateKey", "desc":"应用私钥", "type": "textarea","verify":"required"},{"name":"alipayPublicKey", "desc":"支付宝公钥(不使用证书时必填)", "type": "textarea"},{"name":"signType","desc":"接口签名方式(推荐使用RSA2)","type":"radio","verify":"","values":"RSA,RSA2","titles":"RSA,RSA2","verify":"required"},{"name":"useCert","desc":"公钥证书","type":"radio","verify":"","values":"1,0","titles":"使用证书（请使用RSA2私钥）,不使用证书"},{"name":"appPublicCert","desc":"应用公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayPublicCert","desc":"支付宝公钥证书（.crt格式）","type":"file","verify":""},{"name":"alipayRootCert","desc":"支付宝根证书（.crt格式）","type":"file","verify":""}]',
        '[{"wayCode": "ALI_JSAPI"}, {"wayCode": "ALI_WAP"}, {"wayCode": "ALI_BAR"}, {"wayCode": "ALI_APP"}, {"wayCode": "ALI_PC"}, {"wayCode": "ALI_QR"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/alipay.png', '#1779FF', 1, '支付宝官方通道');

INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('wxpay', '微信支付官方', 1, 1,
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"key", "desc":"API密钥", "type": "textarea","verify":"required"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"apiV3Key", "desc":"API V3秘钥（V3接口必填）", "type": "textarea","verify":""},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":""},{"name":"cert", "desc":"API证书(.p12格式)", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(.pem格式)", "type": "file","verify":""}]',
        '[{"name":"subMchId","desc":"子商户ID","type":"text","verify":"required"},{"name":"subMchAppId","desc":"子账户appID(线上支付必填)","type":"text","verify":""}]',
        '[{"name":"mchId", "desc":"微信支付商户号", "type": "text","verify":"required"},{"name":"appId","desc":"应用App ID","type":"text","verify":"required"},{"name":"appSecret","desc":"应用AppSecret","type":"text","verify":"required"},{"name":"oauth2Url", "desc":"oauth2地址（置空将使用官方）", "type": "text"},{"name":"key", "desc":"API密钥", "type": "textarea","verify":"required"},{"name":"apiVersion", "desc":"微信支付API版本", "type": "radio","values":"V2,V3","titles":"V2,V3","verify":"required"},{"name":"apiV3Key", "desc":"API V3秘钥（V3接口必填）", "type": "textarea","verify":""},{"name":"serialNo", "desc":"序列号（V3接口必填）", "type": "textarea","verify":""},{"name":"cert", "desc":"API证书(.p12格式)", "type": "file","verify":""},{"name":"apiClientKey", "desc":"私钥文件(.pem格式)", "type": "file","verify":""}]',
        '[{"wayCode": "WX_APP"}, {"wayCode": "WX_H5"}, {"wayCode": "WX_NATIVE"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "WX_BAR"}, {"wayCode": "WX_LITE"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/wxpay.png', '#04BE02', 1, '微信官方通道');

INSERT INTO t_pay_interface_define (if_code, if_name, is_mch_mode, is_isv_mode, isv_params, isvsub_mch_params, normal_mch_params, way_codes, icon, bg_color, state, remark)
VALUES ('ysfpay', '云闪付官方', 0, 1,
        '[{"name":"sandbox","desc":"环境配置","type":"radio","verify":"","values":"1,0","titles":"沙箱环境,生产环境","verify":"required"},{"name":"serProvId","desc":"服务商开发ID[serProvId]","type":"text","verify":"required"},{"name":"isvPrivateCertFile","desc":"服务商私钥文件（.pfx格式）","type":"file","verify":"required"},{"name":"isvPrivateCertPwd","desc":"服务商私钥文件密码","type":"text","verify":"required"},{"name":"ysfpayPublicKey","desc":"云闪付开发公钥（证书管理页面可查询）","type":"textarea","verify":"required"},{"name":"acqOrgCode","desc":"可用支付机构编号","type":"text","verify":"required"}]',
        '[{"name":"merId","desc":"商户编号","type":"text","verify":"required"}]',
        NULL,
        '[{"wayCode": "YSF_BAR"}, {"wayCode": "ALI_JSAPI"}, {"wayCode": "WX_JSAPI"}, {"wayCode": "ALI_BAR"}, {"wayCode": "WX_BAR"}]',
        'http://jeequan.oss-cn-beijing.aliyuncs.com/jeepay/img/ysfpay.png', 'red', 1, '云闪付官方通道');
