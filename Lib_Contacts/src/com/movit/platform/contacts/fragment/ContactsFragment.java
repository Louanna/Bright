package com.movit.platform.contacts.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.contacts.adapter.ContactsAdapter;
import com.movit.platform.contacts.R;
import com.movit.platform.framework.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

public class ContactsFragment extends Fragment {

    private EditText searchText;
    private List<UserInfo> tempFriendList = new ArrayList<UserInfo>();
    private ContactsAdapter searchResultAdapter;
    private LinearLayout searchView;
    private ListView searchList;
    private ImageView searchClear;
    private InputMethodManager inputmanger;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        inputmanger = (InputMethodManager) this.getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_user, null);

        initViews(rootView);
        initDatas();
        return rootView;
    }

    protected void initViews(View rootView) {
        searchText = (EditText) rootView.findViewById(R.id.search_key);
        searchClear = (ImageView) rootView.findViewById(R.id.search_clear);
        searchView = (LinearLayout) rootView.findViewById(R.id.search_view);
        searchList = (ListView) rootView.findViewById(R.id.search_list);

        searchClear.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                searchText.setText("");
                searchClear.setVisibility(View.INVISIBLE);
                inputmanger.hideSoftInputFromWindow(
                        searchText.getWindowToken(), 0);
                sortFreshData("");
            }
        });
        searchText.setOnKeyListener(new OnKeyListener() {// 输入完后按键盘上的搜索键

            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN) {// 修改回车键功能
                    String content = searchText.getText().toString();
                    if (content != null && !"".equals(content)) {
                        searchClear.setVisibility(View.VISIBLE);
                        searchView.setVisibility(View.VISIBLE);
                        sortFreshData(content + "");
                    } else {
                        searchClear.setVisibility(View.INVISIBLE);
                        searchView.setVisibility(View.GONE);
                    }
                    inputmanger.hideSoftInputFromWindow(
                            searchText.getWindowToken(), 0);
                }
                return false;
            }
        });
    }

    private void initDatas() {
        searchResultAdapter = new ContactsAdapter(tempFriendList,
                this.getActivity(), "");
        searchList.setAdapter(searchResultAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        sortFreshData(searchText.getText().toString());
    }

    private void sortFreshData(String content) {
        tempFriendList.clear();
        if (content != null && !"".equals(content)) {
            UserDao dao = UserDao.getInstance(this.getActivity());
            List<UserInfo> searchList = dao.getAllUserInfosBySearch(content);
            tempFriendList.addAll(searchList);
            searchResultAdapter.setTitle(getString(R.string.contact_str));
            tempFriendList.add(0, new UserInfo());
            tempFriendList.add(1, new UserInfo());
            tempFriendList.add(2, new UserInfo());
        } else {
            tempFriendList.add(0, new UserInfo());
            tempFriendList.add(1, new UserInfo());
            tempFriendList.add(2, new UserInfo());

            ArrayList<String> idStrings = CommConstants.loginConfig.getmUserInfo()
                    .getAttentionPO();
            UserDao dao = UserDao.getInstance(this.getActivity());
            for (int i = 0; i < idStrings.size(); i++) {
                UserInfo userInfo = dao.getUserInfoById(idStrings.get(i));
                if (userInfo != null) {
                    tempFriendList.add(userInfo);
                }
            }
            searchResultAdapter.setTitle(getString(R.string.my_attention));
        }
        searchResultAdapter.setUserInfos(tempFriendList);
        searchResultAdapter.notifyDataSetChanged();
        if (!CommConstants.GET_ATTENTION_FINISH) {
            DialogUtils.getInstants().showLoadingDialog(getActivity(), getString(com.movit.platform.common.R.string.waiting), true);
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (CommConstants.GET_ATTENTION_FINISH) {
                        DialogUtils.getInstants().dismiss();
                        sortFreshData(searchText.getText().toString());
                    } else {
                        handler.postDelayed(this
                                , 1500);
                    }
                }
            }, 1500);
        }
    }

    private Handler handler = new Handler();

    @Override
    public void onDestroy() {
        super.onDestroy();
        tempFriendList.clear();
        tempFriendList = null;
    }

}
