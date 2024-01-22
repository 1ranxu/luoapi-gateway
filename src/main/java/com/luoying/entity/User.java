package com.luoying.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class User implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 性别 0-男 1-女
     */
    private Integer gender;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;

    /**
     * 密码
     */
    private String userPassword;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 密码
     */
    private String accessKey;

    /**
     * 密码
     */
    private String secretKey;

    /**
     * 积分余额,注册送30积分
     */
    private Long score;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}