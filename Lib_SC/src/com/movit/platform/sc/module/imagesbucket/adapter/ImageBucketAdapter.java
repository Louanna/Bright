package com.movit.platform.sc.module.imagesbucket.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.sc.R;
import com.movit.platform.sc.module.imagesbucket.entities.ImageBucket;

import java.io.File;
import java.util.List;

public class ImageBucketAdapter extends BaseAdapter {
    final String TAG = getClass().getSimpleName();
    Activity act;
    /**
     * 图片集列表
     */
    List<ImageBucket> dataList;
    AQuery aQuery;

    public ImageBucketAdapter(Activity act, List<ImageBucket> list) {
        this.act = act;
        dataList = list;
        aQuery = new AQuery(act);
    }

    @Override
    public int getCount() {
        int count = 0;
        if (dataList != null) {
            count = dataList.size();
        }
        return count + 1;
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    class Holder {
        private ImageView iv;
        private TextView name;
        private TextView count;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            holder = new Holder();
            convertView = View.inflate(act, R.layout.sc_item_image_bucket, null);
            holder.iv = (ImageView) convertView.findViewById(R.id.bucket_image);
            holder.name = (TextView) convertView.findViewById(R.id.bucket_name);
            holder.count = (TextView) convertView.findViewById(R.id.bucket_count);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        AQuery aq = aQuery.recycle(convertView);
        if (position == 0) {
            aq.id(R.id.bucket_image).image(R.drawable.zone_icon_camera);
            holder.count.setText("");
            holder.name.setText(act.getString(R.string.photo));
        } else {
            ImageBucket item = dataList.get(position - 1);
            holder.count.setText("(" + item.count + ")");
            holder.name.setText(item.bucketName);

            if (item.imageList != null && item.imageList.size() > 0) {
                String thumbPath = item.imageList.get(0).thumbnailPath;
                final String sourcePath = item.imageList.get(0).imagePath;
                holder.iv.setTag(sourcePath);
                if (aq.shouldDelay(position - 1, convertView, parent, "")) {
                    aq.id(holder.iv).image(R.drawable.zone_pic_default);
                } else {
                    BitmapAjaxCallback callback = new BitmapAjaxCallback();
                    callback.animation(AQuery.FADE_IN);
                    callback.rotate(true);
                    aq.id(holder.iv).image(new File(sourcePath), false,
                            256, callback);
                }
            } else {
                holder.iv.setImageBitmap(null);
            }
        }
        return convertView;
    }

}
