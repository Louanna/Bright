package com.movit.platform.contacts.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.contacts.R;
import com.movit.platform.contacts.fragment.ContactsFragment;
import com.movit.platform.framework.helper.MFSPHelper;

public class ContactsActivity extends FragmentActivity {

    private PopupWindow popupWindow;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);
        initViews();
    }

    protected void initViews() {
        ImageView back = (ImageView) findViewById(R.id.common_top_left);
        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(R.string.contacts);
        back.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        ImageView groupAdd = (ImageView) findViewById(R.id.common_top_right);

        groupAdd.setImageResource(R.drawable.icon_add);
        groupAdd.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getPopupWindow();
                if (popupWindow.isShowing()) {
                    popupWindow.dismiss();
                } else {
                    popupWindow.showAsDropDown(v);
                }
            }
        });
        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        FragmentManager fragmentManager = this.getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(R.id.common_fragment, new ContactsFragment(),
                "UserFragment");
        transaction.commitAllowingStateLoss();
    }

    private void getPopupWindow() {
        if (null != popupWindow) {
            return;
        } else {
            initPopuptWindow();
        }
    }

    private void initPopuptWindow() {

        View contactView = LayoutInflater.from(ContactsActivity.this)
                .inflate(R.layout.pop_window_contact, null);
        LinearLayout group_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_1);
        ImageView imageView1 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_1);
        TextView textView1 = (TextView) contactView
                .findViewById(R.id.pop_textview_1);
        LinearLayout email_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_2);
        ImageView imageView2 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_2);
        TextView textView2 = (TextView) contactView
                .findViewById(R.id.pop_textview_2);
        // TODO: 2016/3/3 meeting
        LinearLayout meeting_add = (LinearLayout) contactView
                .findViewById(R.id.pop_linearlayout_3);
        ImageView imageView3 = (ImageView) contactView
                .findViewById(R.id.pop_imageview_3);
        TextView textView3 = (TextView) contactView
                .findViewById(R.id.pop_textview_3);

        imageView1.setImageResource(R.drawable.icon_add_some);
        textView1.setText(getString(R.string.start_a_group_chat));
        imageView2.setImageResource(R.drawable.icon_add_email);
        textView2.setText(getString(R.string.start_a_email));
        group_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }

                Intent intent = new Intent();
                intent.putExtra("ACTION", "GROUP").putExtra("TITLE", getString(R.string.start_a_group_chat)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
                ((BaseApplication) ContactsActivity.this.getApplication()).getUIController().onIMOrgClickListener(ContactsActivity.this, intent, 0);

            }
        });
        email_add.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }

                Intent intent = new Intent();
                intent.putExtra("TITLE", getString(R.string.start_a_email)).putExtra("ACTION", "EMAIL");
                ((BaseApplication) ContactsActivity.this.getApplication()).getUIController().onIMOrgClickListener(ContactsActivity.this, intent, 0);

            }
        });

        boolean meeting = false;
        try {
            meeting = getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.GET_META_DATA).metaData.getBoolean(
                    "CHANNEL_MEETING", false);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (meeting) {
            imageView3.setImageResource(R.drawable.icon_meeting);
            textView3.setText(R.string.video_conference);
        } else {
            meeting_add.setVisibility(View.GONE);
        }

        meeting_add.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                }
                Intent intent = new Intent();
                intent.putExtra("ACTION", "MEETING").putExtra("TITLE", getString(R.string.video_conference)).putExtra(CommConstants.KEY_GROUP_TYPE, CommConstants.CHAT_TYPE_GROUP_PERSON);
                ((BaseApplication) ContactsActivity.this.getApplication()).getUIController().onIMOrgClickListener(ContactsActivity.this, intent, 0);

            }
        });

        popupWindow = new PopupWindow(contactView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(false);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
    }

}
