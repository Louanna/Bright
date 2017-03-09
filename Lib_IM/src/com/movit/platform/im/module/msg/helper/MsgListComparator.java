package com.movit.platform.im.module.msg.helper;

import java.util.Comparator;

import com.movit.platform.common.entities.MessageBean;
import com.movit.platform.framework.utils.DateUtils;

public class MsgListComparator implements Comparator {

	@Override
	public int compare(Object lhs, Object rhs) {
		MessageBean msg1 = (MessageBean) lhs;
		MessageBean msg2 = (MessageBean) rhs;
		String time1 = msg1.getFormateTime();
		String time2 = msg2.getFormateTime();
		try {
			long da1 = DateUtils.str2Date(time1).getTime();
			long da2 = DateUtils.str2Date(time2).getTime();
			return (int) (da2 - da1);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}

}
