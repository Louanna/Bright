package com.movit.platform.framework.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.movit.platform.common.R;

/**
 * Created by Administrator on 2016/1/5.
 */
public class PopupUtils {

    public static void showPopupWindow(final Context mContext, final View view, final String copyStr, final PopupClickEvent popupClickEvent) {

        // 一个自定义的布局，作为显示的内容
        View popupView = LayoutInflater.from(mContext).inflate(R.layout.comm_pop_copy, null);

        final PopupWindow popupWindow = new PopupWindow(popupView,
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

        popupWindow.setTouchable(true);

        popupWindow.setTouchInterceptor(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                return false;
                // 这里如果返回true的话，touch事件将被拦截
                // 拦截后 PopupWindow的onTouchEvent不被调用，这样点击外部区域无法dismiss
            }
        });

        // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
        // 这是API的一个bug
        popupWindow.setBackgroundDrawable(new BitmapDrawable());

        // 设置好参数之后再show
        popupView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        int popupWidth = popupView.getMeasuredWidth();
        int popupHeight = popupView.getMeasuredHeight();

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        popupWindow.showAtLocation(view, Gravity.NO_GRAVITY, (location[0] + view.getWidth() / 2) - popupWidth / 2,
                location[1] - popupHeight);

        // 设置按钮的点击事件
        TextView tv_copy = (TextView) popupView.findViewById(R.id.tv_copy);
        tv_copy.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                ClipboardManager myClipboard = (ClipboardManager) mContext.getSystemService(mContext.CLIPBOARD_SERVICE);
                ClipData myClip = ClipData.newPlainText("text", copyStr);
                myClipboard.setPrimaryClip(myClip);

                popupWindow.dismiss();
            }
        });

        if(null!=copyStr){
            tv_copy.setVisibility(View.VISIBLE);
        }else{
            tv_copy.setVisibility(View.GONE);
        }

        // 设置按钮的点击事件
        final TextView tv_resend = (TextView) popupView.findViewById(R.id.tv_resend);
        tv_resend.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
                popupClickEvent.onPopupItemClicked(tv_resend);
            }
        });
    }

    public interface PopupClickEvent{
        public void onPopupItemClicked(TextView tv);
    }

}
