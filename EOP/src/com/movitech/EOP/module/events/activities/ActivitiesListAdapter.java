package com.movitech.EOP.module.events.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.events.model.Activities;

import java.util.List;

class ActivitiesListAdapter extends BaseAdapter {

    private Context context;
    //活动
    private List<Activities> activitiesList;

    public ActivitiesListAdapter(Context context, List<Activities> activitiesList) {
        this.context = context;
        this.activitiesList = activitiesList;
    }

    @Override
    public int getCount() {
        return activitiesList.size();
    }

    @Override
    public Object getItem(int i) {
        return activitiesList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (null == view) {
            holder = new ViewHolder();
            view = LayoutInflater.from(context).inflate(
                    R.layout.item_activities, null);

            holder.iv_activities_icon = (ImageView) view
                    .findViewById(R.id.iv_activities_icon);
            holder.tv_activities_name = (TextView) view
                    .findViewById(R.id.tv_activities_name);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.iv_activities_icon.setBackground(context.getResources().getDrawable(R.drawable.icon_activities));
        holder.tv_activities_name.setText(activitiesList.get(i).getName());

        return view;
    }

    private static class ViewHolder {
        ImageView iv_activities_icon;
        TextView tv_activities_name;
    }
}
