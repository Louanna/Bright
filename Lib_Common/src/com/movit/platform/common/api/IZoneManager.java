/*
 * Copyright (C) 2015 Bright Yu Haiyang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: y.haiyang@qq.com
 */

package com.movit.platform.common.api;

import android.os.Handler;

import com.movit.platform.framework.core.okhttp.callback.StringCallback;

/**
 * Created by Louanna.Lu on 2015/10/20.
 *
 */
public interface IZoneManager {

    void getKnowledge(String userId, int page,StringCallback responseCallback );
    void saveKnowlegeLike(String knowledgeId,String userId,String createBy, StringCallback responseCallback );
    void delKnowledgeLike(String knowledgeId,String userId, StringCallback responseCallback );
    void saveKnowledgeComment(String knowledgeId,String userId,String content, StringCallback responseCallback );

    //上传同事圈背景图片
    public void uploadFile(final String filePath,final Handler handler);

    //获取说说列表
    public void getZoneList(final String officeId, final String refreshTime, final String tCreateTime, final String bCreateTime,
                            final String isAfter, final String type, final String isSecret,
                            final Handler handler);

    //发布说说
    public void say(final String content, final String type,
                    final String isSecret, final String sImages, final String sAtGroup,
                    final String sAtPerson, final String sMessageList,
                    final Handler handler);

    //获取说说详情
    public void getSay(final String cSayId, final Handler handler);

    //删除说说
    public void saydel(final String sayId, final int postion,
                       final Handler handler);

    //获取说说数量
    public void mysaycount(final Handler handler);

    //是否有新说说
    public void havenew(final String officeId, final String dCreateTime, final Handler handler);

    //获取个人主页
    public void getPersonalZoneList(final String userId,
                                    final String refreshTime, final String tCreateTime,
                                    final String bCreateTime, final String isAfter, final String type,
                                    final String isSecret, final Handler handler);

    //点赞
    public void nice(final String cSayId, final String touserId,
                     final String undo, final int postion, final Handler handler);

    //发表评论
    public void comment(final String cSayId, final String userId,
                        final String touserId, final String sContent,
                        final String cParentId, final String cRootId, final int postion,
                        final Handler handler);

    //删除评论
    public void commentdel(final String commentId, final int postion,
                           final int delCommentLine, final Handler handler);

    //获取动态消息
    public void messages(final Handler handler);

    //获取动态消息数量
    public void messagecount(final Handler handler);

    //清空消息
    public void messagedel(final Handler handler);

}
