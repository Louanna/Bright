package com.movitech.EOP.module.workbench.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.workbench.model.ProcessModel;

import java.util.ArrayList;

/**
 * Created by air on 16/10/19.
 * 待办,已办数据适配
 */

public class ProcessAdapter extends BaseAdapter {

    public static String TODO = "TODO";
    public static String DONE = "DONE";

    private Context context;
    private LayoutInflater inflater;
    private String isDo;
    private AQuery aq;

    private ArrayList<ProcessModel> dataList;

    public ProcessAdapter(Context mContext,ArrayList<ProcessModel> dataList) {
        this.context = mContext;
        this.dataList = dataList;
        aq = new AQuery(context);
        inflater = LayoutInflater.from(context);
    }

    public void setIsDo(String isDo){
        this.isDo = isDo;
    }


    public ArrayList<ProcessModel> getDataList(){
        return dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
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
                    R.layout.item_process,parent ,false);
            holder = new ViewHolder();
            holder.ivCircle = (ImageView) convertView
                    .findViewById(R.id.iv_circle);
            holder.img = (ImageView) convertView
                    .findViewById(R.id.user_avatar);
            holder.title = (TextView) convertView
                    .findViewById(R.id.tv_process_title);
            holder.time = (TextView) convertView
                    .findViewById(R.id.tv_process_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if(isDo.equals(DONE)){
            holder.ivCircle.setImageResource(R.drawable.process_gou);
        }else {
            holder.ivCircle.setImageResource(R.drawable.process_point);
        }

        ProcessModel processModel = (ProcessModel)getItem(position);
        holder.title.setText(processModel.getTitle());
        holder.time.setText(processModel.getProcessArriveTime());

        UserInfo userInfo = CommConstants.loginConfig.getmUserInfo();
        if (userInfo != null) {
            String avatar = userInfo.getAvatar();
            String gender = userInfo.getGender();
            int picId = com.movit.platform.common.R.drawable.avatar_male;
            if (!TextUtils.isEmpty(gender) && "女".equals(gender)) {
                picId = com.movit.platform.common.R.drawable.avatar_female;
            }
            if (!TextUtils.isEmpty(avatar) && !avatar.equals("null")) {
                AQuery aQuery = aq.recycle(convertView);
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatar).memCache(true)
                        .fileCache(true).targetWidth(128);
                aQuery.id(holder.img).image(callback);
            } else {
                holder.img.setImageResource(picId);
            }
        } else {
            holder.img.setImageResource(com.movit.platform.mail.R.drawable.avatar_email);
        }

        return convertView;
    }

    class ViewHolder {
        ImageView img;
        ImageView ivCircle;
        TextView title;
        TextView time;
    }
}
