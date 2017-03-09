package com.movit.platform.common.module.organization.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.common.module.organization.adapter.Org2Adapter;
import com.movit.platform.common.module.organization.adapter.SearchResultAdapter;
import com.movit.platform.common.module.organization.entities.OrganizationBean;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by air on 16/9/20.
 *
 */
public class Org2Fragment extends Fragment {

    private ListView listView;
    private LinearLayout searchView;
    private ListView searchOrgList;
    private EditText searchText;
    private ImageView searchClear;
    private LinearLayout searchBar;

    private Org2Adapter adapter;
    private SearchResultAdapter searchOrgAdapter;

    private List<OrganizationBean> users;

    private List<UserInfo> tempFriendList = new ArrayList<>();
    private InputMethodManager inputmanger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inputmanger = (InputMethodManager) this.getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.comm_fragment_organization, null);

        initView(rootView);// 初始化view
        initData();
        return rootView;
    }

    private void initView(View rootView) {

        LinearLayout myGroup = (LinearLayout) rootView.findViewById(R.id.myGroup);
        myGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BaseApplication) Org2Fragment.this.getActivity().getApplication()).getUIController().onGroupListClickListener(Org2Fragment.this.getActivity());
            }
        });

        listView = (ListView) rootView.findViewById(R.id.orgunition_list);
        searchBar = (LinearLayout) rootView.findViewById(R.id.search_bar);
        searchClear = (ImageView) rootView.findViewById(R.id.search_clear);
        searchText = (EditText) rootView.findViewById(R.id.search_key);
        searchView = (LinearLayout) rootView.findViewById(R.id.search_view);
        searchOrgList = (ListView) rootView.findViewById(R.id.search_list);

        searchBar.setVisibility(View.VISIBLE);

        searchOrgList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

            }

        });
        searchClear.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputmanger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
                searchView.setVisibility(View.GONE);
                listView.setVisibility(View.VISIBLE);
            }
        });
        final InputMethodManager inputMethodManager = (InputMethodManager) this
                .getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        searchText.setOnKeyListener(new View.OnKeyListener() {// 输入完后按键盘上的搜索键

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

        UserDao dao = UserDao.getInstance(this.getActivity());
        List<UserInfo> searchList = dao.getAllUserInfosBySearch(content);
        tempFriendList.addAll(searchList);

        searchOrgAdapter = new SearchResultAdapter(null, this.getActivity(),
                    false, null);

        searchOrgList.setAdapter(searchOrgAdapter);
        searchOrgAdapter.setUserInfos(tempFriendList);
        searchOrgAdapter.notifyDataSetChanged();
    }

    private void initData() {
        UserDao userDao = UserDao.getInstance(getActivity());
        users = userDao.getAllUserWithOrganization();
        adapter = new Org2Adapter(users,getActivity());
        listView.setAdapter(adapter);
    }
}
