package com.movit.platform.common.module.organization.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.helper.CommonHelper;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.organization.fragment.Org2Fragment;
import com.movit.platform.common.module.organization.fragment.OrgFragment;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ActivityUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OrgActivity extends FragmentActivity {

    protected TextView mTopTitle, topClose;
    private ImageView mTopLeftImage, topRight;
    private String isFromOrg, titleStr, actionStr;

    protected int ctype = -1;

    public static List<UserInfo> originalUserInfos;
    public static Map<String, UserInfo> orgCheckedMap = new HashMap<String, UserInfo>();
    public static Map<String, OrganizationTree> orgCheckedCatMap = new HashMap<String, OrganizationTree>();
    private Intent intent;

    private List<Fragment> fragments = new ArrayList<>();
    private int currentTab; // 当前Tab页面索引

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comm_activity_organization);
        intent = getReceivedIntent();
        initView();
        initData();

        fragments.clear();
        fragments.add(new OrgFragment());
        fragments.add(new Org2Fragment());

        // 默认显示第一页
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        ft.add(R.id.common_fragment, fragments.get(0));
        ft.add(R.id.common_fragment, fragments.get(1));
        ft.hide(fragments.get(1));
        ft.commit();

    }

//    private void changeTab(Fragment fragment){
//        FragmentTransaction ft = obtainFragmentTransaction(i);
//        getCurrentFragment().onPause(); // 暂停当前tab
//        if (fragment.isAdded()) {
//            fragment.onResume(); // 启动目标tab的onResume()
//        } else {
//            ft.add(R.id.common_fragment, fragment);
//        }
//        showTab(i); // 显示目标tab
//        ft.commit();
//    }

    /**
     * 切换tab
     *
     * @param idx witch tab
     */
    private void showTab(int idx) {
        for (int i = 0; i < fragments.size(); i++) {
            Fragment fragment = fragments.get(i);
            FragmentTransaction ft = obtainFragmentTransaction(idx);

            if (idx == i) {
                ft.show(fragment);
            } else {
                ft.hide(fragment);
            }
            ft.commit();
        }
        currentTab = idx; // 更新目标tab为当前tab
    }

    /**
     * 获取一个带动画的FragmentTransaction
     *
     * @param index
     * @return
     */
    private FragmentTransaction obtainFragmentTransaction(int index) {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();
        // 设置切换动画
        if (index > currentTab) {
            ft.setCustomAnimations(R.anim.slide_left_in, R.anim.slide_left_out);
        } else {
            ft.setCustomAnimations(R.anim.slide_right_in,
                    R.anim.slide_right_out);
        }
        return ft;
    }

    @SuppressWarnings("unchecked")
    private void initData() {
        orgCheckedMap.clear();
        orgCheckedCatMap.clear();
        if (originalUserInfos != null) {
            originalUserInfos.clear();
        }
        originalUserInfos = (List<UserInfo>) intent
                .getSerializableExtra("userInfos");

        CommonHelper tools = new CommonHelper(this);

        if ("@".equals(actionStr) || "WebView".equals(actionStr) || "share".equals(actionStr)) {
            List<UserInfo> atUserInfos = (List<UserInfo>) intent
                    .getSerializableExtra("atUserInfos");
            List<OrganizationTree> atOrgunitionLists = (List<OrganizationTree>) intent
                    .getSerializableExtra("atOrgunitionLists");
            if (atUserInfos != null) {
                for (int i = 0; i < atUserInfos.size(); i++) {
                    orgCheckedMap.put(atUserInfos.get(i).getEmpAdname(),
                            atUserInfos.get(i));
                }
            }
            if (atOrgunitionLists != null) {
                for (int i = 0; i < atOrgunitionLists.size(); i++) {
                    orgCheckedCatMap.put(atOrgunitionLists.get(i).getObjname(),
                            atOrgunitionLists.get(i));
                }
            }
            if ("WebView".equals(actionStr)) {
                UserInfo userInfo = tools.getLoginConfig().getmUserInfo();
                if (originalUserInfos == null) {
                    originalUserInfos = new ArrayList<UserInfo>();
                }
                if (!originalUserInfos.contains(userInfo)) {
                    originalUserInfos.add(userInfo);
                }
            }
        } else if ("NativeEmailAction".equals(actionStr) || "EMAIL".equals(actionStr)) {

        } else {
            UserInfo userInfo = tools.getLoginConfig().getmUserInfo();
            if (originalUserInfos == null) {
                originalUserInfos = new ArrayList<UserInfo>();
            }
            if (!originalUserInfos.contains(userInfo)) {
                originalUserInfos.add(userInfo);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (originalUserInfos != null) {
            originalUserInfos.clear();
        }
        if (orgCheckedMap != null) {
            orgCheckedMap.clear();
        }
    }

    private void initView() {
        mTopTitle = (TextView) findViewById(R.id.common_top_title);
        topClose = (TextView) findViewById(R.id.common_top_close);
        mTopLeftImage = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (ImageView) findViewById(R.id.common_top_img_right);



        if (!"default".equals(MFSPHelper.getString(BaseApplication.SKINTYPE))) {
            RelativeLayout layout = (RelativeLayout) findViewById(R.id.common_top_layout);
            layout.setBackgroundColor(Color.parseColor(BaseApplication.TOP_COLOR));
        }

        if ("NativeEmailAction".equals(actionStr) || "GROUP".equals(actionStr) || "EMAIL".equals(actionStr)
                || "@".equals(actionStr) || "WebView".equals(actionStr)
                || "MEETING".equals(actionStr) || "share".equals(actionStr) || "forward".equals(actionStr)) {
            mTopTitle.setText(titleStr);
        } else if ("Y".equalsIgnoreCase(isFromOrg)) {
            mTopTitle.setText("通讯录");
        } else {
            mTopTitle.setText("发起群聊");
        }

        mTopLeftImage.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        if ("share".equals(actionStr)) {
            topRight.setImageResource(R.drawable.icon_share);
        } else if ("NativeEmailAction".equals(actionStr) || "EMAIL".equals(actionStr)) {
            topRight.setVisibility(View.INVISIBLE);
            topClose.setVisibility(View.VISIBLE);
        } else if("GROUP".equals(actionStr)){
            topClose.setVisibility(View.VISIBLE);
            topRight.setVisibility(View.GONE);
        } else {
            topRight.setImageResource(R.drawable.icon_zuzhi);
        }

        topRight.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("share".equals(actionStr)) {
                    ATEventListener();
                } else {
                    if (currentTab == 0) {
                        showTab(1);
                    } else {
                        showTab(0);
                    }
                }
            }
        });

//        topRight.setOnClickListener(new OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                topRight.setClickable(false);
//                if ("NativeEmailAction".equals(actionStr) || "EMAIL".equals(actionStr)) {
//                    nativeEmailListener();
//                } else if ("@".equals(actionStr) || "WebView".equals(actionStr) || "share".equals(actionStr)) {
//                    ATEventListener();
//                } else {
//                    Log.d("OrgActivity", "ctype=" + ctype);
//                    if (ctype == CommConstants.CHAT_TYPE_GROUP) {
//                        if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
//                            topRight.setClickable(true);
//                            return;
//                        }
//                        if (orgCheckedMap.size() > 15) {
//                            ToastUtils.showToast(OrgActivity.this,
//                                    getString(R.string.only_15_people_select));
//                            topRight.setClickable(true);
//                            return;
//                        }
//
//                        DialogUtils.getInstants().showLoadingDialog(
//                                OrgActivity.this, getString(R.string.add_group_member), false);
//                        topRight.setClickable(true);
//                        // 添加群组成员
//                        addGroupMembers();
//                    } else {
//                        //创建群组
//                        createGroup();
//                    }
//                }
//            }
//        });

        topClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("NativeEmailAction".equals(actionStr) || "EMAIL".equals(actionStr)) {
                    nativeEmailListener();
                }else if ("GROUP".equals(actionStr)) {
                    if (ctype == CommConstants.CHAT_TYPE_GROUP) {
                        if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
                            topRight.setClickable(true);
                            return;
                        }
                        if (orgCheckedMap.size() > 15) {
                            ToastUtils.showToast(OrgActivity.this,
                                    getString(R.string.only_15_people_select));
                            topRight.setClickable(true);
                            return;
                        }

                        DialogUtils.getInstants().showLoadingDialog(
                                OrgActivity.this, getString(R.string.add_group_member), false);
                        topRight.setClickable(true);
                        // 添加群组成员
                        addGroupMembers();
                    } else {
                        //创建群组
                        createGroup();
                    }
                }
            }
        });

    }

    public void addGroupMembers() {
        String memberIdsString = "";
        Iterator<Map.Entry<String, UserInfo>> it = orgCheckedMap
                .entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, UserInfo> entry = it.next();
            UserInfo val = entry.getValue();
            if (val.getId() != null
                    && !"".equalsIgnoreCase(val.getId())) {
                memberIdsString += val.getId() + ",";
            }

        }
        String memberIds = memberIdsString.substring(0,
                memberIdsString.length() - 1);

        ((BaseApplication) this.getApplication()).getManagerFactory().getGroupManager().addMembers(getReceivedIntent().getStringExtra("groupId"), memberIds, initHandler());
    }

    public void createGroup() {
        String cname = MFSPHelper.getString(CommConstants.EMPCNAME);
        String memberIdsString = "";
        List<String> subjects = new ArrayList<String>();
        subjects.add(cname);
        if (ctype == CommConstants.CHAT_TYPE_SINGLE) {
            // 把原来聊天的成员也加
            for (int i = 0; i < originalUserInfos.size() - 1; i++) {
                if (StringUtils.notEmpty(originalUserInfos
                        .get(i).getId())) {
                    memberIdsString += originalUserInfos.get(i)
                            .getId() + ",";
                    subjects.add(originalUserInfos.get(i)
                            .getEmpCname());
                }
            }
        } else {
            if (orgCheckedMap == null
                    || orgCheckedMap.isEmpty()) {
                topRight.setClickable(true);
                return;
            }
        }

        if (orgCheckedMap.size() > 15) {
            ToastUtils.showToast(this, getString(R.string.only_15_people_select));
            topRight.setClickable(true);
            return;
        }

        DialogUtils.getInstants().showLoadingDialog(
                this, getString(R.string.create_room),
                false);
        topRight.setClickable(true);
        Iterator<Map.Entry<String, UserInfo>> it = orgCheckedMap
                .entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry<String, UserInfo> entry = it.next();
            UserInfo val = entry.getValue();
            if (val.getId() != null
                    && !"".equalsIgnoreCase(val.getId())) {
                memberIdsString += val.getId() + ",";
                subjects.add(val.getEmpCname());
            }
        }

        String memberIds = memberIdsString.substring(0,
                memberIdsString.length() - 1);

        //TODO modify by anna
        String cnames = "";
        switch (getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0)) {
            case CommConstants.CHAT_TYPE_GROUP_PERSON:
                //实名群聊群名称生成规则：XX、XX等X人
                cnames = getRealNameGroup(subjects);
                break;
            case CommConstants.CHAT_TYPE_GROUP_ANS:
                //匿名群聊群名称生成规则：XX的匿名群组（XX为创建者即群组）
                cnames = getAnsGroup(cname);
                break;
            default:
                break;
        }
        ((BaseApplication) this.getApplication()).getManagerFactory().getGroupManager().createGroup(
                memberIds, cnames, cname + getString(R.string.create_group), getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0), initHandler());
    }

    //匿名群聊群名称生成规则：XX的匿名群组（XX为创建者即群组）
    private String getAnsGroup(String cname) {
        return cname + getString(R.string.ans_group);
    }

    //实名群聊群名称生成规则：XX、XX等X人
    private String getRealNameGroup(List<String> subjects) {
        String cnames = "";
        for (int i = 0; i < subjects.size(); i++) {
            cnames += subjects.get(i).split("\\.")[0] + "、";
            if (i == 1) {
                break;
            }
        }
        cnames = cnames.subSequence(0, cnames.length() - 1)
                + getString(R.string.etc) + subjects.size() + getString(R.string.people);
        return cnames;
    }

    protected Handler initHandler() {
        return new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                List<Map<String, Object>> listmMaps = new ArrayList<Map<String, Object>>();
                Map<String, Object> child = new HashMap<String, Object>();
                listmMaps.add(child);

                switch (msg.what) {

                    case 2:
                        DialogUtils.getInstants().dismiss();

                        String[] arr = (String[]) msg.obj;

                        Bundle bundle = new Bundle();
                        bundle.putString("room", arr[0]);
                        bundle.putString("subject", arr[1]);
                        bundle.putInt(CommConstants.KEY_GROUP_TYPE, getIntent().getIntExtra(CommConstants.KEY_GROUP_TYPE, 0));
                        //如果是发起会议，先接口获取会议url，在默认发送一条消息
                        if ("MEETING".equals(actionStr)) {
                            bundle.putBoolean("meeting", true);
                        }
                        ((BaseApplication) OrgActivity.this.getApplication()).getUIController().startMultChat(OrgActivity.this, bundle);
                        finish();
                        orgCheckedMap.clear();
                        break;
                    case 3:
                        DialogUtils.getInstants().dismiss();
                        ToastUtils.showToast(OrgActivity.this,
                                getString(R.string.create_fail));
                        break;
                    case 4:
                        // 更新group 统一由IMChatService 管理
                        setResult(1);
                        DialogUtils.getInstants().dismiss();
                        finish();
                        break;
                    case 5:
                        DialogUtils.getInstants().dismiss();
                        ToastUtils.showToast(OrgActivity.this, getString(R.string.invite_fail));
                        break;
                    default:
                        break;
                }
            }
        };
    }


    private void emailEventListener() {
        try {
            if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
                topRight.setClickable(true);
                return;
            }
            List<String> emaiList = new ArrayList<String>();
            Set<String> keys = orgCheckedMap.keySet();
            UserInfo u;
            for (String s : keys) {
                u = orgCheckedMap.get(s);
                if (!StringUtils.empty(u.getMail())) {
                    emaiList.add(u.getMail().trim());
                }
            }
            String[] emails = emaiList.toArray(new String[emaiList
                    .size()]);
            ActivityUtils.sendMails(OrgActivity.this,
                    emails);
        } catch (Exception e) {
            e.printStackTrace();
        }
        topRight.setClickable(true);
    }

    private void nativeEmailListener() {
        if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
            //topRight.setClickable(true);
            Toast.makeText(OrgActivity.this, R.string.no_email_address_for_org, Toast.LENGTH_SHORT).show();
            return;
        }
        ArrayList<UserInfo> result = new ArrayList<>();
        Set<String> keys = orgCheckedMap.keySet();
        for (String s : keys) {
            UserInfo u = orgCheckedMap.get(s);
            if (!StringUtils.empty(u.getMail()) && !u.getMail().equals("null")) {
                result.add(u);
            }
        }
        if (result.size() > 0) {
            Intent intent = new Intent();
            intent.putExtra("userInfos", result);
            if ("NativeEmailAction".equals(actionStr)) {
                setResult(1, intent);
                finish();
            } else {
                BaseApplication application = (BaseApplication) getApplication();
                application.getUIController().onSendEmailClickListener(this, intent);
            }
        } else {
            Toast.makeText(OrgActivity.this, R.string.no_email_address_for_org, Toast.LENGTH_SHORT).show();
        }
    }

    private void ATEventListener() {
        if ("@".equals(actionStr) || "share".equals(actionStr)) {
            if (orgCheckedMap == null || orgCheckedMap.isEmpty()) {
                topRight.setClickable(true);
                ToastUtils.showToast(OrgActivity.this,
                        getString(R.string.at_last_one));
                return;
            }
        }
        // orgUserInfos intent 传递的已选择的人
        ArrayList<UserInfo> atUserInfos = new ArrayList<UserInfo>();
        if ("WebView".equals(actionStr)) {
            for (int i = 0; i < originalUserInfos.size(); i++) {
                atUserInfos.add(originalUserInfos.get(i));
            }
        }

        if ("WebView".equals(actionStr) || "share".equals(actionStr)) {
            if (orgCheckedMap.size() > 15) {
                ToastUtils.showToast(OrgActivity.this,
                        getString(R.string.only_15_people_select));
                topRight.setClickable(true);
                return;
            }
        }

        Set<String> keys = orgCheckedMap.keySet();
        for (String key : keys) {
            UserInfo userInfo = orgCheckedMap.get(key);
            if (!atUserInfos.contains(userInfo)) {
                atUserInfos.add(userInfo);
            }
        }
        ArrayList<OrganizationTree> atOrgunitionLists = new ArrayList<OrganizationTree>();
        Set<String> catKeys = orgCheckedCatMap.keySet();
        for (String cat : catKeys) {
            atOrgunitionLists.add(orgCheckedCatMap.get(cat));
        }
        topRight.setClickable(true);

        Intent intent = new Intent();
        intent.putExtra("atUserInfos", atUserInfos);
        intent.putExtra("atOrgunitionLists", atOrgunitionLists);
        setResult(1, intent);
        finish();
    }

    public Intent getReceivedIntent() {

        Intent intent = getIntent();
        isFromOrg = intent.getStringExtra("IS_FROM_ORG");
        titleStr = intent.getStringExtra("TITLE");
        actionStr = intent.getStringExtra("ACTION");
        ctype = intent.getIntExtra("ctype", -1);
        return intent;
    }

    public void setResult(UserInfo atUserInfos) {
        Intent intent = new Intent();
        intent.putExtra("atUserInfos", atUserInfos);
        setResult(1, intent);
        finish();
    }

}
