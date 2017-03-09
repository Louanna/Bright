package com.movitech.EOP.module.events.awards;

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
import com.movitech.EOP.module.events.model.Awards;

import java.util.List;

class AwardsListAdapter extends BaseAdapter {

    private Context context;
    //奖项
    private List<Awards> awardsList;

    public AwardsListAdapter(Context context, List<Awards> awardsList) {
        this.context = context;
        this.awardsList = awardsList;
    }

    @Override
    public int getCount() {
        return awardsList.size();
    }

    @Override
    public Object getItem(int i) {
        return awardsList.get(i);
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
                    R.layout.item_activities_awards, null);

            holder.iv_awards_icon = (ImageView) view
                    .findViewById(R.id.iv_awards_icon);
            holder.tv_awards_name = (TextView) view
                    .findViewById(R.id.tv_awards_name);
            holder.tv_prize_name = (TextView) view
                    .findViewById(R.id.tv_prize_name);
            holder.tv_awards_status = (TextView) view
                    .findViewById(R.id.tv_awards_status);
            holder.tv_awards_num = (TextView) view
                    .findViewById(R.id.tv_awards_num);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Awards awards = awardsList.get(i);

        holder.iv_awards_icon.setBackground(context.getResources().getDrawable(R.drawable.icon_awards_1));
        holder.tv_awards_name.setText(awards.getAwardsName());
        holder.tv_prize_name.setText(awards.getPrizeName());

        if ("0".equalsIgnoreCase(awards.getStatus())) {
            holder.tv_awards_status.setText(context.getResources().getString(R.string.unlucky));
            holder.tv_awards_status.setTextColor(context.getResources().getColor(R.color.text_color_2));
        } else if ("1".equalsIgnoreCase(awards.getStatus())) {
            holder.tv_awards_status.setText(context.getResources().getString(R.string.lucky));
            holder.tv_awards_status.setTextColor(context.getResources().getColor(R.color.red));
        }

        holder.tv_awards_num.setText(awards.getNum());

        return view;
    }

    private static class ViewHolder {
        ImageView iv_awards_icon;
        TextView tv_awards_name;
        TextView tv_prize_name;
        TextView tv_awards_status;
        TextView tv_awards_num;
    }
}
