package com.luoying.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class UserInterfaceInfo implements Serializable {
    /**
     * 主键
     */
    private Long id;

    /**
     * 调用者Id
     */
    private Long userId;

    /**
     * 接口id
     */
    private Long interfaceInfoId;

    /**
     * 已调用次数
     */
    private Long invokedNum;

    /**
     * 剩余调用次数
     */
    private Long leftNum;

    /**
     * 用户状态 0-限制 1-正常
     */
    private Integer status;

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
    private Integer idDelete;

    private static final long serialVersionUID = 1L;
}