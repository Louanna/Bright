package com.movitech.EOP.module.workbench.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.workbench.model.News;

import java.util.ArrayList;

/**
 * Created by air on 16/9/27.
 *
 */

public class CompanyNewsAdapter extends BaseAdapter {

    private ArrayList<News> datas;
    private LayoutInflater inflater;
    private Context mContext;

    public CompanyNewsAdapter(ArrayList<News> bean, Context context){
        this.datas = bean;
        this.mContext = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public Object getItem(int position) {
        return datas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = inflater.inflate(
                    R.layout.item_news_list,parent ,false);
            holder = new ViewHolder();
            holder.title = (TextView) convertView.findViewById(R.id.tv_news_title);
            holder.time = (TextView) convertView.findViewById(R.id.tv_news_time);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.title.setText(datas.get(position).getTitle());
        holder.time.setText(datas.get(position).getTime());

        return convertView;
    }

    class ViewHolder {
        TextView title;
        TextView time;
    }
}
