package com.forte.qqrobot.beans.messages.result;

/**
 * 群共享文件列表
 * @author ForteScarlet <[163邮箱地址]ForteScarlet@163.com>
 * @since JDK1.8
 **/
public interface ShareList extends InfoResultList<ShareList.Share> {

    /** 获取分享列表（文件列表） */
//    Share[] getShareList();

    /**
     * 分享(文件)
     */
    interface Share extends ResultInner{
        /** BUSID */
        String getBusid();
        /** 创建时间 */
        Long getCreateTime();
        /** 上传完成时间 */
        Long getModiflyTime();

        /** 下载次数 */
        Integer getDLTimes();
        /** 文件名 */
        String getName();
        /** 文件路径 */
        String getFilePath();
        /** 文件大小 */
        Long getSize();
        /** 本地文件名 */
        String getLocalName();

        /** 上传者群昵称 */
        String getNick();
        /** 上传者QQ号 */
        String getQQ();
    }
}
