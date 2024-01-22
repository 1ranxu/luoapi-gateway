package com.luoying.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class InterfaceInfo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 接口名称
     */
    private String name;

    /**
     * 接口地址
     */
    private String url;

    /**
     * 创建人
     */
    private Long userId;

    /**
     * 请求方法
     */
    private String method;

    /**
     * 接口请求参数
     */
    private String requestParams;

    /**
     * 接口响应参数
     */
    private String responseParams;

    /**
     * 扣除积分数
     */
    private Long reduceScore;

    /**
     * 请求示例
     */
    private String requestExample;

    /**
     * 请求头
     */
    private String requestHeader;

    /**
     * 响应头
     */
    private String responseHeader;

    /**
     * 返回格式（JSON等）
     */
    private String returnFormat;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 接口状态 0-关闭 1-开启
     */
    private Integer status;

    /**
     * 接口总调用次数
     */
    private Integer totalInvokes;

    /**
     * 接口头像
     */
    private String avatarUrl;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除
     */
    private Integer isDelete;

    private static final long serialVersionUID = 1L;
}