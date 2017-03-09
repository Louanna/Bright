package com.movit.platform.im.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.im.R;
import com.movit.platform.im.activity.IMBaseActivity;

import org.json.JSONObject;

/**
 * Created by Administrator on 2016/7/18.
 */
public class SelectedImageActivity extends IMBaseActivity {

    private ImageView topLeft, imageView;
    private TextView topRight, topTitle;

    private String picPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_image);

        picPath = getIntent().getStringExtra("takePicturePath");
        imageView = (ImageView) findViewById(R.id.iv);
        topRight = (TextView) findViewById(R.id.common_top_img_right);
        topTitle = (TextView) findViewById(R.id.common_top_title);
        topLeft = (ImageView) findViewById(R.id.common_top_img_left);

        Bitmap bitmap = BitmapFactory.decodeFile(picPath);
        imageView.setImageBitmap(bitmap);

        topTitle.setText("");
        topLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        topRight.setText(getString(R.string.finish));

        topRight.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    String path = PicUtils.getSmallImageFromFileAndRotaing(picPath);
                    JSONObject localPicJson = new JSONObject();
                    localPicJson.put("url", path);
                    localPicJson.put("size", PicUtils.getPicSizeJson(picPath));

                    Intent intent = new Intent();
                    intent.putExtra("takePicturePath", localPicJson.toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }
}
