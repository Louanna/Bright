package com.movit.platform.im.module.detail.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;

public class ChatDetailAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<UserInfo> mData;
    private Handler handler;
    private boolean isDeling = false;
    GridView gridView;
    int ctype, groupType;
    List<String> adminIds;
    String createId;
    AQuery aq;

    public ChatDetailAdapter(Context context, List<UserInfo> mData,
                             Handler handler, GridView gridView, int ctype, String[] adminIds,
                             String createId, int groupType) {
        super();
        this.context = context;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(context);
        this.handler = handler;
        this.gridView = gridView;
        this.ctype = ctype;
        this.groupType = groupType;

        if (ctype == CommConstants.CHAT_TYPE_GROUP) {
            this.createId = createId;
        }
        aq = new AQuery(context);
    }

    public boolean isDeling() {
        return isDeling;
    }

    public void setDeling(boolean isDeling) {
        this.isDeling = isDeling;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(final int postion, View converView, ViewGroup arg2) {
        ViewHolder holder = null;
        if (converView == null) {
            holder = new ViewHolder();
            converView = mInflater.inflate(R.layout.im_item_chat_detail, arg2,
                    false);
            holder.name = (TextView) converView
                    .findViewById(R.id.gridview_item_name);
            holder.name1 = (TextView) converView
                    .findViewById(R.id.gridview_item_name1);
            holder.photo = (ImageView) converView
                    .findViewById(R.id.gridview_item_img);
            holder.delImg = (ImageView) converView
                    .findViewById(R.id.gridview_item_delImg);
            holder.grid_rl = (RelativeLayout) converView
                    .findViewById(R.id.grid_rl);
            converView.setTag(holder);
        } else {
            holder = (ViewHolder) converView.getTag();
        }
        UserInfo userInfo = (UserInfo) getItem(postion);
        int picId = R.drawable.avatar_male;
        AQuery aQuery = aq.recycle(converView);
        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            if (postion == getCount() - 1) {
                picId = R.drawable.group_invite_add;
                holder.name.setVisibility(View.INVISIBLE);
                holder.delImg.setVisibility(View.GONE);
                aQuery.id(holder.photo).image(picId);
            } else {
                setItem(holder, userInfo, postion, aQuery);
            }
        } else if (groupType == CommConstants.CHAT_TYPE_GROUP_PERSON) {
            //实名聊天
            String userId = MFSPHelper.getString(CommConstants.USERID);
            if (userId.equals(createId)) {
                if (postion == getCount() - 1) {
                    holder.name.setVisibility(View.INVISIBLE);
                    holder.delImg.setVisibility(View.GONE);
                    holder.photo.setVisibility(View.VISIBLE);
                    aQuery.id(holder.photo).image(R.drawable.group_kick_del);
                } else if (postion == getCount() - 2) {
                    holder.name.setVisibility(View.INVISIBLE);
                    holder.delImg.setVisibility(View.GONE);
                    aQuery.id(holder.photo).image(R.drawable.group_invite_add);
                } else {
                    setItem(holder, userInfo, postion, aQuery);
                }
            } else {
                if (postion == getCount() - 1) {
                    holder.name.setVisibility(View.INVISIBLE);
                    holder.delImg.setVisibility(View.GONE);
                    aQuery.id(holder.photo).image(R.drawable.group_invite_add);
                } else {
                    setItem(holder, userInfo, postion, aQuery);
                }
            }
        } else if (groupType == CommConstants.CHAT_TYPE_GROUP_ANS) {
            //匿名聊天
            String userId = MFSPHelper.getString(CommConstants.USERID);
            if (userId.equals(createId)) {
                if (postion == getCount() - 1) {
                    holder.name.setVisibility(View.INVISIBLE);
                    holder.delImg.setVisibility(View.GONE);
                    holder.photo.setVisibility(View.VISIBLE);
                    aQuery.id(holder.photo).image(R.drawable.group_kick_del);
                } else if (postion == getCount() - 2) {
                    holder.name.setVisibility(View.INVISIBLE);
                    holder.delImg.setVisibility(View.GONE);
                    aQuery.id(holder.photo).image(R.drawable.group_invite_add);
                } else {
                    setAnsItem(holder, userInfo, postion, aQuery);
                }
            } else {
                setAnsItem(holder, userInfo, postion, aQuery);
            }
        }
        return converView;
    }

    private void setAnsItem(ViewHolder holder, UserInfo user, final int postion,
                            AQuery aQuery) {

        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(user.getId());

        if (createId.equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID)) || userInfo.getId().equalsIgnoreCase(createId) || userInfo.getId().equalsIgnoreCase(MFSPHelper.getString(CommConstants.USERID))) {
            //当前用户为群主,显示：昵称（真名）
            //群主和自己显示：昵称（真名）

            int picId = R.drawable.avatar_male;
            if (context.getString(R.string.boy).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if (context.getString(R.string.girl).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_female;
            }

            holder.name.setText(user.getNickName());
            holder.name1.setText("(" + userInfo.getEmpCname() + ")");
            holder.name1.setVisibility(View.VISIBLE);

            String curUserAvatar = MFSPHelper.getString(CommConstants.AVATAR);
            String curUserAdname = MFSPHelper.getString(CommConstants.EMPADNAME);
            String avatarName = userInfo.getAvatar();
            String avatarUrl = "";
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (curUserAdname.equalsIgnoreCase(userInfo.getEmpAdname()) && StringUtils.notEmpty(curUserAvatar)) {
                avatarUrl = curUserAvatar;
            }
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();

                //为了适配其他项目
                if (avatarUrl.startsWith("http")) {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true).round(10)
                            .fallback(picId).url(avatarUrl)
                            .memCache(true).fileCache(true).targetWidth(128);
                } else {
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true).round(10)
                            .fallback(picId).url(CommConstants.URL_DOWN + avatarUrl)
                            .memCache(true).fileCache(true).targetWidth(128);
                }

                aQuery.id(holder.photo).image(callback);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(context, picId, 10);
                holder.photo.setImageBitmap(bitmap);
            }
        } else {
            //其他成员显示：昵称
            holder.name.setText(user.getNickName());
            holder.name1.setVisibility(View.GONE);
            Bitmap bitmap = PicUtils.getRoundedCornerBitmap(context, R.drawable.avatar_ans, 10);
            holder.photo.setImageBitmap(bitmap);
        }

        holder.delImg.setVisibility(View.GONE);

        if (isDeling) {
            holder.delImg.setVisibility(View.VISIBLE);
            if (postion == 0) {
                holder.delImg.setVisibility(View.GONE);
            }
            if (null != adminIds && adminIds.contains(userInfo.getId())) {
                // 是管理员
                holder.delImg.setVisibility(View.GONE);
            }
        } else {
            holder.delImg.setVisibility(View.GONE);
        }
        holder.delImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(1, postion).sendToTarget();
            }
        });
    }

    private void setItem(ViewHolder holder, UserInfo user, final int postion,
                         AQuery aQuery) {

        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(user.getId());

        holder.name.setText(userInfo.getEmpCname());
        holder.delImg.setVisibility(View.GONE);
        int picId = R.drawable.avatar_male;
        if (context.getString(R.string.boy).equals(userInfo.getGender())) {
            picId = R.drawable.avatar_male;
        } else if (context.getString(R.string.girl).equals(userInfo.getGender())) {
            picId = R.drawable.avatar_female;
        }
        String uname = MFSPHelper.getString(CommConstants.AVATAR);
        String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
        String avatarName = userInfo.getAvatar();
        String avatarUrl = "";
        if (StringUtils.notEmpty(avatarName)) {
            avatarUrl = avatarName;
        }
        if (adname.equalsIgnoreCase(userInfo.getEmpAdname())
                && StringUtils.notEmpty(uname)) {
            avatarUrl = uname;
        }
        if (StringUtils.notEmpty(avatarUrl)) {
            BitmapAjaxCallback callback = new BitmapAjaxCallback();

            //为了适配其他项目
            if (avatarUrl.startsWith("http")) {
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true).round(10)
                        .fallback(picId).url(avatarUrl)
                        .memCache(true).fileCache(true).targetWidth(128);
            } else {
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true).round(10)
                        .fallback(picId).url(CommConstants.URL_DOWN + avatarUrl)
                        .memCache(true).fileCache(true).targetWidth(128);
            }

            aQuery.id(holder.photo).image(callback);
        } else {
            Bitmap bitmap = PicUtils.getRoundedCornerBitmap(context, picId, 10);
            holder.photo.setImageBitmap(bitmap);
        }

        if (isDeling) {
            holder.delImg.setVisibility(View.VISIBLE);
            if (postion == 0) {
                holder.delImg.setVisibility(View.GONE);
            }
            if (ctype == CommConstants.CHAT_TYPE_GROUP) {
                if (null != adminIds && adminIds.contains(userInfo.getId())) {
                    // 是管理员
                    holder.delImg.setVisibility(View.GONE);
                }
            }
        } else {
            holder.delImg.setVisibility(View.GONE);
        }
        holder.delImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(1, postion).sendToTarget();
            }
        });
    }

    public final class ViewHolder {
        public RelativeLayout grid_rl;
        public ImageView photo;
        public TextView name;
        public TextView name1;
        public ImageView delImg;
    }

    public List<UserInfo> getmData() {
        return mData;
    }

    public void setmData(List<UserInfo> mData) {
        this.mData = mData;
    }

}
