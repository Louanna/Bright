package com.movit.platform.im.module.group.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.widget.CusListView;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.adapter.GroupListAdapter;
import com.movit.platform.im.module.group.entities.Group;

import java.util.List;

public class GroupListActivity extends IMBaseActivity {

    private TextView title;
    private ImageView topLeft, topRight;
    private CusListView listView;

    private GroupListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comm_activity_group);
        iniView();
        initData();
    }

    private void iniView() {
        listView = (CusListView) findViewById(R.id.group_listview);
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_left);
        topRight = (ImageView) findViewById(R.id.common_top_right);
        topRight.setVisibility(View.GONE);
        title.setText(getString(R.string.my_group));
    }

    private void initData() {
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        adapter = new GroupListAdapter(GroupListActivity.this, IMConstants.groupListDatas);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // 多人聊天
                Intent intent = new Intent(GroupListActivity.this,
                        GroupChatActivity.class);
                List<Group> groups = IMConstants.groupListDatas;

                Bundle bundle = new Bundle();
                bundle.putString("room", groups.get(position).getGroupName());
                bundle.putString("subject", groups.get(position).getDisplayName());
                bundle.putInt(CommConstants.KEY_GROUP_TYPE, groups.get(position).getType());
                intent.putExtras(bundle);

                startActivity(intent);

                Intent it = new Intent(CommConstants.MSG_UPDATE_UNREAD_ACTION);
                MessageBean bean = new MessageBean();
                bean.setRoomId(groups.get(position).getGroupName());
                bean.setCtype(CommConstants.CHAT_TYPE_GROUP);
                it.putExtra("messageDataObj", bean);
                it.setPackage(getPackageName());
                sendBroadcast(it);
            }
        });
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

    @Override
    public void updateGroup() {
        adapter.notifyDataSetChanged();
    }
}
