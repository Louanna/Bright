package com.movit.platform.im.utils;

import com.movit.platform.common.module.user.entities.UserInfo;
import com.movit.platform.framework.utils.Pinyin4jUtils;

public class BuildQueryString {

	public static String buildQueryName(String name) {
		try {
			String pinyin = Pinyin4jUtils.getFullPinYin(name);
			String jianpin = Pinyin4jUtils.getFirstPinYin(name);
			return name+(pinyin + "," + jianpin).toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public static String buildQueryName(UserInfo userInfo) {
		try {
			String name = userInfo.getEmpCname();
			String pinyin = Pinyin4jUtils.getFullPinYin(name);
			String jianpin = Pinyin4jUtils.getFirstPinYin(name);
			return (userInfo.getEmpCname() + "," + userInfo.getEmpAdname()
					+ "," + pinyin + "," + jianpin).toUpperCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

}
