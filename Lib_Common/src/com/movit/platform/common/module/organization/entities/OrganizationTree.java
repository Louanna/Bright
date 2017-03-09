package com.movit.platform.common.module.organization.entities;

import java.io.Serializable;

import com.movit.platform.framework.view.tree.TreeNodeId;
import com.movit.platform.framework.view.tree.TreeNodeLabel;
import com.movit.platform.framework.view.tree.TreeNodePid;

public class OrganizationTree implements Serializable {
	private static final long serialVersionUID = 1L;
	@TreeNodeId
	String id;
	@TreeNodePid
	String parentId;
	@TreeNodeLabel
	String objname;
	String deltaFlag;

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getObjname() {
		return objname;
	}

	public void setObjname(String objname) {
		this.objname = objname;
	}

	public String getDeltaFlag() {
		return deltaFlag;
	}

	public void setDeltaFlag(String deltaFlag) {
		this.deltaFlag = deltaFlag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		OrganizationTree other = (OrganizationTree) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
