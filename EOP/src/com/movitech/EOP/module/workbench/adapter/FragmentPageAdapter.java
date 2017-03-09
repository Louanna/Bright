package com.movitech.EOP.module.workbench.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Qian Ping
 * @En_Name: Zoro Qian
 * @E-mail: qianping1220@gmail.com
 * @version: 1.0
 * @Created Time: 2015年6月12日 下午4:41:12
 * @Description: This is Zoro Qian's property.
 **/
public class FragmentPageAdapter extends FragmentStatePagerAdapter {

	List<Fragment> fragments = null;
	
	public FragmentPageAdapter(FragmentManager fm) {
		super(fm);
		fragments = new ArrayList<>();
	}

	public void addAll(List<Fragment> pages) {
		fragments.clear();
		fragments.addAll(pages);
		this.notifyDataSetChanged();
	}

	@Override
	public Fragment getItem(int arg0) {
		return fragments.get(arg0);
	}

	@Override
	public int getCount() {
		return fragments.size();
	}

}
