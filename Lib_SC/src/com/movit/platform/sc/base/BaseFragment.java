package com.movit.platform.sc.base;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.movit.platform.framework.utils.DialogUtils;

public abstract class BaseFragment extends Fragment {
	
	protected View mRootView;
	protected DialogUtils progressDialogUtil;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		progressDialogUtil =  DialogUtils.getInstants();
	}

	@Override
	public View onCreateView(LayoutInflater inflater,
			@Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		initViews();
		initDatas();
		return mRootView;
	}

	protected abstract void initViews();
	protected abstract void initDatas();
	protected abstract void resumeDatas();
	
	public View findViewById(int id) {
		return mRootView.findViewById(id);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		resumeDatas();
	}

	@Override
	public void onDestroy() {
		progressDialogUtil.dismiss();
		progressDialogUtil = null;
		super.onDestroy();
	}

}
