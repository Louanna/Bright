package com.movitech.EOP.module.workbench.adapter;

import java.io.File;
import java.util.List;

import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movitech.EOP.Test.R;

public class SuggestionsGridAdapter extends BaseAdapter {
    private LayoutInflater mInflater;
    private List<Object> images;
    private boolean isDeling = false;
    AQuery aq;
    Handler handler;
    boolean disableListener = false;

    public SuggestionsGridAdapter(Context context, List<Object> images,
                                  Handler handler) {
        super();
        this.images = images;
        this.mInflater = LayoutInflater.from(context);
        aq = new AQuery(context);
        this.handler = handler;
    }

    public boolean isDeling() {
        return isDeling;
    }

    public void setDeling(boolean isDeling) {
        this.isDeling = isDeling;
    }

    public void setDisableListener(boolean disableListener) {
        this.disableListener = disableListener;
    }

    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public Object getItem(int arg0) {
        return images.get(arg0);
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
            converView = mInflater.inflate(R.layout.group_gridview_image, arg2,
                    false);
            holder.photo = (ImageView) converView
                    .findViewById(R.id.gridview_item_img);
            holder.delImg = (ImageView) converView
                    .findViewById(R.id.gridview_item_delImg);
            holder.grid_rl = (RelativeLayout) converView
                    .findViewById(R.id.grid_rl);
            converView.setTag(holder);
        } else {
            holder = (ViewHolder) converView.getTag();
        }

        AQuery aQuery = aq.recycle(converView);
        Object object = images.get(postion);
        holder.delImg.setVisibility(View.GONE);
        holder.photo.setScaleType(ScaleType.CENTER_CROP);
        if (object instanceof Integer) {
            aQuery.id(holder.photo).image((Integer) object);
        } else if (object instanceof String) {
            BitmapAjaxCallback callback = new BitmapAjaxCallback();
            callback.animation(AQuery.FADE_IN_FILE);
            callback.rotate(true);
            aQuery.id(holder.photo).image(new File((String) object), true, 256,
                    callback);
        }
        holder.photo.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(3, postion).sendToTarget();
            }
        });
        if (isDeling) {
            if (object instanceof Integer) {
                holder.delImg.setVisibility(View.GONE);
            } else {
                holder.delImg.setVisibility(View.VISIBLE);
            }
        } else {
            holder.delImg.setVisibility(View.GONE);
        }
        holder.delImg.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                handler.obtainMessage(2, postion).sendToTarget();
            }

        });

        return converView;
    }

    public final class ViewHolder {
        public RelativeLayout grid_rl;
        public ImageView photo;
        public ImageView delImg;
    }

}
