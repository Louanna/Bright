package com.movit.platform.sc.module.msg.adapter;

import android.content.Context;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.widget.swipeLayout.SwipeLayout;
import com.movit.platform.framework.widget.swipeLayout.adapter.BaseSwipeAdapter;
import com.movit.platform.sc.R;
import com.movit.platform.sc.entities.ZoneMessage;
import com.movit.platform.sc.module.msg.activity.ZoneMsgActivity;

import java.util.List;

public class ZoneMsgAdapter extends BaseSwipeAdapter {
    private Context context;
    private LayoutInflater mInflater;
    private List<ZoneMessage> mData;
    private Handler handler;

    public ZoneMsgAdapter(Context context, List<ZoneMessage> mData,Handler handler) {
        super();
        this.context = context;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(context);
        this.handler = handler;
    }

    public List<ZoneMessage> getmData() {
        return mData;
    }

    public void setmData(List<ZoneMessage> mData) {
        this.mData = mData;
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
    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    @Override
    public View generateView(final int position, ViewGroup parent) {
        View v = LayoutInflater.from(context).inflate(R.layout.sc_item_zone_msg, null);
        SwipeLayout swipeLayout = (SwipeLayout) v.findViewById(getSwipeLayoutResourceId(position));
        swipeLayout.setShowMode(SwipeLayout.ShowMode.LayDown);
        swipeLayout.setDragEdge(SwipeLayout.DragEdge.Right);
        swipeLayout.findViewById(R.id.recent_del_btn).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        closeItem(position);
                        handler.postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                handler.obtainMessage(
                                        ZoneMsgActivity.POP_DEL_BTN,
                                        position).sendToTarget();
                            }
                        }, 200);
                    }
                });


        RelativeLayout rl_line = (RelativeLayout) v.findViewById(R.id.rl_line);

        ZoneMessage message = (ZoneMessage) getItem(position);

        if (message.getiHasRead() == 0) {
            rl_line.setBackgroundResource(R.color.list_item_selector_color);
        } else if (message.getiHasRead() == 1) {
            rl_line.setBackgroundResource(R.drawable.m_list_item_selector);
        }

        return v;
    }

    @Override
    public void fillValues(int postion, View converView) {
        ViewHolder holder = new ViewHolder();

        holder.photo = (ImageView) converView
                .findViewById(R.id.zone_msg_item_icon);
        holder.name = (TextView) converView
                .findViewById(R.id.zone_msg_item_name);
        holder.content = (TextView) converView
                .findViewById(R.id.zone_msg_item_content);
        holder.type = (ImageView) converView
                .findViewById(R.id.zone_msg_item_type_img);
        converView.setTag(holder);

        holder.flag = postion;
        ZoneMessage message = (ZoneMessage) getItem(postion);

        UserDao dao = UserDao.getInstance(context);
        UserInfo userInfo = dao.getUserInfoById(message.getcUserId());
        if (userInfo == null) {
            return;
        }
        // 1.评论,2.@,3.赞
        if ("1".equals(message.getiType())) {
            holder.type.setImageResource(R.drawable.zone_ico_comment);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + context.getString(R.string.commented_on_my_article));
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        } else if ("2".equals(message.getiType())) {
            holder.type.setImageResource(R.drawable.zone_ico_at_1);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + "@"+context.getString(R.string.at_me));
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        } else if ("3".equals(message.getiType())) {
            holder.type.setImageResource(R.drawable.zone_ico_like_normal);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + context.getString(R.string.approve_my_article));
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        } else {
            holder.type.setVisibility(View.GONE);
            SpannableString spanableInfo = new SpannableString(
                    userInfo.getEmpCname() + context.getString(R.string.approve_my_article));
            spanableInfo.setSpan(new ForegroundColorSpan(context.getResources()
                            .getColor(R.color.user_detail_content_blue_color)), 0,
                    userInfo.getEmpCname().length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.name.setText(spanableInfo);
        }

        holder.content.setText(DateUtils.getFormateDateWithTime(message
                .getdCreateTime()));

//        if (message.getiHasRead() == 0) {
//            converView.setBackgroundResource(R.color.list_item_selector_color);
//        } else if (message.getiHasRead() == 1) {
//            converView.setBackgroundResource(R.drawable.m_list_item_selector);
//        }
    }


    public final class ViewHolder {
        int flag;
        public ImageView photo;
        public TextView name;
        public TextView content;
        public ImageView type;
    }

}
