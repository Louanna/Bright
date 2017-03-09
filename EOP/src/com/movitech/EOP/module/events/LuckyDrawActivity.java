package com.movitech.EOP.module.events;

import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;
import com.movit.platform.framework.helper.MFSPHelper;
import com.movit.platform.framework.utils.ToastUtils;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.module.events.base.EventBaseActivity;
import com.movitech.EOP.module.events.manager.EventsManager;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * Created by Administrator on 2015/12/30.
 *
 */
public class LuckyDrawActivity extends EventBaseActivity {

    private ImageView image;
    private SensorManager sensorManager;
    private Sensor sensor;
    private Vibrator vibrator;
    /**  * 音效  */
    private SoundPool mSoundPool;
    /**  * 音频id  */
    private int mSoundId;
    private static final int UPTATE_INTERVAL_TIME = 50;
    private static final int SPEED_SHRESHOLD = 30;//这个值调节灵敏度
    private long lastUpdateTime;
    private float lastX;
    private float lastY;
    private float lastZ;

    private AnimationDrawable animationDrawable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        image = (ImageView) findViewById(R.id.image);

        animationDrawable = (AnimationDrawable) image.getDrawable();
        animationDrawable.stop();

        TextView tv_name = (TextView) findViewById(R.id.tv_name);
        tv_name.setText(getString(R.string.lucky_draw_people)+MFSPHelper.getString(CommConstants.EMPCNAME));

        //获取传感器管理服务  mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //震动  mVibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);

        mSoundPool= new SoundPool(1, AudioManager.STREAM_SYSTEM, 0);
        mSoundId = mSoundPool.load(this,R.raw.shake, 1); //第二个参数是音乐资源文件  }

    }

    @Override
    protected int getContentView() {
        return R.layout.activity_lucky_draw;
    }

    @Override
    protected void setTopBarValue() {
        tv_topbar_center.setText(getResources().getString(R.string.lucky_draw));
    }

    @Override
    protected void setContentValue() {


    }

    @Override
    protected void onItemClickListener(int position) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != sensorManager) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }
        if (null != sensor) {
            sensorManager.registerListener(sensorEventListener,
                    sensor,
                    SensorManager.SENSOR_DELAY_GAME);//这里选择感应频率
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != sensorManager && null != sensor) {
            sensorManager.unregisterListener(sensorEventListener, sensor);
        }
        /*离开界面释放音频资源*/
        if(null != mSoundPool){
            mSoundPool.unload(mSoundId);
        }

    }

    private void sendRequest() {
        //发起摇奖请求
        EventsManager.postLuckyDraw(MFSPHelper.getString(CommConstants.USERID), new StringCallback() {

            @Override
            public void onError(Call call, Exception e) {

            }

            @Override
            public void onResponse(String response) {

                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getBoolean("ok") && jsonObject.getBoolean("objValue")) {
                        ToastUtils.showToastBottom(LuckyDrawActivity.this, "摇奖成功，感谢参与！");
                    } else {
                        ToastUtils.showToastBottom(LuckyDrawActivity.this, "屏幕开始滚动，摇奖才有效哦！");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 重力感应监听
     */
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            long currentUpdateTime = System.currentTimeMillis();
            long timeInterval = currentUpdateTime - lastUpdateTime;
            if (timeInterval < UPTATE_INTERVAL_TIME) {
                return;
            }
            lastUpdateTime = currentUpdateTime;
            // 传感器信息改变时执行该方法
            float[] values = event.values;
            float x = values[0]; // x轴方向的重力加速度，向右为正
            float y = values[1]; // y轴方向的重力加速度，向前为正
            float z = values[2]; // z轴方向的重力加速度，向上为正
            float deltaX = x - lastX;
            float deltaY = y - lastY;
            float deltaZ = z - lastZ;

            lastX = x;
            lastY = y;
            lastZ = z;
            double speed = (Math.sqrt(deltaX * deltaX + deltaY * deltaY
                    + deltaZ * deltaZ) / timeInterval) * 100;
            if (speed >= SPEED_SHRESHOLD) {

                /*播放摇一摇音效*/
                mSoundPool.play(mSoundId, 1, 1, 1, 0, 1);

                vibrator.vibrate(300);
                animationDrawable.start();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationDrawable.stop();
                        //发起摇奖请求
                        sendRequest();

                    }
                }, 1000);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };
}
