package com.movitech.EOP.module.workbench.meeting.model;

import java.io.Serializable;

public class Meeting implements Serializable {

	private static final long serialVersionUID = 1L;
	private int id;
	private String mettingTitle;
	private String mettingDate;
	private String meetingbeginDate;
	private String meetingendDate;
	private String meetingUser;
	private String meetingCompany;
	private String meetingroom;
	private boolean isShowing;

	public boolean isShowing() {
		return isShowing;
	}

	public void setShowing(boolean isShowing) {
		this.isShowing = isShowing;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getMettingTitle() {
		return mettingTitle;
	}

	public void setMettingTitle(String mettingTitle) {
		this.mettingTitle = mettingTitle;
	}

	public String getMettingDate() {
		return mettingDate;
	}

	public void setMettingDate(String mettingDate) {
		this.mettingDate = mettingDate;
	}

	public String getMeetingbeginDate() {
		return meetingbeginDate;
	}

	public void setMeetingbeginDate(String meetingbeginDate) {
		this.meetingbeginDate = meetingbeginDate;
	}

	public String getMeetingendDate() {
		return meetingendDate;
	}

	public void setMeetingendDate(String meetingendDate) {
		this.meetingendDate = meetingendDate;
	}

	public String getMeetingUser() {
		return meetingUser;
	}

	public void setMeetingUser(String meetingUser) {
		this.meetingUser = meetingUser;
	}

	public String getMeetingCompany() {
		return meetingCompany;
	}

	public void setMeetingCompany(String meetingCompany) {
		this.meetingCompany = meetingCompany;
	}

	public String getMeetingroom() {
		return meetingroom;
	}

	public void setMeetingroom(String meetingroom) {
		this.meetingroom = meetingroom;
	}

}
