package com.movit.platform.contacts.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.contacts.R;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.StringUtils;

import java.util.List;

@SuppressLint("ResourceAsColor")
public class ContactsAdapter extends BaseAdapter {

    private List<UserInfo> userInfos;
    private Activity activity;
    private LayoutInflater inflater;
    private ViewHolderChild holderChild;
    private String title;
    AQuery aq;

    public ContactsAdapter(List<UserInfo> userInfos, Activity activity,
                          String title) {
        super();
        this.userInfos = userInfos;
        this.activity = activity;
        inflater = LayoutInflater.from(activity);
        this.title = title;
        aq = new AQuery(activity);
    }

    public void setUserInfos(List<UserInfo> userInfos) {
        this.userInfos = userInfos;
    }

    @Override
    public int getCount() {
        return (userInfos == null || userInfos.size() == 0) ? 0 : userInfos
                .size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null
                || convertView.getTag(R.drawable.icon + position) == null) {
            convertView = inflater.inflate(R.layout.item_user, null);
            holderChild = new ViewHolderChild();
            holderChild.icon = (ImageView) convertView
                    .findViewById(R.id.contact_item_icon);
            holderChild.name = (TextView) convertView
                    .findViewById(R.id.contact_item_name);
            holderChild.content = (TextView) convertView
                    .findViewById(R.id.contact_item_content);
            holderChild.subName = (TextView) convertView
                    .findViewById(R.id.contact_item_sub_name);
            holderChild.checkbox = (CheckBox) convertView
                    .findViewById(R.id.contact_item_checkbox);
            holderChild.title_content = (TextView) convertView
                    .findViewById(R.id.search_comment);
            holderChild.front = (RelativeLayout) convertView
                    .findViewById(R.id.front);
            holderChild.nofocus = (ImageView) convertView
                    .findViewById(R.id.no_attention);
            convertView.setTag(R.id.icon + position, holderChild);
        } else {
            holderChild = (ViewHolderChild) convertView.getTag(R.id.icon
                    + position);
        }
        AQuery aQuery = aq.recycle(convertView);
        try {
            holderChild.title_content.setText(title + "");
            final UserInfo userInfo = (UserInfo) userInfos.get(position);

            if (position == 0) {
                holderChild.nofocus.setVisibility(View.GONE);
                holderChild.title_content.setVisibility(View.GONE);
                holderChild.content.setVisibility(View.GONE);
                holderChild.subName.setVisibility(View.GONE);
                holderChild.name.setText(R.string.organization);
                aQuery.id(holderChild.icon).image(
                        R.drawable.contact_organization);
            } else if (position == 1) {
                holderChild.nofocus.setVisibility(View.GONE);
                holderChild.title_content.setVisibility(View.GONE);
                holderChild.content.setVisibility(View.GONE);
                holderChild.subName.setVisibility(View.GONE);
                holderChild.name.setText(R.string.my_group);
                aQuery.id(holderChild.icon).image(R.drawable.contact_group);
            } else if (position == 2) {
                holderChild.title_content.setVisibility(View.VISIBLE);
                if (getCount() == 3) {
                    holderChild.nofocus.setVisibility(View.VISIBLE);
                } else {
                    holderChild.title_content.setVisibility(View.VISIBLE);
                }
                holderChild.front.setVisibility(View.GONE);
            } else {

                if (userInfo == null) {
                    return convertView;
                }
                UserDao userDao = UserDao.getInstance(activity);
                UserInfo user = userDao.getUserInfoById(userInfo.getId());
                if (user == null) {
                    return convertView;
                }

                holderChild.nofocus.setVisibility(View.GONE);
                holderChild.title_content.setVisibility(View.GONE);
                holderChild.checkbox.setTag(position + "checkbox");
                int picId = R.drawable.avatar_male;
                if (activity.getString(R.string.boy).equals(user.getGender())) {
                    picId = R.drawable.avatar_male;
                } else if (activity.getString(R.string.girl).equals(user.getGender())) {
                    picId = R.drawable.avatar_female;
                }
                String uname = MFSPHelper.getString(CommConstants.AVATAR);
                String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
                String avatarName = user.getAvatar();

                String avatarUrl = "";
                if (StringUtils.notEmpty(avatarName)) {
                    avatarUrl = avatarName;
                }
                if (adname.equalsIgnoreCase(user.getEmpAdname())
                        && StringUtils.notEmpty(uname)) {
                    avatarUrl = uname;
                }
                if (StringUtils.notEmpty(avatarUrl)) {
                    BitmapAjaxCallback callback = new BitmapAjaxCallback();
                    callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                            .round(10).fallback(picId)
                            .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                            .fileCache(true).targetWidth(128);
                    aQuery.id(holderChild.icon).image(callback);

                } else {
                    Bitmap bitmap = PicUtils.getRoundedCornerBitmap(activity,
                            picId, 10);
                    holderChild.icon.setImageBitmap(bitmap);
                }

                holderChild.content.setVisibility(View.VISIBLE);
                holderChild.subName.setVisibility(View.GONE);
                UserDao dao = UserDao.getInstance(activity);
                OrganizationTree org = dao
                        .getOrganizationByOrgId(user.getOrgId());
                if (org != null) {
                    holderChild.content.setText(org.getObjname());
                }
                holderChild.name.setText(user.getEmpCname().split("\\.")[0]);
                holderChild.subName.setText(user.getEmpAdname());
                holderChild.checkbox.setVisibility(View.GONE);
            }
            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v("setOnClickListener", "click");
                    if (position == 0) {
                        Intent i = new Intent();
                        i.putExtra("IS_FROM_ORG", "Y");
                        ((BaseApplication) activity.getApplication()).getUIController().onIMOrgClickListener(activity, i,0);

                    } else if (position == 1) {

                        ((BaseApplication) activity.getApplication()).getUIController().onGroupListClickListener(activity);

                    } else if (position == 2) {
                        return;
                    } else {
                        Intent intent = new Intent();
                        intent.putExtra("userInfo", userInfo);
                        ((BaseApplication) activity.getApplication()).getUIController().onOwnHeadClickListener(activity, intent,0);
                    }
                }
            });

            convertView.setOnLongClickListener(new OnLongClickListener() {

                @Override
                public boolean onLongClick(View v) {
                    if (position == 0) {
                        Intent i = new Intent();
                        i.putExtra("IS_FROM_ORG", "Y");
                        ((BaseApplication) activity.getApplication()).getUIController().onIMOrgClickListener(activity, i,0);
                    } else if (position == 1) {
                        ((BaseApplication) activity.getApplication()).getUIController().onGroupListClickListener(activity);
                    } else if (position == 2) {
                        return true;
                    } else {
                        String adnameString = MFSPHelper.getString(CommConstants.EMPADNAME);
                        if (userInfo.getEmpAdname().equalsIgnoreCase(
                                adnameString)) {
                            return true;
                        }

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userInfo", userInfo);

                        ((BaseApplication) activity.getApplication()).getUIController().startPrivateChat(activity, bundle);
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return convertView;
    }

    class ViewHolderChild {

        ImageView icon;
        TextView name;
        TextView content;
        TextView title_content;
        TextView subName;
        CheckBox checkbox;
        RelativeLayout front;
        ImageView nofocus;
    }
}
