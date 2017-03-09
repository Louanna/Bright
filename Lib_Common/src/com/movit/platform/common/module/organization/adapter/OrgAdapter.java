package com.movit.platform.common.module.organization.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.R;
import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.activity.OrgActivity;
import com.movit.platform.common.module.organization.entities.OrganizationTree;
import com.movit.platform.common.module.user.db.UserDao;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.CusDialog;
import com.movit.platform.framework.view.tree.Node;
import com.movit.platform.framework.view.tree.TreeListViewAdapter;
import com.movit.platform.framework.widget.CircleImageView;

import java.util.List;

public class OrgAdapter<T> extends TreeListViewAdapter<T> {
    private AQuery aq;
    private String isFromOrg;
    private String action;
    private OrgActivity orgActivity;

    public OrgAdapter(ListView mTree, Context context,
                      List<T> userNodes, List<T> orgNodes, int defaultExpandLevel,
                      String isFromOrg, OrgActivity iChat) throws IllegalArgumentException,
            IllegalAccessException {
        super(mTree, context, userNodes, orgNodes, defaultExpandLevel);
        aq = new AQuery(context);
        this.isFromOrg = isFromOrg;
        this.orgActivity = iChat;
    }

    public void setAction(String action){
        this.action = action;
    }

    @Override
    public View getConvertView(Node node, int position, View convertView,
                               ViewGroup parent) {
        if (node.isRoot()) {
            convertView = initGroupItem(convertView, position, node, parent);
        } else {
            if (node.isLeaf() && node.getBean().getUserInfo() != null) {
                convertView = initChildItem(convertView, position, node, parent);
            } else {
                convertView = initGroupItem(convertView, position, node, parent);
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        CheckBox checkBox;
        TextView name;
        TextView content;
        ImageView expandOrClase;
        ImageView treeFlag;
    }

    private static class ViewHolderChild {
        CheckBox checkBox;
        CircleImageView avatar;
        TextView name;
        TextView content;
    }

    public View initChildItem(View convertView, int postion, final Node node,
                              ViewGroup parent) {
        ViewHolderChild holderChild = new ViewHolderChild();
        convertView = mInflater.inflate(
                R.layout.comm_item_organization_expandlist, parent, false);
        holderChild.checkBox = (CheckBox) convertView
                .findViewById(R.id.item_child_checkbox);
        holderChild.avatar = (CircleImageView) convertView
                .findViewById(R.id.item_child_avatar);
        holderChild.name = (TextView) convertView
                .findViewById(R.id.item_child_value1);
        holderChild.content = (TextView) convertView
                .findViewById(R.id.item_child_value2);
        convertView.setTag(holderChild);

        AQuery aQuery = aq.recycle(convertView);
        final UserInfo userInfo = node.getBean().getUserInfo();
        if (userInfo != null) {

            int picId = R.drawable.avatar_male;
            if (mContext.getString(R.string.boy).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_male;
            } else if (mContext.getString(R.string.girl).equals(userInfo.getGender())) {
                picId = R.drawable.avatar_female;
            }
            String uname = MFSPHelper.getString(CommConstants.AVATAR);
            final String adname = MFSPHelper.getString(CommConstants.EMPADNAME);
            String avatarUrl = "";
            String avatarName = userInfo.getAvatar();
            if (StringUtils.notEmpty(avatarName)) {
                avatarUrl = avatarName;
            }
            if (adname.equalsIgnoreCase(userInfo.getEmpAdname())
                    && StringUtils.notEmpty(uname)) {
                avatarUrl = uname;
            }
            if (StringUtils.notEmpty(avatarUrl)) {
                BitmapAjaxCallback callback = new BitmapAjaxCallback();
                callback.animation(AQuery.FADE_IN_NETWORK).rotate(true)
                        .round(10).fallback(picId)
                        .url(CommConstants.URL_DOWN + avatarUrl).memCache(true)
                        .fileCache(true).targetWidth(128);
                aQuery.id(holderChild.avatar).image(callback);
            } else {
                Bitmap bitmap = PicUtils.getRoundedCornerBitmap(mContext, picId,
                        10);
                holderChild.avatar.setImageBitmap(bitmap);
            }

            holderChild.name.setText(userInfo.getEmpCname());
            holderChild.content.setText(userInfo.getEmpAdname());

            holderChild.checkBox.setEnabled(true);
            if (node.getChecked() == 0) {
                holderChild.checkBox.setChecked(false);
            } else if (node.getChecked() == 1) {
                holderChild.checkBox.setChecked(true);
            } else if (node.getChecked() == 2) {
                holderChild.checkBox.setEnabled(false);
            }

            if ("Y".equalsIgnoreCase(isFromOrg)) {
                holderChild.checkBox.setVisibility(View.GONE);
                convertView.setOnLongClickListener(new OnLongClickListener() {

                    @Override
                    public boolean onLongClick(View v) {
                        if (userInfo.getEmpAdname().equalsIgnoreCase(adname)) {
                            return true;
                        }

                        Bundle bundle = new Bundle();
                        bundle.putSerializable("userInfo", userInfo);

                        ((BaseApplication) orgActivity.getApplication()).getUIController().startPrivateChat(orgActivity, bundle);
                        return true;
                    }
                });
                convertView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent();
                        intent.putExtra("userInfo", userInfo);
                        ((BaseApplication) orgActivity.getApplication()).getUIController().onOwnHeadClickListener(orgActivity, intent, 0);
                    }
                });
            } else {
                if("forward".equals(action)){
                    holderChild.checkBox.setVisibility(View.GONE);
                    convertView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            UserInfo u = CommConstants.loginConfig.getmUserInfo();
                            if(userInfo.getId().equals(u.getId())){
                                ToastUtils.showToast(mContext,mContext.getString(R.string.chat_file_no_forward_self));
                                return;
                            }
                            final CusDialog cusDialog = CusDialog.getInstance();
                            cusDialog.showVersionDialog(mContext);
                            String str = String.format(mContext.getString(R.string.chat_file_forward_to),userInfo.getEmpCname());
                            cusDialog.setWebDialog(str);
                            cusDialog.setCancleClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cusDialog.dismiss();
                                }
                            });
                            cusDialog.setConfirmClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    orgActivity.setResult(userInfo);
                                    cusDialog.dismiss();
                                }
                            });
                        }
                    });
                }else {
                    holderChild.checkBox.setVisibility(View.VISIBLE);
                    convertView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            CheckBox checkBox = (CheckBox) v
                                    .findViewById(R.id.item_child_checkbox);
                            checkBox.callOnClick();
                        }
                    });
                }
            }
        }

        holderChild.checkBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (node.getChecked() == 0) {
                    node.setChecked(1);
                    node.setParentChecked(1);
                } else if (node.getChecked() == 1) {
                    node.setChecked(0);
                    node.setParentChecked(0);
                }

                notifyDataSetChanged();
                bindDataFromNode();
            }
        });

        return convertView;
    }

    public View initGroupItem(View convertView, int postion, final Node node,
                              ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        convertView = mInflater.inflate(
                R.layout.comm_item_organization, parent, false);
        holder.checkBox = (CheckBox) convertView
                .findViewById(R.id.item_group_checkbox);
        holder.name = (TextView) convertView
                .findViewById(R.id.item_group_value1);
        holder.content = (TextView) convertView
                .findViewById(R.id.item_group_value2);
        holder.expandOrClase = (ImageView) convertView
                .findViewById(R.id.item_group_flagicon);
        holder.treeFlag = (ImageView) convertView
                .findViewById(R.id.item_group_tree_flag);
        convertView.setTag(holder);
        boolean orgTree = false;
        try {
            orgTree = mContext.getPackageManager().getApplicationInfo(
                    mContext.getPackageName(), PackageManager.GET_META_DATA).metaData
                    .getBoolean("CHANNEL_ORG_TREE", false);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (orgTree) {
            if (node.getLevel() == 0) {
                holder.treeFlag.setImageResource(R.drawable.tree_1_flag);
            } else if (node.getLevel() == 1) {
                holder.treeFlag.setImageResource(R.drawable.tree_2_flag);
            } else if (node.getLevel() == 2) {
                holder.treeFlag.setImageResource(R.drawable.tree_3_flag);
            } else if (node.getLevel() == 3) {
                holder.treeFlag.setImageResource(R.drawable.tree_4_flag);
            } else if (node.getLevel() == 4) {
                holder.treeFlag.setImageResource(R.drawable.tree_5_flag);
            } else if (node.getLevel() == 5) {
                holder.treeFlag.setImageResource(R.drawable.tree_6_flag);
            } else if (node.getLevel() == 6) {
                holder.treeFlag.setImageResource(R.drawable.tree_7_flag);
            } else if (node.getLevel() == 7) {
                holder.treeFlag.setImageResource(R.drawable.tree_8_flag);
            } else if (node.getLevel() == 8) {
                holder.treeFlag.setImageResource(R.drawable.tree_9_flag);
            }
        } else {
            holder.treeFlag.setVisibility(View.GONE);
        }

        if ("Y".equalsIgnoreCase(isFromOrg)||"forward".equals(action)) {
            holder.checkBox.setVisibility(View.GONE);
        } else {
            holder.checkBox.setVisibility(View.VISIBLE);
        }

        if (node.getCount() != 0) {
            holder.content.setText(node.getCount() + mContext.getString(R.string.people));
        } else {
            holder.content.setVisibility(View.GONE);
        }
        holder.name.setText(node.getBean().getObjName());
        holder.expandOrClase.setImageResource(node.getIcon());

        holder.checkBox.setEnabled(true);
        if (node.getChecked() == 0) {
            holder.checkBox.setChecked(false);
        } else if (node.getChecked() == 1) {
            holder.checkBox.setChecked(true);
        } else if (node.getChecked() == 2) {
            holder.checkBox.setEnabled(false);
        }

        holder.checkBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (node.getChecked() == 0) {
                    node.setChecked(1);
                    node.setParentChecked(1);
                } else if (node.getChecked() == 1) {
                    node.setChecked(0);
                    node.setParentChecked(0);
                }
                notifyDataSetChanged();
                bindDataFromNode();
            }
        });
        return convertView;
    }

    @Override
    public void checkNodeStatus(Node node) {
        if (OrgActivity.orgCheckedCatMap != null
                && OrgActivity.orgCheckedCatMap.containsKey(node
                .getBean().getObjName())) {
            node.setChecked(1);
            node.setParentChecked(1);
        }
        if (OrgActivity.orgCheckedMap != null) {
            UserInfo userInfo = node.getBean().getUserInfo();
            if (userInfo != null) {
                if (OrgActivity.orgCheckedMap
                        .containsKey(userInfo.getEmpAdname())) {
                    node.setChecked(1);
                    node.setParentChecked(1);
                }
            }
        }

        if (OrgActivity.originalUserInfos != null
                && !OrgActivity.originalUserInfos.isEmpty()) {

            if (node.getBean().getUserInfo() != null
                    && OrgActivity.originalUserInfos
                    .contains(node.getBean().getUserInfo())) {
                node.setChecked(2);
                node.setParentChecked(2);
            }
        }

    }

    public void bindDataFromNode() {
        for (Node node : mAllNodes) {
            if (node.getChecked() == 1) {
                if (node.getBean().getUserInfo() != null) {
                    UserInfo userInfo = node.getBean().getUserInfo();
                    OrgActivity.orgCheckedMap.put(
                            userInfo.getEmpAdname(), userInfo);
                } else {
                    UserDao dao = UserDao.getInstance(mContext);
                    OrganizationTree orgu = dao.getOrganizationByName(node
                            .getBean().getObjName());
                    if (orgu != null) {
                        OrgActivity.orgCheckedCatMap.put(node
                                .getBean().getObjName(), orgu);
                    }
                }
            } else if (node.getChecked() == 0) {
                if (node.getBean().getUserInfo() != null) {
                    UserInfo userInfo = node.getBean().getUserInfo();
                    OrgActivity.orgCheckedMap.remove(userInfo
                            .getEmpAdname());
                } else {
                    OrgActivity.orgCheckedCatMap.remove(node
                            .getBean().getObjName());
                }
            }
        }
    }

    public void searchBindCheckData() {
        for (Node node : mAllNodes) {
            checkNodeStatus(node);
        }
        notifyDataSetChanged();
    }
}
