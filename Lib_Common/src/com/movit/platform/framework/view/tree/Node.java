package com.movit.platform.framework.view.tree;

import java.util.ArrayList;
import java.util.List;

import com.movit.platform.common.module.organization.entities.OrganizationBean;

/**
 * http://blog.csdn.net/lmj623565791/article/details/40212367
 * 
 * @author zhy
 * 
 */
public class Node {

	private String id;
	/**
	 * 根节点pId为0
	 */
	private String pId = "0";

	private OrganizationBean bean;

	/**
	 * 当前的级别
	 */
	private int level;

	/**
	 * 是否展开
	 */
	private boolean isExpand = false;

	private int icon;

	/**
	 * 下一级的子Node
	 */
	private List<Node> children = new ArrayList<Node>();

	/**
	 * 父Node
	 */
	private Node parent;

	private int checked = 0;

	public Node() {
	}

	public Node(String id, String pId, OrganizationBean bean) {
		super();
		this.id = id;
		this.pId = pId;
		this.bean = bean;
	}

	public int getIcon() {
		return icon;
	}

	public void setIcon(int icon) {
		this.icon = icon;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getpId() {
		return pId;
	}

	public void setpId(String pId) {
		this.pId = pId;
	}

	public OrganizationBean getBean() {
		return bean;
	}

	public void setBean(OrganizationBean bean) {
		this.bean = bean;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public boolean isExpand() {
		return isExpand;
	}

	public List<Node> getChildren() {
		return children;
	}

	public void setChildren(List<Node> children) {
		this.children = children;
	}

	public Node getParent() {
		return parent;
	}

	public void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * 是否为跟节点
	 * 
	 * @return
	 */
	public boolean isRoot() {
		return parent == null;
	}

	/**
	 * 判断父节点是否展开
	 * 
	 * @return
	 */
	public boolean isParentExpand() {
		if (parent == null)
			return false;
		return parent.isExpand();
	}

	/**
	 * 是否是叶子界点
	 * 
	 * @return
	 */
	public boolean isLeaf() {
		return children.size() == 0;
	}

	/**
	 * 获取level
	 */
	public int getLevel() {
		return parent == null ? 0 : parent.getLevel() + 1;
	}

	/**
	 * 设置展开
	 * 
	 * @param isExpand
	 */
	public void setExpand(boolean isExpand) {
		this.isExpand = isExpand;
		if (!isExpand) {

			for (Node node : children) {
				node.setExpand(isExpand);
			}
		}
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
		Node other = (Node) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int getChecked() {
		return checked;
	}

	/**
	 * @param checked
	 *            0未选中，1选中,2不可选
	 */
	public void setChecked(int checked) {
		// if (children.isEmpty() && bean.getUserInfo() == null) {
		// // 没有子部门和人的时候
		// return;
		// }

		if (getCount() == 0 && bean.getUserInfo() == null) {
			return;
		}
		this.checked = checked;
		for (Node node : children) {
			if (node.checked != 2) {
				node.setChecked(checked);
			}
		}
	}

	/**
	 * @param checked
	 *            0未选中，1选中,2不可选
	 */
	public void setParentChecked(int checked) {

		if (getCount() == 0 && bean.getUserInfo() == null) {
			return;
		}

		if (parent != null) {
			boolean flag = true;
			for (Node node : parent.children) {
				if (node.id.equals(this.id)) {
					continue;
				}
				if (checked == 0) {// 取消勾

				} else if (checked == 1) {// 打勾
					if (node.getCount() == 0
							&& node.getBean().getUserInfo() == null) {
						continue;
					}
					if (node.getChecked() == 0) {
						flag = false;
					}

				} else if (checked == 2) {
					if (node.getChecked() != checked) {
						flag = false;
					}
				}

			}
			if (flag) {
				parent.checked = checked;
				parent.setParentChecked(checked);
			}

		}
	}

	public int getCount() {
		int count = 0;
		for (Node n : children) {
			if (n.getBean().getUserInfo() != null) {
				count++;
			} else {
				count += n.getCount();
			}
		}
		return count;
	}
}
