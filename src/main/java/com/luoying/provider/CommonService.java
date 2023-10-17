package com.luoying.provider;


import com.luoying.entity.InterfaceInfo;
import com.luoying.entity.User;


public interface CommonService {
    /**
     * 根据accessKey查询数据库，是否存在包含该accessKey的用户
     *
     * @param accessKey
     * @return
     */
    User getInvokeUser(String accessKey);

    /**
     * 从数据库中查询接口是否存在（请求方法，请求路径）
     *
     * @param method
     * @param url
     * @return
     */
    InterfaceInfo getInvokeInterfaceInfo(String method, String url);

    /**
     * 接口调用次数统计
     *
     * @param interfaceInfoId
     * @param userId
     * @return
     */
    boolean invokeCount(long interfaceInfoId, long userId);
}
