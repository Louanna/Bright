package com.movit.platform.common.helper;

import android.content.Context;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.entities.LoginInfo;
import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.helper.MFSPHelper;

public class CommonHelper {

    public CommonHelper(Context context) {
        super();
    }

    public void saveLoginConfig(LoginInfo loginConfig) {
        if (loginConfig == null) {
            return;
        }
        MFSPHelper.setString(CommConstants.USERNAME, loginConfig.getmUserInfo().getEmpAdname());
        MFSPHelper.setString(CommConstants.PASSWORD, loginConfig.getPassword());

        MFSPHelper.setString(CommConstants.EMPADNAME, loginConfig.getmUserInfo()
                .getEmpAdname().toLowerCase());
        MFSPHelper.setString(CommConstants.EMPCNAME, loginConfig.getmUserInfo()
                .getEmpCname());
        MFSPHelper.setString(CommConstants.EMPID, loginConfig.getmUserInfo().getEmpId());

        MFSPHelper.setString(CommConstants.GENDER, loginConfig.getmUserInfo()
                .getGender());
        MFSPHelper.setString(CommConstants.AVATAR, loginConfig.getmUserInfo()
                .getAvatar());
        MFSPHelper.setString(CommConstants.TOKEN, loginConfig.getmUserInfo()
                .getOpenFireToken());
        MFSPHelper.setString(CommConstants.USERID, loginConfig.getmUserInfo().getId());
        MFSPHelper.setInteger(CommConstants.CALL_COUNT, loginConfig.getmUserInfo().getCallCount());
        MFSPHelper.setString(CommConstants.EMAIL_ADDRESS,loginConfig.getmUserInfo().getMail());
        MFSPHelper.setString(CommConstants.MINGYUANNO,loginConfig.getmUserInfo().getMingyuanNo());
    }

    public LoginInfo getLoginConfig() {
        LoginInfo loginConfig = new LoginInfo();
        loginConfig.setPassword(MFSPHelper.getString(CommConstants.PASSWORD));
        UserInfo userInfo = new UserInfo();
        userInfo.setEmpId(MFSPHelper.getString(CommConstants.EMPID));
        userInfo.setEmpAdname(MFSPHelper.getString(CommConstants.EMPADNAME));
        userInfo.setEmpCname(MFSPHelper.getString(CommConstants.EMPCNAME));
        userInfo.setAvatar(MFSPHelper.getString(CommConstants.AVATAR));
        userInfo.setGender(MFSPHelper.getString(CommConstants.GENDER));
        userInfo.setOpenFireToken(MFSPHelper.getString(CommConstants.TOKEN));
        userInfo.setId(MFSPHelper.getString(CommConstants.USERID));
        userInfo.setCallCount(MFSPHelper.getInteger(CommConstants.CALL_COUNT));
        userInfo.setMail(MFSPHelper.getString(CommConstants.EMAIL_ADDRESS));
        userInfo.setMingyuanNo(MFSPHelper.getString(CommConstants.MINGYUANNO));
        loginConfig.setmUserInfo(userInfo);

        return loginConfig;
    }

}
