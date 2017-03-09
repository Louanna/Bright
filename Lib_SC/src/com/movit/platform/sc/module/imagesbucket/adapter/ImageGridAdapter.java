package com.movit.platform.sc.module.imagesbucket.adapter;

import java.io.File;
import java.util.List;

import android.app.Activity;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.sc.R;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;
import com.movit.platform.sc.module.imagesbucket.entities.ImageItem;
import com.movit.platform.sc.module.imagesbucket.activity.PicGridPickedActivity;

public class ImageGridAdapter extends BaseAdapter {

    private TextCallback textcallback = null;
    final String TAG = getClass().getSimpleName();
    Activity act;
    List<ImageItem> dataList;
    private Handler mHandler;

    ColorMatrix cMatrix_select = new ColorMatrix(new float[]{1, 0, 0, 0, -50,
            0, 1, 0, 0, -50, 0, 0, 1, 0, -50, 0, 0, 0, 1, 0});
    ColorMatrix cMatrix_no_select = new ColorMatrix(new float[]{1, 0, 0, 0,
            0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0});

    AQuery aq;

    public static interface TextCallback {
        public void onListen(int count);
    }

    public void setTextCallback(TextCallback listener) {
        textcallback = listener;
    }

    public ImageGridAdapter(Activity act, List<ImageItem> list, Handler mHandler) {
        this.act = act;
        dataList = list;
        this.mHandler = mHandler;
        aq = new AQuery(act);
    }

    @Override
    public int getCount() {
        int count = 0;
        if (dataList != null) {
            count = dataList.size();
        }
        return count;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    class Holder {
        private int flag;
        private ImageView iv;
        private CheckBox selected;
        private TextView text;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final Holder holder;

        if (convertView == null
                || ((Holder) convertView.getTag()).flag != position) {
            holder = new Holder();
            convertView = View.inflate(act, R.layout.sc_item_image_grid, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.image);
            holder.selected = (CheckBox) convertView
                    .findViewById(R.id.isselected);
            holder.text = (TextView) convertView
                    .findViewById(R.id.item_image_grid_text);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        holder.flag = position;

        final ImageItem item = dataList.get(position);
        AQuery aQuery = aq.recycle(convertView);

        holder.iv.setTag(item.imagePath);

        if (aQuery.shouldDelay(position, convertView, parent, "")) {
            aQuery.id(holder.iv).image(R.drawable.zone_pic_default);
        } else {
            BitmapAjaxCallback callback = new BitmapAjaxCallback();
            callback.animation(AQuery.FADE_IN);
            callback.rotate(true);
            aQuery.id(holder.iv).image(new File(item.imagePath), false, 256,
                    callback);
        }

        if (ZonePublishActivity.selectImagesList.contains(item.imagePath)) {
            item.isSelected = true;
            holder.selected.setChecked(true);
            holder.text.setBackgroundResource(R.drawable.m_image_stroke_line);
            holder.iv
                    .setColorFilter(new ColorMatrixColorFilter(cMatrix_select));
        } else {
            item.isSelected = false;
            holder.selected.setChecked(false);
            holder.text.setBackgroundColor(0x00000000);
        }

        if (textcallback != null
                && !ZonePublishActivity.selectImagesList.isEmpty())
            textcallback.onListen(ZonePublishActivity.selectImagesList.size());

        holder.iv.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String path = dataList.get(position).imagePath;
                // 总数<9
                if ((ZonePublishActivity.selectImagesList.size()) < PicGridPickedActivity.CHOOSE_PICS_COUNT) {
                    if (item.isSelected) {
                        holder.selected.setChecked(false);
                        holder.text.setBackgroundColor(0x00000000);
                        ZonePublishActivity.selectImagesList.remove(path);
                        if (textcallback != null)
                            textcallback
                                    .onListen(ZonePublishActivity.selectImagesList
                                            .size());

                        item.isSelected = false;
                        holder.iv.setColorFilter(new ColorMatrixColorFilter(
                                cMatrix_no_select));
                    } else {
                        holder.selected.setChecked(true);
                        holder.text
                                .setBackgroundResource(R.drawable.m_image_stroke_line);
                        ZonePublishActivity.selectImagesList.add(path);
                        if (textcallback != null)
                            textcallback
                                    .onListen(ZonePublishActivity.selectImagesList
                                            .size());
                        item.isSelected = true;
                        holder.iv.setColorFilter(new ColorMatrixColorFilter(
                                cMatrix_select));
                    }

                } else if ((ZonePublishActivity.selectImagesList.size()) >= PicGridPickedActivity.CHOOSE_PICS_COUNT) {
                    if (item.isSelected) {
                        holder.selected.setChecked(false);
                        holder.text.setBackgroundColor(0x00000000);
                        ZonePublishActivity.selectImagesList.remove(path);
                        if (textcallback != null)
                            textcallback
                                    .onListen(ZonePublishActivity.selectImagesList
                                            .size());
                        item.isSelected = false;
                        holder.iv.setColorFilter(new ColorMatrixColorFilter(
                                cMatrix_no_select));
                    } else {
                        Message message = Message.obtain(mHandler, 0);
                        message.sendToTarget();
                    }
                }
            }

        });

        return convertView;
    }

}
