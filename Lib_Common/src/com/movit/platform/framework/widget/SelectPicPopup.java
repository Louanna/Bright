package com.movit.platform.framework.widget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.movit.platform.common.R;

/**
 * @author: Tao Yangjun
 * @En_Name: Potter Tao
 * @E-mail: wudayu@gmail.com
 * @version: 1.0
 * @Created Time: Jun 19, 2014 13:48:10 PM
 * @Description: This is Potter Tao's property.
 **/
public class SelectPicPopup extends PopupWindow {

    private RelativeLayout rl_take_txt, rl_btn_del, rl_take_camera,
            rl_pick_photo;
    private Button btn_take_txt, btn_take_photo, btn_pick_photo, btn_cancel,
            btn_pick_attchment, btn_del;
    private View mMenuView;

    public SelectPicPopup(Activity context, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.comm_popup_photo_picker, null);
        rl_take_txt = (RelativeLayout) mMenuView.findViewById(R.id.rl_take_txt);
        rl_btn_del = (RelativeLayout) mMenuView.findViewById(R.id.rl_btn_del);
        rl_take_camera = (RelativeLayout) mMenuView
                .findViewById(R.id.rl_take_camera);
        rl_pick_photo = (RelativeLayout) mMenuView
                .findViewById(R.id.rl_pick_photo);
        btn_del = (Button) mMenuView.findViewById(R.id.btn_del);
        btn_take_txt = (Button) mMenuView.findViewById(R.id.btn_take_txt);
        btn_take_photo = (Button) mMenuView.findViewById(R.id.btn_take_photo);
        btn_pick_photo = (Button) mMenuView.findViewById(R.id.btn_pick_photo);
        btn_pick_attchment = (Button) mMenuView
                .findViewById(R.id.btn_pick_attchment);
        btn_cancel = (Button) mMenuView.findViewById(R.id.btn_cancel);
        rl_take_txt.setVisibility(View.GONE);
        rl_btn_del.setVisibility(View.GONE);

        // 取消按钮
        btn_cancel.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                // 销毁弹出框
                dismiss();
            }
        });
        // 设置按钮监听
        btn_take_txt.setOnClickListener(itemsOnClick);
        btn_pick_photo.setOnClickListener(itemsOnClick);
        btn_take_photo.setOnClickListener(itemsOnClick);
        btn_pick_attchment.setOnClickListener(itemsOnClick);
        btn_del.setOnClickListener(itemsOnClick);
        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // 设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.popwindown_animstyle);
        // 实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        // 设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });
    }

    public void showDel() {
        rl_take_camera.setVisibility(View.GONE);
        rl_pick_photo.setVisibility(View.GONE);
        rl_btn_del.setVisibility(View.VISIBLE);
    }

}
