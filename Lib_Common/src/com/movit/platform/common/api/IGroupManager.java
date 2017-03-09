package com.movit.platform.common.api;

import android.os.Handler;

/**
 * Created by Louanna.Lu on 2015/11/5.
 */
public interface IGroupManager {

    /**
     * 添加Group成员
     **/
    public void addMembers(final String groupId, final String memberIds,
                           final Handler handler) ;

    /**
     * 创建Group
     **/
    public void createGroup(final String memberIds, final String displayName,
                            final String description, final int type, final Handler handler);

    /**
     * 获取Group列表
     **/
    public void getGroupList();
}
