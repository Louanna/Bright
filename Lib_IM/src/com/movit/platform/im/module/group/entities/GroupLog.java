package com.movit.platform.im.module.group.entities;

import java.io.Serializable;

public class GroupLog implements Serializable {
	private static final long serialVersionUID = 1L;
	String userId;
	String groupId;
	String leaveTime;
	String joinTime;
	String id;

	@Override
	public String toString() {
		return "GroupLog [userId=" + userId + ", groupId=" + groupId
				+ ", leaveTime=" + leaveTime + ", joinTime=" + joinTime
				+ ", id=" + id + "]";
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getLeaveTime() {
		return leaveTime;
	}

	public void setLeaveTime(String leaveTime) {
		this.leaveTime = leaveTime;
	}

	public String getJoinTime() {
		return joinTime;
	}

	public void setJoinTime(String joinTime) {
		this.joinTime = joinTime;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
