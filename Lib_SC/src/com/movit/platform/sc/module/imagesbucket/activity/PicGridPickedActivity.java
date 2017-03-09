package com.movit.platform.sc.module.imagesbucket.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.framework.view.viewpager.ImageViewPagerActivity;
import com.movit.platform.sc.R;
import com.movit.platform.sc.activity.SCBaseActivity;
import com.movit.platform.sc.module.imagesbucket.adapter.ImageGridAdapter;
import com.movit.platform.sc.module.imagesbucket.adapter.ImageGridAdapter.TextCallback;
import com.movit.platform.sc.module.imagesbucket.entities.ImageBucket;
import com.movit.platform.sc.module.imagesbucket.entities.ImageItem;
import com.movit.platform.sc.module.imagesbucket.helper.AlbumHelper;
import com.movit.platform.sc.module.zone.activity.ZonePublishActivity;

import java.util.List;

public class PicGridPickedActivity extends SCBaseActivity {
    TextView title;
    ImageView topLeft;
    TextView topRight;
    AlbumHelper helper;
    List<ImageItem> dataList;
    GridView gridView;
    ImageGridAdapter adapter;
    Button bt;
    public static final int CHOOSE_PICS_COUNT = 9;
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Toast.makeText(PicGridPickedActivity.this, getString(R.string.most_upload_9), Toast.LENGTH_LONG)
                            .show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_pic_grid_picked);
        helper = AlbumHelper.getHelper();
        helper.init(getApplicationContext());

        List<ImageBucket> buckets = helper.getImagesBucketList(false);
        int postion = getIntent().getIntExtra(
                PicBucketActivity.EXTRA_IMAGE_LIST_POSTION, 0);
        dataList = buckets.get(postion).imageList;

        initView();
    }

    private void initView() {
        title = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        gridView = (GridView) findViewById(R.id.image_picked_gridview);
        bt = (Button) findViewById(R.id.bt);

        title.setText(getString(R.string.picture));
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight.setText(getString(R.string.preview));
        adapter = new ImageGridAdapter(this, dataList, mHandler);
        gridView.setAdapter(adapter);
        adapter.setTextCallback(new TextCallback() {
            public void onListen(int count) {
                bt.setText(getString(R.string.complete) + "(" + count + "/9)");
                topRight.setText(getString(R.string.preview) + "(" + count + "/9)");
            }
        });

        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(PicGridPickedActivity.this,
                        ImageViewPagerActivity.class).putStringArrayListExtra(
                        "selectedImgs", ZonePublishActivity.selectImagesList)
                        .putExtra("FromBucket", true));
                overridePendingTransition(0, 0);
            }
        });
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                adapter.notifyDataSetChanged();
            }

        });

        bt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setResult(1);
                finish();
            }
        });
    }

    @Override
    public void onDestroy() {
        BitmapAjaxCallback.clearCache();
        super.onDestroy();
    }

}
