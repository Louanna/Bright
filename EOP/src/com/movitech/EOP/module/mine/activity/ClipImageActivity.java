package com.movitech.EOP.module.mine.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.StringUtils;
import com.movit.platform.framework.utils.ToastUtils;
import com.movit.platform.framework.view.clipview.ClipImageView;
import com.movitech.EOP.Test.R;
import com.movitech.EOP.base.EOPBaseActivity;

import java.io.File;

public class ClipImageActivity extends EOPBaseActivity {
    private ClipImageView imageView;
    private TextView topRight;
    private TextView topTitle;
    private ImageView topLeft;

    String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sc_activity_clipimage);

        path = getIntent().getStringExtra("takePicturePath");
        imageView = (ClipImageView) findViewById(R.id.src_pic);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        topTitle = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);
        // 设置需要裁剪的图片
        File bitmapFile = new File(path);
        if (bitmapFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(path, getBitmapOption(2));
            imageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "图片文件不存", Toast.LENGTH_SHORT).show();
        }

        topTitle.setText("");
        topLeft.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight.setText(getString(R.string.finish));
        topRight.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // 此处获取剪裁后的bitmap
                Bitmap bitmap = imageView.clip();
                String tempPath = PicUtils.compressImageAndSave(path, bitmap, 300);
                if(StringUtils.notEmpty(tempPath)){
                    Intent intent = new Intent();
                    intent.putExtra("takePicturePath", tempPath);
                    setResult(3, intent);
                    finish();
                }else{
                    ToastUtils.showToast(ClipImageActivity.this,"图片访问权限受限！");
                    finish();
                }
            }
        });
    }

    private BitmapFactory.Options getBitmapOption(int inSampleSize){
        System.gc();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPurgeable = true;
        options.inSampleSize = inSampleSize;
        return options;
    }

}
