-- ==============================================================================
-- CRM 客户关系与运营管理系统数据库初始化脚本: mall_demo
-- 适用数据库: MySQL 8.0+
-- 包含: 用户表、角色表、权限表、用户角色关联、角色权限关联、商品表、地区表、订单表、订单明细表、物流表、物流轨迹表
-- 强制采用 utf8mb4 字符编码与 utf8mb4_general_ci 排序规则
-- ==============================================================================

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

CREATE DATABASE IF NOT EXISTS `mall_demo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER DATABASE `mall_demo` CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci;

USE `mall_demo`;

-- ------------------------------------------------------------------------------
-- 1. 系统用户表 (sys_user)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `username` VARCHAR(50) NOT NULL COMMENT '登录用户名(唯一)',
  `password` VARCHAR(100) NOT NULL COMMENT 'BCrypt加密密码',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '用户昵称/姓名',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1:启用, 0:禁用)',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统用户表';

-- ------------------------------------------------------------------------------
-- 2. 角色表 (sys_role)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称',
  `role_code` VARCHAR(50) NOT NULL COMMENT '角色编码',
  `description` VARCHAR(200) DEFAULT NULL COMMENT '角色描述',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统角色表';

-- ------------------------------------------------------------------------------
-- 3. 权限/菜单表 (sys_permission)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '权限ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级权限ID(0为根)',
  `name` VARCHAR(50) NOT NULL COMMENT '权限/菜单名称',
  `perm_code` VARCHAR(100) DEFAULT NULL COMMENT '权限唯一标识代码',
  `type` TINYINT NOT NULL DEFAULT 1 COMMENT '类型(1:菜单, 2:按钮/接口操作)',
  `sort` INT NOT NULL DEFAULT 0 COMMENT '排序权重',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统权限表';

-- ------------------------------------------------------------------------------
-- 4. 用户-角色关联表 (sys_user_role)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户角色关联表';

-- ------------------------------------------------------------------------------
-- 5. 角色-权限关联表 (sys_role_permission)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` BIGINT NOT NULL COMMENT '角色ID',
  `permission_id` BIGINT NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_role_id` (`role_id`),
  KEY `idx_perm_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='角色权限关联表';

-- ------------------------------------------------------------------------------
-- 6. 商品表 (product) - 扩展 CRM 分类与图文描述、状态
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `category` VARCHAR(50) NOT NULL DEFAULT '数码电子' COMMENT '商品分类',
  `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '销售价格',
  `stock` INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0:待审核, 1:在售, 2:已驳回, 3:已下架)',
  `reject_reason` VARCHAR(255) DEFAULT NULL COMMENT '审核驳回原因',
  `description` TEXT DEFAULT NULL COMMENT '商品详细规格与图文说明',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM商品信息表';

-- ------------------------------------------------------------------------------
-- 7. 行政地区表 (region)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `region`;
CREATE TABLE `region` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '地区ID',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父级地区ID(0表示顶级)',
  `name` VARCHAR(50) NOT NULL COMMENT '地区名称',
  `level` TINYINT NOT NULL COMMENT '层级(1:省/直辖市, 2:市, 3:区/县)',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='地区树形字典表';

-- ------------------------------------------------------------------------------
-- 8. 订单主表 (crm_order)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_order`;
CREATE TABLE `crm_order` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '订单流水编号',
  `user_id` BIGINT NOT NULL COMMENT '下单客户用户ID',
  `customer_name` VARCHAR(50) NOT NULL COMMENT '客户姓名',
  `customer_phone` VARCHAR(20) NOT NULL COMMENT '客户联系电话',
  `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总金额',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '订单状态(1:待支付, 2:待发货, 3:已发货, 4:已完成, 5:已取消)',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收件人姓名',
  `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收件人电话',
  `receiver_address` VARCHAR(255) NOT NULL COMMENT '收货详细地址',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '订单客户备注',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `finish_time` DATETIME DEFAULT NULL COMMENT '完成时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单主表';

-- ------------------------------------------------------------------------------
-- 9. 订单商品明细表 (crm_order_item)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_order_item`;
CREATE TABLE `crm_order_item` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `order_id` BIGINT NOT NULL COMMENT '关联订单主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单编号',
  `product_id` BIGINT NOT NULL COMMENT '商品ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称快照',
  `product_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '销售单价快照',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '购买数量',
  `subtotal` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '小计金额',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM订单明细表';

-- ------------------------------------------------------------------------------
-- 10. 物流运单表 (crm_logistics)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_logistics`;
CREATE TABLE `crm_logistics` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单编号',
  `company_name` VARCHAR(50) NOT NULL COMMENT '物流快递承运商(如:顺丰速运/京东快递)',
  `company_code` VARCHAR(30) NOT NULL COMMENT '承运商简码(SF/JD/ZTO/YTO)',
  `tracking_no` VARCHAR(64) NOT NULL COMMENT '物流快递单号',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '物流状态(1:待揽件, 2:运输中, 3:派送中, 4:已签收, 5:异常滞留)',
  `operator_name` VARCHAR(50) DEFAULT NULL COMMENT '发货经办人员',
  `ship_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发货出库时间',
  `delivered_time` DATETIME DEFAULT NULL COMMENT '签收交付时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_tracking_no` (`tracking_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM物流运单主表';

-- ------------------------------------------------------------------------------
-- 11. 物流轨迹节点表 (crm_logistics_track)
-- ------------------------------------------------------------------------------
DROP TABLE IF EXISTS `crm_logistics_track`;
CREATE TABLE `crm_logistics_track` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '轨迹ID',
  `order_no` VARCHAR(64) NOT NULL COMMENT '关联订单编号',
  `tracking_no` VARCHAR(64) NOT NULL COMMENT '运单号',
  `content` VARCHAR(255) NOT NULL COMMENT '流转节点事件描述',
  `location` VARCHAR(100) DEFAULT NULL COMMENT '流转网点/地理位置',
  `operator` VARCHAR(50) DEFAULT NULL COMMENT '经办网点/配送员',
  `track_time` DATETIME NOT NULL COMMENT '轨迹发生时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_tracking_no` (`tracking_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='CRM物流轨迹节点表';


-- ==============================================================================
-- 种子数据初始化
-- ==============================================================================

-- 1. 用户种子 (密码统一为明文: 123456)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `phone`, `status`) VALUES
(1, 'admin', '$2a$10$7YGKJ6V/udVbvwgI1piWX.FDkbRLzBogyVVYe75VZfM4eYQQlZsDy', '系统超级管理员', '13800138000', 1),
(2, 'operator', '$2a$10$7YGKJ6V/udVbvwgI1piWX.FDkbRLzBogyVVYe75VZfM4eYQQlZsDy', '商品运营主管', '13800138001', 1),
(3, 'logistics_user', '$2a$10$7YGKJ6V/udVbvwgI1piWX.FDkbRLzBogyVVYe75VZfM4eYQQlZsDy', '仓储物流专员', '13800138002', 1),
(4, 'service_user', '$2a$10$7YGKJ6V/udVbvwgI1piWX.FDkbRLzBogyVVYe75VZfM4eYQQlZsDy', '客户服务专员', '13800138003', 1);

-- 2. 角色种子
INSERT INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'ROLE_ADMIN', '拥有系统全模块的所有操作与分配权限'),
(2, '运营主管', 'ROLE_OPERATOR', '负责商品上下架发布、类目管控与订单监控'),
(3, '仓储物流专员', 'ROLE_LOGISTICS', '负责待发货订单出库、物流单号录入与运单流转跟踪'),
(4, '客服人员', 'ROLE_CUSTOMER_SERVICE', '负责客户咨询、订单信息查询与物流轨迹回溯');

-- 3. 权限与功能字典种子
INSERT INTO `sys_permission` (`id`, `parent_id`, `name`, `perm_code`, `type`, `sort`) VALUES
(1, 0, '订单管理', 'crm:order', 1, 1),
(2, 1, '订单综合查询', 'crm:order:list', 2, 1),
(3, 1, '订单详情查看', 'crm:order:detail', 2, 2),
(4, 1, '订单发货操作', 'crm:order:ship', 2, 3),

(5, 0, '商品管理', 'crm:product', 1, 2),
(6, 5, '商品列表查询', 'crm:product:list', 2, 1),
(7, 5, '商品发布创建', 'crm:product:publish', 2, 2),
(8, 5, '商品修改编辑', 'crm:product:edit', 2, 3),
(9, 5, '商品上下架切换', 'crm:product:status', 2, 4),
(10, 5, '商品物理删除', 'crm:product:delete', 2, 5),
(18, 5, '商品发布审核', 'crm:product:audit', 2, 6),

(11, 0, '物流中心', 'crm:logistics', 1, 3),
(12, 11, '物流发货录入', 'crm:logistics:ship', 2, 1),
(13, 11, '物流轨迹追踪', 'crm:logistics:query', 2, 2),

(14, 0, '用户权限', 'crm:system', 1, 4),
(15, 14, '用户列表查询', 'crm:user:list', 2, 1),
(16, 14, '用户角色分配', 'crm:user:role', 2, 2),
(17, 14, '角色权限配置', 'crm:role:perm', 2, 3);

-- 4. 用户与角色绑定 (admin: 超管; operator: 运营; logistics_user: 物流; service_user: 客服)
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4);

-- 5. 角色与权限绑定 (超级管理员绑定全量 1~17)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(1, 1), (1, 2), (1, 3), (1, 4),
(1, 5), (1, 6), (1, 7), (1, 8), (1, 9), (1, 10),
(1, 11), (1, 12), (1, 13),
(1, 14), (1, 15), (1, 16), (1, 17), (1, 18),
-- 运营主管: 订单查询、商品全量(含审核)
(2, 1), (2, 2), (2, 3),
(2, 5), (2, 6), (2, 7), (2, 8), (2, 9), (2, 18),
-- 物流专员: 订单发货、物流全量
(3, 1), (3, 2), (3, 4),
(3, 11), (3, 12), (3, 13),
-- 客服专员: 订单查询与详情、物流轨迹查看
(4, 1), (4, 2), (4, 3),
(4, 11), (4, 13);

-- 6. 商品种子数据
INSERT INTO `product` (`id`, `name`, `category`, `price`, `stock`, `status`, `reject_reason`, `description`) VALUES
(1, 'iPhone 15 Pro Max 256GB 原色钛金属', '数码电子', 9999.00, 85, 1, NULL, '配备 A17 Pro 芯片，航空级钛金属边框设计，支持 5 倍光学变焦镜头'),
(2, 'MacBook Pro 16英寸 M3 Max 36G+1T', '数码电子', 24999.00, 48, 1, NULL, '顶配专业生产力工具，Liquid 视网膜 XDR 屏，强劲续航达 22 小时'),
(3, '索尼 (SONY) WH-1000XM5 无线降噪耳机', '影音娱乐', 2299.00, 180, 1, NULL, '双芯驱动旗舰降噪，集成处理器 V1 与 HD 降噪处理器 QN1，舒适佩戴'),
(4, '罗技 (Logitech) MX Master 3S 无线鼠标', '电脑办公', 599.00, 260, 1, NULL, '8000 DPI 高精度传感器，静音微动按键，MagSpeed 电磁滚轮'),
(5, '27英寸 4K 144Hz MiniLED 广色域电竞显示器', '电脑办公', 2899.00, 72, 1, NULL, '1152 分区独立控光，HDR1000 高动态对比度，支持 Type-C 90W 反向供电'),
(6, '华为 Mate 60 Pro 12GB+512GB 雅川青', '数码电子', 6999.00, 95, 1, NULL, '超可靠玄武架构，全焦段超清影像，支持卫星通话与自研鸿蒙智慧体验'),
(7, '大疆 DJI Mini 4 Pro 航拍无人机带屏套装', '数码电子', 6188.00, 30, 1, NULL, '全向主动避障，4K/60fps HDR 真实色彩，长达 34 分钟单电飞行续航'),
(8, '小米 14 Ultra 钛金属特别版 16GB+1TB', '数码电子', 7799.00, 60, 0, NULL, '徕卡全焦段四摄，一英寸可变光圈主摄，双向卫星通信，全新发布待审核'),
(9, '仿造名牌超长续航三防电子手表', '影音娱乐', 99.00, 999, 2, '商品缺少官方授权资质文件，且描述涉嫌虚假宣传，审核不予通过', '超强待机180天，多功能蓝牙通话');

-- 7. 地区种子数据
INSERT INTO `region` (`id`, `parent_id`, `name`, `level`) VALUES
(1, 0, '广东省', 1),
(2, 1, '广州市', 2),
(3, 1, '深圳市', 2),
(4, 2, '天河区', 3),
(5, 0, '北京市', 1),
(6, 5, '朝阳区', 2),
(7, 0, '上海市', 1),
(8, 7, '浦东新区', 2);

-- 8. CRM 订单种子数据 (涵盖各种状态便于测试)
INSERT INTO `crm_order` (`id`, `order_no`, `user_id`, `customer_name`, `customer_phone`, `total_amount`, `status`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`, `pay_time`, `ship_time`, `finish_time`, `create_time`) VALUES
(1, 'ORD202610250001A01', 1, '系统管理员', '13800138000', 9999.00, 2, '张伟', '13800138000', '广东省广州市天河区天河路208号粤海天河城A座1201', '发顺丰特快，送前请电话联络', '2026-10-25 09:15:00', NULL, NULL, '2026-10-25 09:00:00'),
(2, 'ORD202610250002B02', 2, '商品运营主管', '13800138001', 25598.00, 3, '李娜', '13800138001', '上海市浦东新区陆家嘴环路1000号恒生银行大厦25楼', '办公急用设备，感谢加急安排', '2026-10-24 14:10:00', '2026-10-24 17:30:00', NULL, '2026-10-24 14:00:00'),
(3, 'ORD202610250003C03', 3, '仓储物流专员', '13800138002', 2299.00, 4, '王强', '13800138002', '北京市朝阳区建国路87号北京SKP 4层', '自提或放前台即可', '2026-10-23 10:20:00', '2026-10-23 11:30:00', '2026-10-24 16:00:00', '2026-10-23 10:00:00'),
(4, 'ORD202610250004D04', 4, '客户服务专员', '13800138003', 6999.00, 1, '赵敏', '13800138003', '广东省深圳市南山区高新南九道科技园腾讯大厦', '等待客户支付尾款', NULL, NULL, NULL, '2026-10-25 15:30:00');

-- 9. 订单明细种子
INSERT INTO `crm_order_item` (`order_id`, `order_no`, `product_id`, `product_name`, `product_price`, `quantity`, `subtotal`) VALUES
(1, 'ORD202610250001A01', 1, 'iPhone 15 Pro Max 256GB 原色钛金属', 9999.00, 1, 9999.00),
(2, 'ORD202610250002B02', 2, 'MacBook Pro 16英寸 M3 Max 36G+1T', 24999.00, 1, 24999.00),
(2, 'ORD202610250002B02', 4, '罗技 (Logitech) MX Master 3S 无线鼠标', 599.00, 1, 599.00),
(3, 'ORD202610250003C03', 3, '索尼 (SONY) WH-1000XM5 无线降噪耳机', 2299.00, 1, 2299.00),
(4, 'ORD202610250004D04', 6, '华为 Mate 60 Pro 12GB+512GB 雅川青', 6999.00, 1, 6999.00);

-- 10. 物流运单种子 (针对已发货 ORD202610250002B02 与已完成 ORD202610250003C03)
INSERT INTO `crm_logistics` (`order_no`, `company_name`, `company_code`, `tracking_no`, `status`, `operator_name`, `ship_time`, `delivered_time`) VALUES
('ORD202610250002B02', '顺丰速运', 'SF', 'SF168892026102401', 2, '仓储物流专员', '2026-10-24 17:30:00', NULL),
('ORD202610250003C03', '京东快递', 'JD', 'JD0099882026102302', 4, '仓储物流专员', '2026-10-23 11:30:00', '2026-10-24 16:00:00');

-- 11. 物流轨迹节点种子
INSERT INTO `crm_logistics_track` (`order_no`, `tracking_no`, `content`, `location`, `operator`, `track_time`) VALUES
('ORD202610250002B02', 'SF168892026102401', '顺丰速运 已揽收，包裹准备发往华东分拨中心', '华东总仓仓库', '揽收员 曹师傅 (13911112222)', '2026-10-24 17:45:00'),
('ORD202610250002B02', 'SF168892026102401', '快件已到达【上海嘉定转运中心】，分拣完毕准备发往下一站', '上海市嘉定转运中心', '分拣中心操作员', '2026-10-24 23:20:00'),
('ORD202610250002B02', 'SF168892026102401', '快件已到达【上海浦东陆家嘴营业点】，正由专线配送车发出', '上海市浦东陆家嘴营业点', '营业点调度员', '2026-10-25 08:30:00'),
('ORD202610250002B02', 'SF168892026102401', '快件正在派送中，顺丰小哥正在为您送达，请保持电话畅通', '上海市浦东新区', '派送员 陈师傅 (13822223333)', '2026-10-25 09:10:00'),

('ORD202610250003C03', 'JD0099882026102302', '京东物流已收件，正发往北京通州分拣中心', '北京亦庄自营仓', '京东揽收员', '2026-10-23 11:45:00'),
('ORD202610250003C03', 'JD0099882026102302', '快件到达【北京大兴中转场】，正在装车发出', '北京大兴中转场', '装车组', '2026-10-23 18:00:00'),
('ORD202610250003C03', 'JD0099882026102302', '快件到达【北京朝阳国贸营业部】，开始派件', '北京朝阳国贸营业部', '配送员 孙师傅 (13766667777)', '2026-10-24 09:30:00'),
('ORD202610250003C03', 'JD0099882026102302', '客户已签收，签收人: 本人签收。感谢使用京东快递！', '北京朝阳区', '配送员 孙师傅', '2026-10-24 16:00:00');
