package com.movit.platform.im.module.group.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;

import java.util.List;

/**
 * Created by Administrator on 2015/11/17.
 */
public class ATMemberAdapter extends BaseAdapter {

    private Context _context;
    private List<UserInfo> _members;
    private int _groupType;
    private String _groupName;

    public ATMemberAdapter(Context context, List<UserInfo> members, int groupType, String groupName) {
        this._context = context;
        this._members = members;
        this._groupType = groupType;
        this._groupName = groupName;
    }

    @Override
    public int getCount() {
        return _members.size();
    }

    @Override
    public Object getItem(int position) {
        return _members.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (null == convertView) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(_context).inflate(R.layout.im_item_at_members, null);
            holder.tv_member_name = (TextView) convertView.findViewById(R.id.tv_member_name);
            holder.iv_member_icon = (ImageView) convertView.findViewById(R.id.iv_member_icon);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        switch (_groupType) {
            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                holder.tv_member_name.setText(_members.get(position).getEmpCname() + " " + _members.get(position).getEmpAdname());
                setDifferentColor(position, holder);
                setMemberIcon(convertView, holder.iv_member_icon, position);
                break;
            case CommConstants.CHAT_TYPE_GROUP_ANS:
                holder.tv_member_name.setText(IMConstants.ansGroupMembers.get(_groupName + "," + _members.get(position).getId()));
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(_context, R.drawable.avatar_ans, 10);
                holder.iv_member_icon.setImageBitmap(bitmap);
                break;
            default:
                break;
        }
        return convertView;
    }

    /**
     * 文字内容分段设置不同的颜色
     * @param position
     * @param holder
     */
    private void setDifferentColor(int position, ViewHolder holder) {
        SpannableStringBuilder builder = new SpannableStringBuilder(holder.tv_member_name.getText().toString());
        //ForegroundColorSpan 为文字前景色，BackgroundColorSpan为文字背景色
        ForegroundColorSpan blackSpan = new ForegroundColorSpan(Color.BLACK);
        ForegroundColorSpan graySpan = new ForegroundColorSpan(Color.rgb(200, 200, 200));

        builder.setSpan(blackSpan, 0, _members.get(position).getEmpCname().length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.setSpan(graySpan, _members.get(position).getEmpCname().length() + 1, holder.tv_member_name.getText().length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        holder.tv_member_name.setText(builder);
    }

    private void setMemberIcon(View convertView, final ImageView iv_member_icon, int position) {

        String avatar = _members.get(position).getAvatar();

        int picId = R.drawable.avatar_male;
        if ("男".equals(_members.get(position).getGender())) {
            picId = R.drawable.avatar_male;
        } else if ("女".equals(_members.get(position).getGender())) {
            picId = R.drawable.avatar_female;
        }

        final Bitmap bitmap = PicUtils.getRoundedCornerBitmap(_context, picId, 10);
        if (StringUtils.notEmpty(avatar)) {
            BitmapAjaxCallback callback = new BitmapAjaxCallback();
            //为了适配其他项目
            if (avatar.startsWith("http")) {
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(avatar).memCache(true)
                        .fileCache(true).targetWidth(128);
            } else {
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatar).memCache(true)
                        .fileCache(true).targetWidth(128);
            }
            AQuery aQuery = new AQuery(_context).recycle(convertView);
//            aQuery.id(iv_member_icon).image(callback);
            aQuery.id(iv_member_icon).image(CommConstants.URL_DOWN + avatar,true,true,0,0,
                    new BitmapAjaxCallback(){
                        @Override
                        protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
                            super.callback(url, iv, bm, status);
                            if( null == bm || bm.getByteCount() <= 0){
                                iv_member_icon.setImageBitmap(bitmap);
                            }
                        }
                    });
        } else {
//            Bitmap bitmap = PicUtils.getRoundedCornerBitmap(_context,
//                    picId, 10);
            iv_member_icon.setImageBitmap(bitmap);
        }
    }

    public final class ViewHolder {
        public TextView tv_member_name;
        public ImageView iv_member_icon;
    }
}
