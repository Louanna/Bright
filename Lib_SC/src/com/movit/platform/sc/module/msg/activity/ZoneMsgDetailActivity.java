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

package com.movit.platform.sc.module.msg.activity;

import android.content.Intent;
import android.os.Message;
import android.view.View;

import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.DialogUtils;
import com.movit.platform.sc.R;
import com.movit.platform.sc.base.ZoneBaseActivity;
import com.movit.platform.sc.constants.SCConstants;
import com.movit.platform.sc.entities.Zone;

import java.util.ArrayList;
import java.util.Date;

public class ZoneMsgDetailActivity extends ZoneBaseActivity {

    private boolean isok = false;

    @Override
    protected void initTopView() {
        title.setText(getString(R.string.detail));
        topRight.setVisibility(View.GONE);
    }

    @Override
    protected void initDatas() {
        DialogUtils.getInstants().showLoadingDialog(this, getString(R.string.xlistview_header_hint_loading), false);
        Intent intent = getIntent();
        String sayid = intent.getStringExtra("sayId");
        zoneManager.getSay(sayid, handler);
    }

    @Override
    protected void setAdapter() {
//        adapter = new ZoneAdapter(this, zoneList, handler,
//                ZoneAdapter.TYPE_DETAIL, "", DialogUtils.getInstants(),zoneManager);
//        zoneListView.setAdapter(adapter);
//        zoneListView.setPullLoadEnable(false);
//        zoneListView.setPullRefreshEnable(false);
//        zoneListView.setXListViewListener(this);
//        zoneListView.stopRefresh();
//        zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
//        zoneListView.setOnItemClickListener(new OnItemClickListener() {
//
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//            }
//        });
    }

    @Override
    public void onRefresh() {
    }

    @Override
    public void onLoadMore() {
    }

    @Override
    public void onBackPressed() {
        if (isok) {
            setResult(1);
            finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void dealHandlers(Message msg) {
        switch (msg.what) {
            case SCConstants.ZONE_GET_RESULT:
                DialogUtils.getInstants().dismiss();
                ArrayList<Zone> arrayList = (ArrayList<Zone>) msg.obj;
                zoneList.clear();
                zoneList.addAll(arrayList);
                zoneListView.stopRefresh();
                zoneListView.setRefreshTime(DateUtils.date2Str(new Date()));
                if (arrayList.isEmpty()) {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    } else {
                        setAdapter();
                    }
                } else {
                    setAdapter();
                }
                isok = true;
                break;
            default:
                break;
        }

    }

    @Override
    protected void refreshData() {

    }

}
