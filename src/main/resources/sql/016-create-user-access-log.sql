-- ----------------------------
-- Table structure for user_access_log
-- ----------------------------
create table if not exists user_access_log
(
    id             bigint unsigned not null comment '主键',
    user_id        bigint unsigned not null default 0 comment '用户ID',
    username       varchar(64)     null comment '用户名(冗余)',
    node_id        bigint unsigned not null default 0 comment '节点ID',
    target_address varchar(255)    not null comment '目标地址(域名或IP)',
    address_type   tinyint         not null default 1 comment '地址类型 1:域名 2:IP',
    hit_count      int             not null default 0 comment '访问次数',
    access_time    datetime        not null comment '日志聚合时间(通常为每小时)',
    create_by      varchar(64)     null comment '创建人',
    create_time    datetime        null comment '创建时间',
    update_by      varchar(64)     null comment '更新人',
    update_time    datetime        null comment '更新时间',
    deleted        tinyint         not null default 0 comment '逻辑删除 0:未删除 1:已删除',
    primary key (id),
    key idx_user_time (user_id, access_time),
    key idx_node_time (node_id, access_time),
    key idx_target_time (target_address, access_time),
    key idx_access_time (access_time)
) engine = InnoDB
  character set = utf8mb4
  comment = '用户访问行为日志表';
