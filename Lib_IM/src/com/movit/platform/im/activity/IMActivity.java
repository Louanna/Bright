/*
 * Copyright (C) 2015 Bright Yu Haiyang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author: y.haiyang@qq.com
 */

package com.movit.platform.im.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.movit.platform.common.application.BaseApplication;
import com.movit.platform.im.R;
import com.movit.platform.im.constants.IMConstants;
import com.movit.platform.im.manager.GroupManager;
import com.movit.platform.im.module.group.activity.GroupListActivity;
import com.movit.platform.im.module.record.activity.ChatRecordsActivity;

public class IMActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_im);

		if (IMConstants.groupListDatas.isEmpty() || IMConstants.groupsMap.isEmpty()) {
			GroupManager.getInstance(this).getGroupList();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onTextViewClicked(View v) {
		int id = v.getId();
		if (id == R.id.tv_chat_list) {

			Intent intent = new Intent(IMActivity.this, ChatRecordsActivity.class);
			startActivity(intent);
			
		} else if (id == R.id.tv_group_list) {
			
			Intent intent = new Intent(IMActivity.this, GroupListActivity.class);
			startActivity(intent);
			
		} else if (id == R.id.tv_single_chat) {
			
			Intent intent = new Intent();
			intent.putExtra("IS_FROM_ORG", "Y");
			((BaseApplication) IMActivity.this.getApplication()).getUIController().onIMOrgClickListener(IMActivity.this, intent,0);

		} else if (id == R.id.tv_mult_chat) {

			Intent intent = new Intent();
			intent.putExtra("TITLE", getString(R.string.start_a_group_chat)).putExtra("ACTION", "GROUP");
			((BaseApplication) IMActivity.this.getApplication()).getUIController().onIMOrgClickListener(IMActivity.this, intent,0);
		}
	}

}
