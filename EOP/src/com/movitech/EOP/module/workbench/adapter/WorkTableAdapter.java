package com.movitech.EOP.module.workbench.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.utils.ScreenUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.workbench.model.WorkTable;

import java.util.List;
import java.util.Map;


/**
 * Created by air on 16/9/21.
 *
 */
public class WorkTableAdapter extends BaseAdapter{

    private List<WorkTable> datas;
    private LayoutInflater inflater;
    private Context mContext;

    public WorkTableAdapter(List<WorkTable> bean, Context context){
        this.datas = bean;
        this.mContext = context;
        inflater = LayoutInflater.from(context);
    }

    private Map<String, Integer> unReadNum;

    public void setUnreadNum(Map<String, Integer> unReadNum) {

        this.unReadNum = unReadNum;
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
                    R.layout.work_table_gridview_item2,parent ,false);
            holder = new ViewHolder();
            holder.img = (ImageView) convertView
                    .findViewById(R.id.gridview_item_img);
            holder.dian = (TextView) convertView
                    .findViewById(R.id.gridview_item_dian);
            holder.name = (TextView) convertView
                    .findViewById(R.id.gridview_item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final WorkTable table = datas.get(position);
        //增加未读数提醒
        if (null != unReadNum && unReadNum.size() > 0 && unReadNum.containsKey(table.getAndroid_access_url())
                && unReadNum.get(table.getAndroid_access_url()) > 0) {
            if (unReadNum.get(table.getAndroid_access_url()) > 99) {
                String num = "99+";
                holder.dian.setText(num);
            } else {
                holder.dian.setText(String.valueOf(unReadNum.get(table.getAndroid_access_url())));
            }
            holder.dian.setVisibility(View.VISIBLE);
        } else {
            holder.dian.setVisibility(View.GONE);
        }

        holder.name.setText(table.getName());

        MFAQueryHelper.setImageView(holder.img, CommConstants.URL_DOWN + table.getPicture(), R.drawable.zone_pic_default);


        AbsListView.LayoutParams param = new AbsListView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (ScreenUtils.getScreenHeight(mContext)-mContext.getResources().getDimensionPixelOffset(R.dimen.dp_66))/3);
        convertView.setLayoutParams(param);

        return convertView;
    }

    class ViewHolder {
        ImageView img;
        TextView dian;
        TextView name;
    }
}
