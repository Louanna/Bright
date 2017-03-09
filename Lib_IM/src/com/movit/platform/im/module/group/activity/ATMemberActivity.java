package com.movit.platform.im.module.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.module.group.adapter.ATMemberAdapter;
import com.movit.platform.im.module.group.entities.Group;
import com.movit.platform.im.utils.BuildQueryString;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2015/11/17.
 */
public class ATMemberActivity extends IMBaseActivity {

    private Group group;
    private List<UserInfo> _members;
    private ATMemberAdapter _memberAdapter;
    private InputMethodManager inputmanger;

    private EditText et_search;
    private ImageView iv_clear;
    private ListView lv_group_members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.im_activity_at_member);

        inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        TextView title = (TextView) findViewById(R.id.common_top_title);
        title.setText(getString(R.string.group_member_list));
        ImageView back = (ImageView) findViewById(R.id.common_top_left);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        lv_group_members = (ListView) this.findViewById(R.id.lv_group_members);

        UserDao userDao = UserDao.getInstance(this);
        UserInfo curUser = userDao.getUserInfoById(MFSPHelper.getString(CommConstants.USERID));

        //不能@自己
        group = (Group)this.getIntent().getSerializableExtra(IMConstants.KEY_GROUP);
        _members = group.getMembers();
        _members.remove(curUser);

        _memberAdapter = new ATMemberAdapter(this,_members,group.getType(),group.getGroupName());
        lv_group_members.setAdapter(_memberAdapter);
        lv_group_members.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putSerializable(IMConstants.KEY_MEMBER, _members.get(position));
                intent.putExtras(bundle);
                ATMemberActivity.this.setResult(RESULT_OK,intent);
                ATMemberActivity.this.finish();
            }
        });

        et_search = (EditText) this.findViewById(R.id.et_search);
        iv_clear = (ImageView) this.findViewById(R.id.iv_clear);

        iv_clear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                et_search.setText("");
                iv_clear.setVisibility(View.INVISIBLE);
                inputmanger.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                refreshMemberList("");
            }
        });

        et_search.setOnKeyListener(new View.OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = et_search.getText().toString().trim();
                    if (null != content && !"".equalsIgnoreCase(content)) {
                        iv_clear.setVisibility(View.VISIBLE);
                    } else {
                        iv_clear.setVisibility(View.INVISIBLE);
                    }
                    refreshMemberList(content.toUpperCase());
                    inputmanger.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                }

                return false;
            }
        });
    }

    private void refreshMemberList(String content) {

        List<UserInfo> _tempMembers = new ArrayList<UserInfo>();

        switch (group.getType()) {
            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                for (int i = 0; i < _members.size(); i++) {
                    String queryStr = new BuildQueryString().buildQueryName(_members.get(i).getEmpCname());
                    if (queryStr.contains(content)) {
                        _tempMembers.add(_members.get(i));
                    }
                }
                break;
            case CommConstants.CHAT_TYPE_GROUP_ANS:
                for (int i = 0; i < _members.size(); i++) {
                    String queryStr = new BuildQueryString().buildQueryName(_members.get(i).getNickName());
                    if (queryStr.contains(content)) {
                        _tempMembers.add(_members.get(i));
                    }
                }
                break;
            default:
                break;
        }

        _memberAdapter = new ATMemberAdapter(this, _tempMembers,group.getType(),group.getGroupName());
        lv_group_members.setAdapter(_memberAdapter);
    }

    @Override
    public void onBackPressed() {
        this.finish();
    }

}
