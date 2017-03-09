package com.movit.platform.im.widget;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.movit.platform.im.R;

/**
 * Created by Administrator on 2015/11/23.
 */
public class CurPopup extends PopupWindow {

    public CurPopup(View contentView, PopupListener popupListener, Context context) {
        super(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        setFocusable(false);
        setOutsideTouchable(true);
        setBackgroundDrawable(new BitmapDrawable());

        initPopup(contentView, popupListener, context);
    }

    private void initPopup(View contentView, final PopupListener popupListener, Context context) {

        LinearLayout group_add = (LinearLayout) contentView
                .findViewById(R.id.pop_linearlayout_1);
        ImageView imageView1 = (ImageView) contentView
                .findViewById(R.id.pop_imageview_1);
        TextView textView1 = (TextView) contentView
                .findViewById(R.id.pop_textview_1);
        LinearLayout email_add = (LinearLayout) contentView
                .findViewById(R.id.pop_linearlayout_2);
        ImageView imageView2 = (ImageView) contentView
                .findViewById(R.id.pop_imageview_2);
        TextView textView2 = (TextView) contentView
                .findViewById(R.id.pop_textview_2);
        // TODO: 2016/3/3 meeting
        LinearLayout meeting_add = (LinearLayout) contentView
                .findViewById(R.id.pop_linearlayout_3);
        ImageView imageView3 = (ImageView) contentView
                .findViewById(R.id.pop_imageview_3);
        TextView textView3 = (TextView) contentView
                .findViewById(R.id.pop_textview_3);
        View cuttingline =  contentView.findViewById(R.id.cuttingline);

        imageView1.setImageResource(R.drawable.ico_group1);
        textView1.setText(context.getResources().getString(R.string.start_a_group_chat));
        imageView2.setImageResource(R.drawable.ico_group2);
        textView2.setText(context.getResources().getString(R.string.nick_group_chat));

        boolean meeting = false;
        try {
            meeting = context.getPackageManager().getApplicationInfo(
                    context.getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getBoolean(
                    "CHANNEL_MEETING", false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (meeting) {
            imageView3.setImageResource(R.drawable.icon_meeting);
            textView3.setText(context.getResources().getString(R.string.video_conference));
        } else {
            cuttingline.setVisibility(View.GONE);
            meeting_add.setVisibility(View.GONE);
        }

        group_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    dismiss();
                }
                popupListener.onWindowItemClickListener(v.getId());
            }
        });
        email_add.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    dismiss();
                }
                popupListener.onWindowItemClickListener(v.getId());
            }
        });
        meeting_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isShowing()) {
                    dismiss();
                }
                popupListener.onWindowItemClickListener(v.getId());
            }
        });
    }

    public interface PopupListener {
        public void onWindowItemClickListener(int viewId);
    }
}


