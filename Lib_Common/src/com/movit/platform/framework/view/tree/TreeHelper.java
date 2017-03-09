package com.movit.platform.framework.view.tree;

import android.content.pm.PackageManager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import com.movit.platform.common.R;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.common.module.organization.entities.OrganizationBean;

/**
 * http://blog.csdn.net/lmj623565791/article/details/40212367
 * 
 * @author zhy
 * 
 */
public class TreeHelper {
	/**
	 * 传入我们的普通bean，转化为我们排序后的Node
	 * 
	 * @param datas
	 * @param defaultExpandLevel
	 * @return
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public static <T> List<Node> getSortedNodes(List<T> userNodes,
			List<T> orgNodes, int defaultExpandLevel)
			throws IllegalArgumentException, IllegalAccessException {
		List<Node> result = new ArrayList<Node>();

		List<Node> nodes = convertDatas((List<Node>) userNodes,
				(List<Node>) orgNodes);

		// 拿到根节点
		List<Node> rootNodes = getRootNodes(nodes);
		// 排序以及设置Node间关系
		for (Node node : rootNodes) {
			addNode(result, node, defaultExpandLevel, 1);
		}
		return result;
	}

	public static List<Node> convertDatas(List<Node> userNodes,
			List<Node> orgNodes) {
		/**
		 * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
		 */
		for (Node n : userNodes) {
			for (Node m : orgNodes) {
				if (m.getId().equals(n.getpId())) {
					m.getChildren().add(n);
					n.setParent(m);
					break;
				}
			}
		}
		for (int i = 0; i < orgNodes.size(); i++) {
			Node n = orgNodes.get(i);
			for (int j = i + 1; j < orgNodes.size(); j++) {
				Node m = orgNodes.get(j);
				if (m.getpId().equals(n.getId())) {
					n.getChildren().add(m);
					m.setParent(n);
				} else if (m.getId().equals(n.getpId())) {
					m.getChildren().add(n);
					n.setParent(m);
				}
			}
		}
		List<Node> nodes = new ArrayList<Node>();
		nodes.addAll(userNodes);
		nodes.addAll(orgNodes);
		// 设置图片
		for (Node n : nodes) {
			setNodeIcon(n);
		}
		return nodes;
	}

	/**
	 * 过滤出所有可见的Node
	 * 
	 * @param nodes
	 * @return
	 */
	public static List<Node> filterVisibleNode(List<Node> nodes) {
		List<Node> result = new ArrayList<Node>();

		for (Node node : nodes) {
			// 如果为跟节点，或者上层目录为展开状态
			if (node.isRoot() || node.isParentExpand()) {
				setNodeIcon(node);
				result.add(node);
			}
		}
		return result;
	}

	/**
	 * 将我们的数据转化为树的节点
	 * 
	 * @param datas
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 */
	private static <T> List<Node> convetData2Node(List<T> datas)
			throws IllegalArgumentException, IllegalAccessException

	{
		List<Node> nodes = new ArrayList<Node>();
		Node node = null;

		for (T t : datas) {
			// 转化为自己的bean
			String id = null;
			String pId = null;
			String label = null;
			Class<? extends Object> clazz = t.getClass();
			Field[] declaredFields = clazz.getDeclaredFields();
			for (Field f : declaredFields) {
				if (f.getAnnotation(TreeNodeId.class) != null) {
					f.setAccessible(true);
					id = (String) f.get(t);
				}
				if (f.getAnnotation(TreeNodePid.class) != null) {
					f.setAccessible(true);
					pId = (String) f.get(t);
				}
				if (f.getAnnotation(TreeNodeLabel.class) != null) {
					f.setAccessible(true);
					label = (String) f.get(t);
				}
				if (id != null && pId != null && label != null) {
					break;
				}
			}
			OrganizationBean bean = new OrganizationBean(label, null);
			node = new Node(id, pId, bean);
			nodes.add(node);
		}

		/**
		 * 设置Node间，父子关系;让每两个节点都比较一次，即可设置其中的关系
		 */
		for (int i = 0; i < nodes.size(); i++) {
			Node n = nodes.get(i);
			for (int j = i + 1; j < nodes.size(); j++) {
				Node m = nodes.get(j);
				if (m.getpId().equals(n.getId())) {
					n.getChildren().add(m);
					m.setParent(n);
				} else if (m.getId().equals(n.getpId())) {
					m.getChildren().add(n);
					n.setParent(m);
				}
			}
		}

		// 设置图片
		for (Node n : nodes) {
			setNodeIcon(n);
		}
		return nodes;
	}

	private static List<Node> getRootNodes(List<Node> nodes) {
		List<Node> root = new ArrayList<Node>();
		for (Node node : nodes) {
			if (node.isRoot())
				root.add(node);
		}
		return root;
	}

	/**
	 * 把一个节点上的所有的内容都挂上去
	 */
	private static void addNode(List<Node> nodes, Node node,
			int defaultExpandLeval, int currentLevel) {

		nodes.add(node);
		if (defaultExpandLeval >= currentLevel) {
			node.setExpand(true);
		}

		if (node.isLeaf())
			return;
		for (int i = 0; i < node.getChildren().size(); i++) {
			addNode(nodes, node.getChildren().get(i), defaultExpandLeval,
					currentLevel + 1);
		}
	}

	/**
	 * 设置节点的图标
	 * 
	 * @param node
	 */
	private static void setNodeIcon(Node node) {

		if (node.getChildren().size() > 0 && node.isExpand()) {

			if (CommConstants.ORG_TREE) {

				if (node.getLevel() == 0) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 1) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 2) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 3) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 4) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 5) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 6) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 7) {
					node.setIcon(R.drawable.tree_expand1);
				} else if (node.getLevel() == 8) {
					node.setIcon(R.drawable.tree_expand1);
				} else {
					node.setIcon(R.drawable.tree_expand1);
				}
			}else{
				node.setIcon(R.drawable.tree_expand1);
			}

		} else if (node.getChildren().size() > 0 && !node.isExpand()) {
			node.setIcon(R.drawable.tree_close_0);
		} else
			node.setIcon(R.drawable.tree_close_1);
	}

}
