package com.movitech.EOP.module.events.model;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/29.
 *
 * 中奖人
 */
public class Winners implements Serializable {

    //姓名
    private String name;
    //手机号
    private String phone;
    //所属部门
    private String department;

    //登录名
    private String loginName;

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

}
