package com.movit.platform.im.module.group.entities;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.movit.platform.common.module.user.entities.UserInfo;

public class Group implements Serializable {
	private static final long serialVersionUID = 1L;
	List<UserInfo> members;
	String delflg;
	String groupName;
	String createrId;
	String[] adminIds;
	String roomServerName;
	String imServerName;
	String id;
	int type;
	String displayName;
	String description;

	List<GroupLog> joinGroupLog;
	String dissolveDate;
	String createDate;

	@Override
	public String toString() {
		return "Group [members=" + members + ", delflg=" + delflg
				+ ", groupName=" + groupName + ", createrId=" + createrId
				+ ", adminIds=" + Arrays.toString(adminIds)
				+ ", roomServerName=" + roomServerName + ", imServerName="
				+ imServerName + ", id=" + id + ", type=" + type
				+ ", displayName=" + displayName + ", description="
				+ description + ", joinGroupLog=" + joinGroupLog
				+ ", dissolveDate=" + dissolveDate + ", createDate="
				+ createDate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Group other = (Group) obj;
		if (this.groupName.equalsIgnoreCase(other.getGroupName())) {
			return true;
		}
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equalsIgnoreCase(other.groupName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getDelflg() {
		return delflg;
	}

	public void setDelflg(String delflg) {
		this.delflg = delflg;
	}

	public List<UserInfo> getMembers() {
		return members;
	}

	public void setMembers(List<UserInfo> members) {
		this.members = members;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getCreaterId() {
		return createrId;
	}

	public void setCreaterId(String createrId) {
		this.createrId = createrId;
	}

	public String[] getAdminIds() {
		return adminIds;
	}

	public void setAdminIds(String[] adminIds) {
		this.adminIds = adminIds;
	}

	public String getRoomServerName() {
		return roomServerName;
	}

	public void setRoomServerName(String roomServerName) {
		this.roomServerName = roomServerName;
	}

	public String getImServerName() {
		return imServerName;
	}

	public void setImServerName(String imServerName) {
		this.imServerName = imServerName;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<GroupLog> getJoinGroupLog() {
		return joinGroupLog;
	}

	public void setJoinGroupLog(List<GroupLog> joinGroupLog) {
		this.joinGroupLog = joinGroupLog;
	}

	public String getDissolveDate() {
		return dissolveDate;
	}

	public void setDissolveDate(String dissolveDate) {
		this.dissolveDate = dissolveDate;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

}
