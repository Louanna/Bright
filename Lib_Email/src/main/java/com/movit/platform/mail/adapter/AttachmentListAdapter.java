package com.movit.platform.mail.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.mail.R;
import com.movit.platform.mail.mailstore.AttachmentViewInfo;
import com.movit.platform.mail.util.SizeFormatter;

import java.util.List;

/**
 * Created by Jamison on 2016/6/24.
 */
public class AttachmentListAdapter extends BaseAdapter {

    private final class ViewHolder {
        public TextView name;
        public TextView size;
        public ImageView type;
    }

    private Context context;
    private LayoutInflater mInflater;
    private List<AttachmentViewInfo> mData;

    public AttachmentListAdapter(Context context, List<AttachmentViewInfo> mData) {
        this.context = context;
        this.mData = mData;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mData.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return mData.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return arg0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.attachment_item, arg2,
                    false);
            holder.name = (TextView) convertView
                    .findViewById(R.id.txt_name);
            holder.size = (TextView) convertView
                    .findViewById(R.id.txt_size);
            holder.type = (ImageView) convertView
                    .findViewById(R.id.img_type);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.name.setText(mData.get(position).displayName);
        setAttachmentSize(holder.size, mData.get(position).size);
        setAttachmentTye(holder.type, mData.get(position).mimeType, mData.get(position).displayName);
        return convertView;
    }

    private void setAttachmentSize(TextView textView, long size) {
        if (size == AttachmentViewInfo.UNKNOWN_SIZE) {
            textView.setText("");
        } else {
            String text = SizeFormatter.formatSize(context, size);
            textView.setText(text);
        }
    }

    private void setAttachmentTye(ImageView imageView, String mimeType, String name) {
        if (mimeType != null) {
            String[] s = mimeType.split("[//// ]");
            if (s[0].equals("image")) {
                imageView.setImageResource(R.drawable.icon_pic);
                return;
            }
        }
        if (name != null) {
            String[] s = name.split("[.]");
            String suffix = s[s.length - 1];
            if (suffix.equals("pdf")) {
                imageView.setImageResource(R.drawable.icon_pdf);
            } else if (suffix.equals("ppt")) {
                imageView.setImageResource(R.drawable.icon_ppt);
            } else if (suffix.equals("word")) {
                imageView.setImageResource(R.drawable.icon_word);
            }else if(suffix.equals("jpg") || suffix.equals("png") || suffix.equals("jpeg")) {
                imageView.setImageResource(R.drawable.icon_pic);
            }
        }
    }
}
