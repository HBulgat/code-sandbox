drop schema code_sandbox_auth;
create schema if not exists code_sandbox_auth;
use code_sandbox_auth;

-- 用户表
create table if not exists user
(
    id           bigint auto_increment comment 'id' primary key,
    userAccount  varchar(256)                           not null comment '账号',
    userPassword varchar(512)                           not null comment '密码',
    userRole     varchar(256) default 'user'            not null comment '用户角色：user/admin/ban',
    createTime   datetime     default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime     default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint      default 0                 not null comment '是否删除',
    index idx_userAccount(`userAccount`)
) comment '用户表';


-- code_sandbox_auth.`auth`
create table if not exists code_sandbox_auth.`auth`
(
    `id`         bigint not null auto_increment comment '主键' primary key,
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted`  tinyint default 0 not null comment '是否删除(0-未删, 1-已删)',
    `accessKey`  varchar(256) not null comment 'accessKey',
    `secretKey`  varchar(256) not null comment 'secretKey',
    `userId` bigint NOT NULL COMMENT '创建者 id',
    index `idx_userId` (`userId`),
    index `idx_accessKey` (`accessKey`)
) comment '权限表';
