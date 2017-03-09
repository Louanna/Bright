package com.movit.platform.common.module.organization.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.movit.platform.common.R;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationBean;
import com.movit.platform.common.module.user.activity.UserDetailActivity;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.widget.CircleImageView;

import java.util.List;

/**
 * Created by air on 16/9/21.
 *
 */
public class Org2Adapter extends BaseAdapter{

    private List<OrganizationBean> datas;
    private Context mContext;
    private LayoutInflater inflater;

    public Org2Adapter(List<OrganizationBean> bean, Context context){
        this.datas = bean;
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(
                    R.layout.comm_item_organization2, null);
            holder = new ViewHolder();
            holder.avatar = (CircleImageView) convertView
                    .findViewById(R.id.user_avatar);
            holder.groupValue1 = (TextView) convertView
                    .findViewById(R.id.item_group_value1);
            holder.groupValue2 = (TextView) convertView
                    .findViewById(R.id.item_group_value2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        OrganizationBean bean = datas.get(position);
        final UserInfo userInfo = bean.getUserInfo();
        if (userInfo != null) {
            int picId = R.drawable.avatar_male;
            if (mContext.getString(R.string.boy).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if (mContext.getString(R.string.girl).equals(userInfo.getGender())) {
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
                MFAQueryHelper.setImageView(holder.avatar,CommConstants.URL_DOWN + avatarUrl,picId);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId,
                        10);
                holder.avatar.setImageBitmap(bitmap);
            }

            holder.groupValue1.setText(userInfo.getEmpCname());
            if(StringUtils.notEmpty(userInfo.getPosition())){
                if(StringUtils.notEmpty(bean.getDepName())){
                    holder.groupValue2.setText("["+userInfo.getPosition()+"]"+bean.getObjName()+"/"+bean.getDepName());
                }else{
                    holder.groupValue2.setText("["+userInfo.getPosition()+"]"+bean.getObjName());
                }

            }else{
                if(StringUtils.notEmpty(bean.getDepName())){
                    holder.groupValue2.setText(bean.getObjName()+"/"+bean.getDepName());
                }else{
                    holder.groupValue2.setText(bean.getObjName());
                }
            }

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(mContext,
                            UserDetailActivity.class);
                    intent.putExtra("userInfo", userInfo);
                    mContext.startActivity(intent);
                }
            });

        }

        return convertView;
    }

    class ViewHolder {
        CircleImageView avatar;
        TextView groupValue1;
        TextView groupValue2;
    }
}
