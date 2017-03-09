package com.movitech.EOP.module.events.winners;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.events.model.Winners;

import java.util.List;

class WinnersListAdapter extends BaseAdapter {

    private Context context;
    //中奖人
    private List<Winners> winnersList;

    public WinnersListAdapter(Context context, List<Winners> winnersList) {
        this.context = context;
        this.winnersList = winnersList;
    }

    @Override
    public int getCount() {
        return winnersList.size();
    }

    @Override
    public Object getItem(int i) {
        return winnersList.get(i);
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
                    R.layout.item_winners, null);

            holder.tv_name = (TextView) view
                    .findViewById(R.id.tv_name);
            holder.tv_phone = (TextView) view
                    .findViewById(R.id.tv_phone);
            holder.tv_department = (TextView) view
                    .findViewById(R.id.tv_department);

            holder.tv_name.setTextColor(context.getResources().getColor(R.color.text_color_2));
            holder.tv_phone.setTextColor(context.getResources().getColor(R.color.text_color_2));
            holder.tv_department.setTextColor(context.getResources().getColor(R.color.text_color_2));

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        Winners winners = winnersList.get(i);
        holder.tv_name.setText(winners.getName());
        holder.tv_phone.setText(winners.getPhone());
        holder.tv_department.setText(winners.getDepartment());

        String curUserId = MFSPHelper.getString(CommConstants.EMPADNAME);

        if(curUserId.equalsIgnoreCase(winners.getLoginName())){
            //登录用户为中奖者则高亮显示
            holder.tv_name.setTextColor(context.getResources().getColor(R.color.text_color_1));
            holder.tv_phone.setTextColor(context.getResources().getColor(R.color.text_color_1));
            holder.tv_department.setTextColor(context.getResources().getColor(R.color.text_color_1));
        }else{
            holder.tv_name.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.tv_phone.setTextColor(context.getResources().getColor(android.R.color.black));
            holder.tv_department.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        return view;
    }

    private static class ViewHolder {
        TextView tv_name;
        TextView tv_phone;
        TextView tv_department;
    }
}
