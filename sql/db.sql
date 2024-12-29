create schema if not exists code_sandbox_auth;
use code_sandbox_auth;

-- code_sandbox_auth.`auth`
create table if not exists code_sandbox_auth.`auth`
(
    `id` bigint not null auto_increment comment '主键' primary key,
    `createTime` datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    `updateTime` datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    `isDeleted` tinyint default 0 not null comment '是否删除(0-未删, 1-已删)',
    `accessKey` varchar(256) not null comment 'accessKey',
    `secretKey` varchar(256) not null comment 'secretKey'
) comment '权限表';


INSERT INTO code_sandbox_auth.auth (id, createTime, updateTime, isDeleted, accessKey, secretKey) VALUES (1, '2024-12-28 18:36:09', '2024-12-28 18:36:09', 0, 'oj-backend', '123456789');
INSERT INTO code_sandbox_auth.auth (id, createTime, updateTime, isDeleted, accessKey, secretKey) VALUES (2, '2024-12-28 18:36:25', '2024-12-28 18:36:25', 0, 'test', '123456789');
