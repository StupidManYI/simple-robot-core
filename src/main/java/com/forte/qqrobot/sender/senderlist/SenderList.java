package com.forte.qqrobot.sender.senderlist;

/**
 * 作为三个接口方法集合(list)相关的父类接口
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public interface SenderList {

    /**
     * 是不是送信接口
     */
    default boolean isSenderList(){
        return this instanceof SenderSendList;
    }
    /**
     * 是不是设置接口
     */
    default boolean isSetterList(){
        return this instanceof SenderSetList;
    }
    /**
     * 是不是获取接口
     */
    default boolean isGetterList(){
        return this instanceof SenderGetList;
    }

}
