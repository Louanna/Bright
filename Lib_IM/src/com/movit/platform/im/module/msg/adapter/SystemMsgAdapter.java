package com.movit.platform.im.module.msg.adapter;

import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.im.R;

public class SystemMsgAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<MessageBean> mData;
    AQuery aq;
    private Context context;

    public SystemMsgAdapter(Context context, List<MessageBean> mData) {
        super();
        this.mData = mData;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        aq = new AQuery(context);
    }

    public List<MessageBean> getmData() {
        return mData;
    }

    public void setmData(List<MessageBean> mData) {
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
    public View getView(final int postion, View converView, ViewGroup arg2) {
        ViewHolder holder = null;
        if (converView == null) {
            holder = new ViewHolder();
            converView = mInflater.inflate(R.layout.im_item_system_msg, arg2,
                    false);
            holder.name = (TextView) converView
                    .findViewById(R.id.system_msg_item_name);
            holder.type = (TextView) converView
                    .findViewById(R.id.system_msg_item_type);
            holder.photo = (ImageView) converView
                    .findViewById(R.id.system_msg_item_icon);
            holder.content = (TextView) converView
                    .findViewById(R.id.system_msg_item_content);
            holder.time = (TextView) converView
                    .findViewById(R.id.system_msg_item_time);
            holder.option = (TextView) converView
                    .findViewById(R.id.system_msg_item_option);
            converView.setTag(holder);
        } else {
            holder = (ViewHolder) converView.getTag();
        }
        AQuery aQuery = aq.recycle(converView);
        MessageBean bean = (MessageBean) getItem(postion);
        holder.option.setVisibility(View.GONE);
        holder.type.setText(context.getString(R.string.notification));
        if (bean.getMtype().equals(CommConstants.MSG_TYPE_DISSOLVE)) {
            holder.content.setText(context.getString(R.string.admin_dissolve_success));
        } else if (bean.getMtype().equals(CommConstants.MSG_TYPE_INVITE)) {
            holder.content.setText(bean.getFriendId() + context.getString(R.string.invite_join_group));
            holder.option.setText(context.getString(R.string.agreed));
            holder.option.setVisibility(View.VISIBLE);
        } else if (bean.getMtype().equals(CommConstants.MSG_TYPE_KICK)) {
            holder.content.setText(context.getString(R.string.admin_removed_you));
        } else if (bean.getMtype().equals(CommConstants.MSG_TYPE_MEMBERS_CHANGE)) {
            /**
             * 0表示新增成员通知; 1表示踢出成员通知; 2表示变更displayName通知; 3解散群通知;4用户退群通知
             */
            if (bean.getRsflag() == 0) {
                holder.content.setText(bean.getFriendId() + context.getString(R.string.join_group));
            } else if (bean.getRsflag() == 1) {
                holder.content.setText(bean.getFriendId() + context.getString(R.string.beremoved_from_group));
            } else if (bean.getRsflag() == 4) {
                holder.content.setText(bean.getFriendId() + context.getString(R.string.exitd_group));
            }
        }

        holder.time.setText(DateUtils.getFormateDateWithTime(bean.getFormateTime()));

        aQuery.id(holder.photo).image(R.drawable.group_default);
        try {
            holder.name.setText(bean.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return converView;
    }

    public final class ViewHolder {
        public TextView type;
        public TextView time;
        public ImageView photo;
        public TextView name;
        public TextView content;
        public TextView option;
    }

}
