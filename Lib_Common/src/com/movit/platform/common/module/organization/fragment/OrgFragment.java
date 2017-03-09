package com.movit.platform.common.module.organization.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.common.module.organization.adapter.OrgAdapter;
import com.movit.platform.common.module.organization.adapter.SearchResultAdapter;
import com.movit.platform.common.module.organization.entities.OrganizationBean;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.common.utils.Json2ObjUtils;
import com.movit.platform.framework.core.okhttp.OkHttpUtils;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.LogUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.utils.ZipUtils;
import com.movit.platform.framework.view.tree.Node;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

public class OrgFragment extends Fragment {

    private ListView listView;
    private OrgAdapter<Node> adapter;
    private SearchResultAdapter searchOrgAdapter;
    private LinearLayout searchView;
    private ListView searchOrgList;
    private EditText searchText;
    private ImageView searchClear;
    private LinearLayout searchBar;

    private List<UserInfo> tempFriendList = new ArrayList<UserInfo>();
    private String isFromOrg = "";
    private String actionStr = "";
    protected Handler handler;
    private InputMethodManager inputmanger;

    public final static int REFRESH_SUCCESS = 6;
    public final static int REFRESH_FAILD = 7;

    private OrgActivity parentActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        parentActivity = (OrgActivity) this.getActivity();

        Intent intent = parentActivity.getReceivedIntent();
        isFromOrg = intent.getStringExtra("IS_FROM_ORG");
        actionStr = intent.getStringExtra("ACTION");

        iniHandler();
        initData();// 初始化数据
        inputmanger = (InputMethodManager) this.getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        if("Y".equalsIgnoreCase(isFromOrg)){
            searchOrgAdapter = new SearchResultAdapter(null, this.getActivity(),
                    false, OrgActivity.orgCheckedMap);
        }else{
            searchOrgAdapter = new SearchResultAdapter(null, this.getActivity(),
                    true, OrgActivity.orgCheckedMap);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.comm_fragment_organization, null);

        initView(rootView);// 初始化view
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tempFriendList.clear();
        tempFriendList = null;
    }

    private void initView(View rootView) {

        LinearLayout myGroup = (LinearLayout) rootView.findViewById(R.id.myGroup);
        myGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseApplication) OrgFragment.this.getActivity().getApplication()).getUIController().onGroupListClickListener(OrgFragment.this.getActivity());
            }
        });

        listView = (ListView) rootView.findViewById(R.id.orgunition_list);
        searchBar = (LinearLayout) rootView.findViewById(R.id.search_bar);
        searchClear = (ImageView) rootView.findViewById(R.id.search_clear);
        searchText = (EditText) rootView.findViewById(R.id.search_key);
        searchView = (LinearLayout) rootView.findViewById(R.id.search_view);
        searchOrgList = (ListView) rootView.findViewById(R.id.search_list);

        searchBar.setVisibility(View.VISIBLE);

        searchOrgList.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                CheckBox checkBox = (CheckBox) view
                        .findViewById(R.id.contact_item_checkbox);
                UserInfo userInfo =  tempFriendList.get(position);
                if (OrgActivity.originalUserInfos != null
                        && OrgActivity.originalUserInfos.contains(userInfo)) {
                    return;
                }
                if (!checkBox.isEnabled()) {
                    return;
                }
                if (checkBox.isChecked()) {
                    checkBox.setChecked(false);
                    parentActivity.orgCheckedMap.remove(userInfo.getEmpAdname());
                } else {
                    checkBox.setChecked(true);
                    OrgActivity.orgCheckedMap.put(userInfo.getEmpAdname(),
                            userInfo);
                }
                adapter.searchBindCheckData();
            }

        });
        searchClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputmanger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
                searchView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
                adapter.notifyDataSetChanged();
            }
        });
        final InputMethodManager inputMethodManager = (InputMethodManager) this
                .getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        searchText.setOnKeyListener(new OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = searchText.getText().toString().trim();
                    if (!TextUtils.isEmpty(content)) {
                        searchClear.setVisibility(View.VISIBLE);
                        searchView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        sortFreshData(content + "");
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                        searchView.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        inputmanger.hideSoftInputFromWindow(
                                searchText.getWindowToken(), 0);
                    }
                    inputMethodManager.hideSoftInputFromWindow(
                            searchText.getWindowToken(), 0);
                }
                return false;
            }
        });

    }

    protected void sortFreshData(final String content) {
        tempFriendList.clear();
        if (content != null && !"".equals(content.trim())) {
            UserDao dao = UserDao.getInstance(this.getActivity());
            List<UserInfo> searchList = dao.getAllUserInfosBySearch(content);
            tempFriendList.addAll(searchList);
        } else {
            tempFriendList.addAll(CommConstants.allUserInfos);
        }

        if("Y".equalsIgnoreCase(isFromOrg)){
            searchOrgAdapter = new SearchResultAdapter(null, this.getActivity(),
                    false, OrgActivity.orgCheckedMap);
        }else{
            searchOrgAdapter = new SearchResultAdapter(null, this.getActivity(),
                    true, OrgActivity.orgCheckedMap);
        }
        searchOrgList.setAdapter(searchOrgAdapter);
        searchOrgAdapter.setUserInfos(tempFriendList);
        searchOrgAdapter.setAction(actionStr, parentActivity);
        searchOrgAdapter.notifyDataSetChanged();
    }

    DialogUtils progressDownLoading;

    private void initData() {
        progressDownLoading = DialogUtils.getInstants();
        updateEoopDB();
    }

    private void downEoopDB() {
        long lastSyncTime = MFSPHelper.getLong("lastSyncTime");
        if (lastSyncTime == -1) {
            progressDownLoading.dismiss();
            DialogUtils.getInstants().dismiss();
            progressDownLoading.showDownLoadingDialog(this.getActivity(),
                    getString(R.string.downloading_file), false);
            progressDownLoading.getLoadingDialog().setCancelable(true);

            OkHttpUtils.getWithToken()
                    .url(CommConstants.URL_FULL_ORG_LIST)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(String response) throws JSONException {
                            File zipFile = null;
                            try {
                                JSONObject jsonResult = new JSONObject(response);
                                long orgTime = jsonResult.getLong("lastSyncTime");
                                String downUrl = jsonResult
                                        .getString("fullOrgFilePath");

                                FileUtils fileUtils = new FileUtils();
                                fileUtils
                                        .setDownLoadProcessListener(progressDownLoading);
                                LogUtils.v("downLoadEOOPDB", "------down------" + downUrl);
                                int k = fileUtils.downfile(handler, downUrl,
                                        CommConstants.SD_DOWNLOAD, "eoop.db.zip");
                                if (k == 0 || k == 1) {// 或者已存在
                                    // 解压缩
                                    Log.v("downLoadEOOPDB", "------upZip------");
                                    zipFile = new File(CommConstants.SD_DOWNLOAD,
                                            "eoop.db.zip");
                                    ZipUtils.upZipFile(zipFile, CommConstants.SD_DOWNLOAD);
                                    MFSPHelper.setLong("lastSyncTime", orgTime);
                                    handler.sendEmptyMessage(9);
                                } else if (k == -1) {// 失败
                                    handler.obtainMessage(99, getString(R.string.downloading_file_fail))
                                            .sendToTarget();
                                }
                            } catch (Exception e) {
                                handler.obtainMessage(99, getString(R.string.downloading_file_fail)).sendToTarget();
                            } finally {
                                // 删除
                                if (zipFile != null) {
                                    zipFile.delete();
                                }
                                new File(CommConstants.SD_DOWNLOAD,
                                        UserDao.getInstance(getActivity()).getUserDBFileName()).delete();
                            }
                        }
                    });
        } else {
            handler.sendEmptyMessage(9);
        }
    }

    private void updateEoopDB() {
        DialogUtils.getInstants().dismiss();
        progressDownLoading.dismiss();
        DialogUtils.getInstants().showLoadingDialog(this.getActivity(),
                getString(R.string.waiting), false);
        DialogUtils.getInstants().getLoadingDialog().setCancelable(true);

        final UserDao dao = UserDao.getInstance(getActivity());
        if (CommConstants.allUserInfos == null) {
            CommConstants.allUserInfos = dao.getAllUserInfos();
            CommConstants.allOrgunits = dao.getAllOrgunitions();
        }

        long lastSyncTime = MFSPHelper.getLong("lastSyncTime");
        // 增量更新
        OkHttpUtils.getWithToken()
                .url(CommConstants.URL_DELTA_ORG_LIST+ DateUtils.date2Str(new Date(lastSyncTime),
                        "yyyy-MM-dd_HH:mm:ss"))
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(String response) throws JSONException {
                        try {
                            JSONObject jsonResult = new JSONObject(response);

                            // "code" : "org.outOfDeltaRange",
                            if (jsonResult.has("code")) {
                                String code = jsonResult.getString("code");
                                if ("org.outOfDeltaRange".equals(code)) {
                                    MFSPHelper.setLong("lastSyncTime", -1);
                                    String databaseFilename = dao.getUserDBFile();
                                    (new File(databaseFilename)).delete();
                                    CommConstants.allUserInfos = null;
                                    CommConstants.allOrgunits = null;
                                    handler.sendEmptyMessage(8);
                                    return;
                                }
                            }
                            long time = jsonResult.getLong("lastSyncTime");
                            List<OrganizationTree> orgList = new ArrayList<OrganizationTree>();
                            List<UserInfo> userList = new ArrayList<UserInfo>();

                            if (jsonResult.has("orgList")) {
                                JSONArray array = jsonResult.getJSONArray("orgList");
                                for (int i = 0; i < array.length(); i++) {
                                    orgList.add(Json2ObjUtils.getOrgunFromJson(array
                                            .getJSONObject(i).toString()));
                                }
                            }
                            if (jsonResult.has("userList")) {
                                JSONArray userarray = jsonResult
                                        .getJSONArray("userList");
                                for (int j = 0; j < userarray.length(); j++) {
                                    userList.add(Json2ObjUtils.getUserInfoFromJson(userarray
                                            .getJSONObject(j).toString()));
                                }
                            }
                            for (int i = 0; i < orgList.size(); i++) {
                                dao.updateOrgByFlags(orgList.get(i));
                            }
                            for (int j = 0; j < userList.size(); j++) {
                                dao.updateUserByFlags(userList.get(j));
                            }

                            CommConstants.allUserInfos = dao.getAllUserInfos();
                            CommConstants.allOrgunits = dao.getAllOrgunitions();

                            MFSPHelper.setLong("lastSyncTime", time);
                            handler.sendEmptyMessage(1);
                        } catch (Exception e) {
                            e.printStackTrace();
                            handler.sendEmptyMessage(99);
                        }
                    }
                });
    }

    public void iniHandler() {
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                List<Map<String, Object>> listmMaps = new ArrayList<Map<String, Object>>();
                Map<String, Object> child = new HashMap<String, Object>();
                listmMaps.add(child);

                switch (msg.what) {
                    case 1:
                        try {
                            List<Node> userNodes = new ArrayList<Node>();
                            for (UserInfo userInfo : CommConstants.allUserInfos) {
                                OrganizationBean bean = new OrganizationBean(
                                        userInfo.getEmpCname(), userInfo);
                                Node node = new Node(userInfo.getId(),
                                        userInfo.getOrgId(), bean);
                                userNodes.add(node);
                            }
                            List<Node> orgNodes = new ArrayList<Node>();
                            for (OrganizationTree orgu : CommConstants.allOrgunits) {
                                OrganizationBean bean = new OrganizationBean(
                                        orgu.getObjname(), null);
                                Node node = new Node(orgu.getId(),
                                        orgu.getParentId(), bean);
                                orgNodes.add(node);
                            }
                            adapter = new OrgAdapter<Node>(listView,
                                    OrgFragment.this.getActivity(),
                                    userNodes, orgNodes, 0, isFromOrg,
                                    parentActivity);
                            adapter.setAction(actionStr);
                            listView.setAdapter(adapter);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                        DialogUtils.getInstants().dismiss();
                        break;

                    case REFRESH_SUCCESS:
                        DialogUtils.getInstants().dismiss();
                        ToastUtils.showToast(
                                OrgFragment.this.getActivity(), getString(R.string.refresh_success));
                        initData();
                        break;
                    case REFRESH_FAILD:
                        DialogUtils.getInstants().dismiss();
                        ToastUtils.showToast(
                                OrgFragment.this.getActivity(), getString(R.string.refresh_fail));
                        break;
                    case 8:
                        downEoopDB();
                        break;
                    case 9:
                        updateEoopDB();
                        break;
                    case DialogUtils.progressHandlerIndex:
                        int fileSize = msg.arg1;
                        int downSize = msg.arg2;
                        progressDownLoading.setDownLoadProcess(fileSize, downSize);
                        break;
                    case 99:
                        DialogUtils.getInstants().dismiss();
                        String errMsg = getString(R.string.load_fail);
                        try {
                            String err = (String) msg.obj;
                            if (StringUtils.notEmpty(err)) {
                                errMsg = err;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        ToastUtils.showToast(
                                OrgFragment.this.getActivity(), errMsg);
                        break;
                    default:
                        break;
                }
            }
        };
    }
}