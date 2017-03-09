package com.movit.platform.im.module.msg.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.widget.CusListView;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.activity.GroupChatActivity;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.module.msg.adapter.SystemMsgAdapter;

import java.util.Map;

public class SystemMsgActivity extends IMBaseActivity {
    TextView title;
    ImageView topLeft;
    TextView topRight;
    CusListView listView;

    SystemMsgAdapter adapter;

    Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            DialogUtils.getInstants().dismiss();
            switch (msg.what) {
                case 1:
                    adapter = new SystemMsgAdapter(SystemMsgActivity.this, IMConstants.sysMsgList);
                    listView.setAdapter(adapter);
                    listView.setOnItemClickListener(new OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            MessageBean bean = IMConstants.sysMsgList
                                    .get(position);
                            Map<String, Group> groupMap = IMConstants.groupsMap;
                            if (groupMap.containsKey(bean.getRoomId())) {
                                Intent intent = new Intent(SystemMsgActivity.this,
                                        GroupChatActivity.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("room", bean.getRoomId());
                                bundle.putString("subject", bean.getSubject());

                                Group group = IMConstants.groupsMap.get(bean.getRoomId());
                                bundle.putInt(CommConstants.KEY_GROUP_TYPE, group.getType());

                                intent.putExtras(bundle);

                                startActivity(intent);
                            } else {
                                ToastUtils.showToast(SystemMsgActivity.this, getString(R.string.leave_group));
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_activity_system_msg);

        iniView();
        iniData();
    }

    private void iniView() {
        listView = (CusListView) findViewById(R.id.system_msg_listview);
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        title.setText(getString(R.string.notification));
        topRight.setText(getString(R.string.clear));
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                IMConstants.sysMsgList.clear();
                handler.sendEmptyMessage(1);
                setResult(99);
                finish();
            }
        });
    }

    private void iniData() {
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        DialogUtils.getInstants().showLoadingDialog(this, getString(R.string.loading), false);
        handler.sendEmptyMessage(1);

        for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
            if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                IMConstants.contactListDatas.get(i).setUnReadCount(0);
                IMConstants.contactListDatas.get(i).setIsread(CommConstants.MSG_READ);
                break;
            }
        }
    }

    //执行移除Member响应
    @Override
    public void afterKicked(String roomName, String displayName){
        adapter.notifyDataSetChanged();
    }

    //执行加入群组的响应
    @Override
    public void afterInvited(String roomName, String inviter, String invitee, Group group){
        adapter.notifyDataSetChanged();
    }

    //执行Member Change响应
    @Override
    public void afterMemberChanged(String roomName, String affecteds, String displayName, String type){
        adapter.notifyDataSetChanged();
    }

    //执行Group Name Change响应
    @Override
    public void afterGroupNameChanged(String roomName, String displayName){
        adapter.notifyDataSetChanged();
    }

    //执行Group Dissolved响应
    @Override
    public void afterGroupDisolved(String roomName, String displayName){
        adapter.notifyDataSetChanged();
    }

    private BroadcastReceiver systemReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (CommConstants.ACTION_GROUP_MEMBERS_CHANGES.equals(action)) {
                handler.sendEmptyMessage(1);
            } else if (CommConstants.ACTION_GROUP_DISPALYNAME_CHANGES.equals(action)) {
                handler.sendEmptyMessage(1);
            } else if (CommConstants.ACTION_GROUP_DISSOLVE_CHANGES.equals(action)) {
                handler.sendEmptyMessage(1);
            } else if (CommConstants.ACTION_MY_KICKED.equals(action)
                    || CommConstants.ACTION_MY_INVITE.equals(action)) {
                handler.sendEmptyMessage(1);
            }

            for (int i = 0; i < IMConstants.contactListDatas.size(); i++) {
                if (IMConstants.contactListDatas.get(i).getCtype() == CommConstants.CHAT_TYPE_SYSTEM) {
                    IMConstants.contactListDatas.get(i).setUnReadCount(0);
                    IMConstants.contactListDatas.get(i).setIsread(CommConstants.MSG_READ);
                    break;
                }
            }
        }
    };
}
