package com.movit.platform.im.module.location;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;
import com.movit.platform.im.widget.popuplist.ScreenUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

/**
 * Created by air on 16/5/12.
 * map view
 */
public class MapViewActivity extends IMBaseActivity {

    Context context;
    MapView mMapView = null;
    BaiduMap mBaiduMap;

    // 定位相关Ø
    LocationClient mLocClient;
    boolean isFirstLoc = true; // 是否首次定位

    private double latitude;
    private double longitude;

    private TextView tvLocation;
    private TextView tvRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_view);
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
        ImageView tvLeft = (ImageView) findViewById(R.id.common_top_img_left);
        tvRight = (TextView) findViewById(R.id.common_top_close);
        tvRight.setText(R.string.chat_send);
        tvRight.setVisibility(View.VISIBLE);
        tvRight.setEnabled(true);
        tvRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvRight.setEnabled(false);
                int imageWidth = (int) (ScreenUtils.getScreenWidth(context) * 0.6);
                int x = mBaiduMap.getMapStatus().targetScreen.x - imageWidth / 2;
                int y = mBaiduMap.getMapStatus().targetScreen.y - imageWidth / 2;
                mBaiduMap.snapshotScope(new Rect(x, y, x + imageWidth, y + imageWidth), new BaiduMap.SnapshotReadyCallback() {
                    public void onSnapshotReady(Bitmap snapshot) {
                        Bitmap front = BitmapFactory.decodeResource(getResources(), R.drawable.add_location);
                        int iconWidth = front.getWidth();
                        int iconHeight = front.getHeight();
                        //取得底层的宽高
                        int bgWidth = snapshot.getWidth();
                        int bgHeight = snapshot.getHeight();

                        //创建新bitmap
                        Bitmap newBmp = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
                        Canvas cv = new Canvas(newBmp);
                        cv.drawBitmap(snapshot, 0, 0, null);//在 0，0坐标开始画入bg
                        cv.drawBitmap(front, bgWidth / 2 - iconWidth / 2, bgHeight / 2 - iconHeight, null);//在 0，0坐标开始画入fg ，可以从任意位置画入
                        cv.save(Canvas.ALL_SAVE_FLAG);//保存
                        cv.restore();//存储

                        String filePath = CommConstants.SD_DATA_PIC + Calendar.getInstance().getTimeInMillis() + ".png";
                        File file = new File(filePath);
                        FileOutputStream out;
                        try {
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            out = new FileOutputStream(file);
                            if (newBmp.compress(
                                    Bitmap.CompressFormat.PNG, 100, out)) {
                                out.flush();
                                out.close();
                            }

                            Intent intent = new Intent();
                            intent.putExtra("filePath", filePath);
                            intent.putExtra("addStr", tvLocation.getText());
                            intent.putExtra("latitude", latitude);
                            intent.putExtra("longitude", longitude);
                            setResult(RESULT_OK, intent);
                            finish();
                        } catch (IOException e) {
                            e.printStackTrace();
                            tvRight.setEnabled(true);
                        }
                    }
                });
            }
        });
        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void initMapAndLocation() {
        mBaiduMap = mMapView.getMap();
        mMapView.showZoomControls(false);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
        // 定位初始化
        mLocClient = new LocationClient(context);
        mLocClient.registerLocationListener(new MyLocationListenner());
        LocationClientOption option = new LocationClientOption();
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);// 打开gps
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListenner implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                ToastUtils.showToast(getApplicationContext(), getString(R.string.chat_location_fail));
                mLocClient.stop();
                return;
            }

            if (location.getLocType() != 161 && location.getLocType() != 61
                    && location.getLocType() != 65 && location.getLocType() != 66) {
                ToastUtils.showToast(getApplicationContext(), getString(R.string.chat_location_fail));
                mLocClient.stop();
                return;
            }

            tvRight.setEnabled(true);

            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(0)
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(0).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(),
                        location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Message msg = new Message();
                Bundle bundle = new Bundle();
                bundle.putString("address",location.getAddrStr());
                msg.setData(bundle);
                myHandler.sendMessage(msg);
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {

        }
    }

    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tvLocation.setText(msg.getData().getString("address"));
        }
    };

    BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus) {
        }

        @Override
        public void onMapStatusChange(MapStatus mapStatus) {
        }

        @Override
        public void onMapStatusChangeFinish(MapStatus mapStatus) {
            LatLng latlng = mBaiduMap.getMapStatus().target;
            latitude = latlng.latitude;
            longitude = latlng.longitude;
            GeoCoder geoCoder = GeoCoder.newInstance();
            geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
                @Override
                public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {

                }

                @Override
                public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
                    tvLocation.setText(reverseGeoCodeResult.getAddress());
                }
            });
            geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(latlng));
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        // 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
        if (mLocClient != null) {
            mLocClient.start();
        }
    }

    @Override
    public void onDestroy() {

        // 退出时销毁定位
        mLocClient.stop();
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
        if (mLocClient != null && mLocClient.isStarted()) {
            mLocClient.stop();
        }
        super.onPause();
    }
}
