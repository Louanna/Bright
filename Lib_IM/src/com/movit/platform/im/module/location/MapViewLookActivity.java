package com.movit.platform.im.module.location;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;

/**
 * Created by air on 16/5/16.
 * look map View
 */
public class MapViewLookActivity extends IMBaseActivity {

    Context context;
    MapView mMapView = null;
    BaiduMap mBaiduMap;

    private double latitude;
    private double longitude;
    private String addrStr;

    private TextView tvLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view_look);
        initLocalView();
        initMapAndLocation();
    }

    public void initLocalView() {
        context = this;
        // 获取地图控件引用
        mMapView = (MapView) findViewById(R.id.mapView);
        tvLocation = (TextView) findViewById(R.id.tv_location);
        findViewById(R.id.common_top_img_right).setVisibility(View.GONE);
        TextView tvTitle = (TextView) findViewById(R.id.common_top_title);
        tvTitle.setText(R.string.chat_menu_send_location);
        ImageView ivLeft = (ImageView) findViewById(R.id.common_top_img_left);
        ivLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        addrStr = getIntent().getStringExtra("addrStr");
        latitude = getIntent().getDoubleExtra("latitude",0f);
        longitude = getIntent().getDoubleExtra("longitude",0f);
    }

    public void initMapAndLocation() {
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false);

        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                .fromResource(R.drawable.add_location);
        LatLng ll = new LatLng(latitude,longitude);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(ll).zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(ll)
                .icon(mCurrentMarker);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);

        tvLocation.setText(addrStr);
    }

    /**
     * 定位SDK监听函数
     */

    @Override
    public void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    public void onDestroy() {
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }

    @Override
    public void onPause() {
        // 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
        super.onPause();
    }

}
