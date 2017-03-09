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

package com.movit.platform.im.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.manager.IMManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Louanna.Lu on 2015/10/16.
 */
public class ChatTools {

    public static void leaveChat() {
        if (IMConstants.CHATTING_TYPE.equals("chat")) {
            // 离开单人聊天会话：
            IMManager.leavePrivateSession(IMConstants.CHATTING_ID);
        } else if (IMConstants.CHATTING_TYPE.equals("group")) {
            // 离开群组聊天会话
            IMManager.leaveGroupSession(IMConstants.CHATTING_ID);
        }
    }

    public static Bitmap createAvatar(Context mContext, String groupId, List<Bitmap> bitmapList) {
        List<MyBitmapEntity> mEntityList = BitmapUtil.getBitmapEntitys(mContext, bitmapList.size());

        Bitmap[] mBitmaps = new Bitmap[bitmapList.size()];
        for (int i = 0; i < bitmapList.size(); i++) {
            int j = (int) Math.floor(i / 3);

            mBitmaps[i] = ThumbnailUtils.extractThumbnail(bitmapList.get(i), (int) mEntityList
                    .get(j).width, (int) mEntityList.get(j).width);
        }

        Bitmap groupAvatar = BitmapUtil.getCombineBitmaps(mEntityList, mBitmaps);
        PicUtils.saveBitmap(CommConstants.SD_DATA_PIC+groupId+".jpg",groupAvatar,100);

        return groupAvatar;
    }

}
