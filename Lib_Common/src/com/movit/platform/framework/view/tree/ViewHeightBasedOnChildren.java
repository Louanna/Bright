package com.movit.platform.framework.view.tree;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;

public class ViewHeightBasedOnChildren {
	Context context;

	public ViewHeightBasedOnChildren(Context context) {
		super();
		this.context = context;
	}

	@SuppressLint("NewApi")
	public void setListViewHeightBasedOnChildren(GridView gridView) {
		// 获取ListView对应的Adapter
		ListAdapter listAdapter = gridView.getAdapter();
		if (listAdapter == null) {
			return;
		}
		int totalHeight = 0;
		int count = listAdapter.getCount();

		// if (ctype == CommConstants.CHAT_TYPE_GROUP) {
		// SharedPreferencesUtil spUtil = new SharedPreferencesUtil(context);
		// String userId = spUtil.getString(CommConstants.USERID);
		// if (!group.getCreaterId().equals(userId)) {
		// count = count -1;
		// }
		// }

		int h = 0;// 行数
		int j = (int) (count / 4);
		int k = (int) (count % 4);
		if (k > 0) {
			h = j + 1;
		} else {
			h = j;
		}
		for (int i = 0, len = h; i < len; i++) {
			View listItem = listAdapter.getView(i, null, gridView);
			listItem.measure(0, 0); // 计算子项View 的宽高
			totalHeight += listItem.getMeasuredHeight(); // 统计所有子项的总高度
		}
		int space = 0;
		try {
//			if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
//				space = gridView.getVerticalSpacing();
//			} else {
				space = dip2px(context, 5f);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ViewGroup.LayoutParams params = gridView.getLayoutParams();
		params.height = totalHeight + (space * (h - 1));
		// listView.getDividerHeight()获取子项间分隔符占用的高度
		// params.height最后得到整个ListView完整显示需要的高度
		gridView.setLayoutParams(params);
	}

	public int dip2px(Context context, float dpValue) {
		if (context == null) {
			return 0;
		}
		float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}
}
