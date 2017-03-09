package com.movit.platform.im.module.group.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.module.group.entities.Group;

import java.io.File;
import java.util.List;

public class GroupListAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Group> mData;
    private AQuery aq;
    private  Context context;

    public GroupListAdapter(Context context, List<Group> mData) {
        super();
        this.mData = mData;
        this.context = context;
        this.mInflater = LayoutInflater.from(context);
        aq = new AQuery(context);
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
            converView = mInflater.inflate(R.layout.im_item_group, arg2,
                    false);
            holder.name = (TextView) converView
                    .findViewById(R.id.group_item_name);
            holder.type = (TextView) converView
                    .findViewById(R.id.group_item_type);
            holder.photo = (ImageView) converView
                    .findViewById(R.id.group_item_icon);
            holder.content = (TextView) converView
                    .findViewById(R.id.group_item_content);
            holder.msgUnReadNum = (TextView) converView
                    .findViewById(R.id.group_item_msgUnReadNum);
            holder.pending = (ImageView) converView
                    .findViewById(R.id.group_item_pending);
            converView.setTag(holder);
        } else {
            holder = (ViewHolder) converView.getTag();
        }
        AQuery aQuery = aq.recycle(converView);
        Group group = (Group) getItem(postion);
        // 0管理员创建群组 1部门群组 2任务群组 3个人群组
        switch (group.getType()) {
            case 0:
                holder.type.setText(context.getString(R.string.admin_define));
                aQuery.id(holder.photo).image(R.drawable.group_admin);
                break;
            case 1:
                holder.type.setText(context.getString(R.string.development));
                aQuery.id(holder.photo).image(R.drawable.group_org);
                break;
            case 2:
                holder.type.setText(context.getString(R.string.bussiness));
                aQuery.id(holder.photo).image(R.drawable.group_task);
                break;
            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                holder.type.setText(context.getString(R.string.personal));
                File file = new File(CommConstants.SD_DATA_PIC+group.getId()+"_temp.jpg");
                if(file.exists()){
                    aQuery.id(holder.photo).image(file,256);
                }else{
                    aQuery.id(holder.photo).image(R.drawable.group_personal);
                }
//                aQuery.id(holder.photo).image(R.drawable.group_personal);
                break;
            case CommConstants.CHAT_TYPE_GROUP_ANS:
                holder.type.setText(context.getString(R.string.nick));
                aQuery.id(holder.photo).image(R.drawable.group_ans);
                break;
            default:
                break;
        }
        holder.name.setText(group.getDisplayName());
        List<UserInfo> members = group.getMembers();
        String cnames = "";
        for (int i = 0; i < members.size(); i++) {
            cnames += members.get(i).getEmpCname().split("\\.")[0] + "、";
            if (i == 2) {
                break;
            }
        }

        if (StringUtils.notEmpty(cnames)){
            holder.content.setText(cnames.subSequence(0, cnames.length() - 1) + "等"
                    + members.size() + "人");
        }

        holder.msgUnReadNum.setVisibility(View.GONE);
        return converView;
    }

    public final class ViewHolder {
        public ImageView photo;
        public TextView name;
        public TextView type;
        public TextView content;
        public TextView msgUnReadNum;
        public ImageView pending;
    }

}
