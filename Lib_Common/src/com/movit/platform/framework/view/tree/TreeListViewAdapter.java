package com.movit.platform.framework.view.tree;

import java.util.List;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.movit.platform.common.constants.CommConstants;

/**
 * http://blog.csdn.net/lmj623565791/article/details/40212367
 * 
 * @author zhy
 * 
 * @param <T>
 */
public abstract class TreeListViewAdapter<T> extends BaseAdapter {

	protected Context mContext;
	/**
	 * 存储所有可见的Node
	 */
	protected List<Node> mNodes;
	protected LayoutInflater mInflater;
	/**
	 * 存储所有的Node
	 */
	protected List<Node> mAllNodes;

	/**
	 * 点击的回调接口
	 */
	private OnTreeNodeClickListener onTreeNodeClickListener;

	private int curLevel = 0;

	public interface OnTreeNodeClickListener {
		void onClick(Node node, int position);
	}

	public void setOnTreeNodeClickListener(
			OnTreeNodeClickListener onTreeNodeClickListener) {
		this.onTreeNodeClickListener = onTreeNodeClickListener;
	}

	/**
	 * 
	 * @param mTree
	 * @param context
	 * @param datas
	 * @param defaultExpandLevel
	 *            默认展开几级树
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 */
	public TreeListViewAdapter(final ListView mTree, Context context,
			List<T> userNodes, List<T> orgNodes, int defaultExpandLevel)
			throws IllegalArgumentException, IllegalAccessException {
		Log.v("TreeListViewAdapter", "------in------");
		mContext = context;
		/**
		 * 对所有的Node进行排序
		 */
		mAllNodes = TreeHelper.getSortedNodes(userNodes, orgNodes,
				defaultExpandLevel);
		for (Node node : mAllNodes) {
			checkNodeStatus(node);
		}
		/**
		 * 过滤出可见的Node
		 */
		mNodes = TreeHelper.filterVisibleNode(mAllNodes);

		mInflater = LayoutInflater.from(context);

		/**
		 * 设置节点点击时，可以展开以及关闭；并且将ItemClick事件继续往外公布
		 */
		mTree.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Node n = mNodes.get(position);
				expandOrCollapse(position);
				int sec = 0;
				for (int i = 0; i < mNodes.size(); i++) {
					Node m = mNodes.get(i);
					if (m.getId().equals(n.getId())) {
						sec = i;
					}
				}
				mTree.setSelection(sec);
			}

		});
		Log.v("TreeListViewAdapter", "------data--ready----");
	}

	/**
	 * 相应ListView的点击事件 展开或关闭某节点
	 * 
	 * @param position
	 */
	public void expandOrCollapse(int position) {
		Node n = mNodes.get(position);

		// 打开节点的时候，关闭同一级别的其他节点
		for (Node node : mNodes) {
			if (n.getLevel() == node.getLevel() && n.getId() != node.getId()) {
				if (node.isExpand()) {
					node.setExpand(!node.isExpand());
				}
			}
		}

		if (n != null)// 排除传入参数错误异常
		{
			if (!n.isLeaf()) {
				n.setExpand(!n.isExpand());
				mNodes = TreeHelper.filterVisibleNode(mAllNodes);
				if (n.isExpand()) {
					curLevel = n.getLevel();
				} else {
					curLevel = n.getLevel() - 1;
				}
				if (curLevel < 0) {
					curLevel = 0;
				}
				notifyDataSetChanged();// 刷新视图

				return;
			}
		}

		if (onTreeNodeClickListener != null) {
			onTreeNodeClickListener.onClick(mNodes.get(position), position);
		}
	}

	@Override
	public int getCount() {
		return mNodes.size();
	}

	@Override
	public Object getItem(int position) {
		return mNodes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Node node = mNodes.get(position);
		convertView = getConvertView(node, position, convertView, parent);

		if (CommConstants.ORG_TREE) {
			// 设置内边距
			if (node.getLevel() <= curLevel) {
				convertView.setPadding(0, 0, 0, 0);
			} else {
				convertView.setPadding(new ViewHeightBasedOnChildren(mContext)
						.dip2px(mContext, 30), 0, 0, 0);
			}

		} else {
			convertView.setPadding(
					node.getLevel()
							* new ViewHeightBasedOnChildren(mContext).dip2px(
									mContext, 20), 0, 0, 0);

		}

		return convertView;
	}

	public abstract View getConvertView(Node node, int position,
			View convertView, ViewGroup parent);

	public abstract void checkNodeStatus(Node node);
}
