package com.movit.platform.framework.view.viewpager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.movit.platform.common.R;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.core.okhttp.HttpManager;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.helper.MFAQueryHelper;
import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.PicUtils;
import com.movit.platform.framework.utils.ToastUtils;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;

public class ImageViewPagerActivity extends Activity {

    MyViewPagerAdapter pagerAdapter;
    CustomViewPager viewPager;
    TextView index;
    ImageView download,delete;

    List<String> presetList = new ArrayList<>();
    List<String> pathList = new ArrayList<>();
    int currentPage = 0;
    AQuery aQuery;
    int width;
    int height;
    boolean defaultImage = false;
    int resid;
    boolean isFromBucket = false;
    boolean isDelete = false;
    boolean isCanDelete = false;
    ArrayList<String> deleteList = new ArrayList<>();
    public static final int IMAGEVIEWPAGE_DELETE = 33;

    boolean isLongURL = false;
    Context context;
    TextView progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.comm_activity_imageview);
        context = this;
        Display display = getWindowManager().getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;// 得到宽度
        height = displayMetrics.heightPixels;
        aQuery = new AQuery(this);
        init();
        initView();
    }

    private void init() {
        currentPage = getIntent().getIntExtra("postion", 0);
        defaultImage = getIntent().getBooleanExtra("defaultImage", false);
        resid = getIntent().getIntExtra("picid", 0);
        isFromBucket = getIntent().getBooleanExtra("FromBucket", false);
        isCanDelete = getIntent().getBooleanExtra("CanDelete", false);
        isLongURL = getIntent().getBooleanExtra("isLongURL", false);
    }

    private void initView() {
        Intent intent = getIntent();
        pathList = intent.getStringArrayListExtra("selectedImgs");
        presetList = intent.getStringArrayListExtra("presetImgs");
        viewPager = (CustomViewPager) findViewById(R.id.image_pager);
        index = (TextView) findViewById(R.id.image_pager_index);
        progressBar = (TextView) findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        download = (ImageView) findViewById(R.id.image_pager_download);
        delete = (ImageView) findViewById(R.id.image_pager_delete);
        List<View> listViews = new ArrayList<View>();

        for (int i = 0; i < pathList.size(); i++) {
            LayoutInflater inflater = LayoutInflater.from(this);
            View view1 = inflater.inflate(R.layout.comm_activity_imgview_loading, null);
            listViews.add(view1);
        }
        pagerAdapter = new MyViewPagerAdapter(listViews);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(currentPage);
        viewPager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                currentPage = arg0;
                index.setText(arg0 + 1 + "/" + pathList.size());
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        index.setText(currentPage + 1 + "/" + pathList.size());
        if (isFromBucket) {
            download.setVisibility(View.GONE);
        }
        download.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String newPath = "";
                if (defaultImage) {
                    Bitmap bitmap = null;
                    try {
                        bitmap = BitmapFactory.decodeResource(getResources(),
                                resid);
                        newPath = CommConstants.SD_CARD_MYPHOTOS + resid + ".jpg";
                        File f = new File(newPath);
                        FileOutputStream out = new FileOutputStream(f);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (bitmap != null) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                } else {
                    File file = new File(pathList.get(currentPage));
                    if (file.exists()) {
                        newPath = CommConstants.SD_CARD_MYPHOTOS
                                + new FileUtils().getFileName(file
                                .getAbsolutePath());
                        FileUtils.copyFile(file.getAbsolutePath(), newPath);
                    } else {
                        String path = pathList.get(currentPage);
                        File cachedFile = null;
                        if (isLongURL) {
                            cachedFile = aQuery.getCachedFile(path);
                            if (cachedFile != null) {
                                newPath = CommConstants.SD_CARD_MYPHOTOS
                                        + path.substring(path.lastIndexOf("/") + 1);
                                FileUtils.copyFile(
                                        cachedFile.getAbsolutePath(), newPath);
                            } else {
                                ToastUtils.showToast(context,
                                        "图片尚未加载，请稍候再试！");
                            }
                        } else {

                            String destFileDir = CommConstants.SD_CARD_MYPHOTOS;
                            String destFileName = path.substring(path.lastIndexOf("/") + 1);

                            progressBar.setVisibility(View.VISIBLE);
                            HttpManager.downloadFile(CommConstants.URL_DOWN + path, new FileCallBack(destFileDir, destFileName) {
                                @Override
                                public void inProgress(float progress, long total) {
                                }

                                @Override
                                public void onError(Call call, Exception e) {
                                    progressBar.setVisibility(View.GONE);
                                    ToastUtils.showToast(context, "图片保存失败！");
                                }

                                @Override
                                public void onResponse(File response) throws JSONException {
                                    progressBar.setVisibility(View.GONE);
                                    ToastUtils.showToast(context, "图片已保存！");
                                    PicUtils.scanImages(context, response.getAbsolutePath());
                                }
                            });
                        }

                    }
                }
            }
        });

        delete.setVisibility(View.GONE);
        if (isCanDelete) {
            delete.setVisibility(View.VISIBLE);
        }
        delete.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                isDelete = true;
                deleteList.add(pathList.get(currentPage));
                pathList.remove(currentPage);

                List<View> listViews = new ArrayList<View>();
                for (int i = 0; i < pathList.size(); i++) {
                    LayoutInflater inflater = LayoutInflater.from(context);
                    View view1 = inflater.inflate(R.layout.comm_activity_imgview_loading,
                            null);
                    listViews.add(view1);
                }
                if (pathList.isEmpty()) {
                    onBackPressed();
                    return;
                }
                pagerAdapter = null;
                pagerAdapter = new MyViewPagerAdapter(listViews);
                viewPager.setAdapter(pagerAdapter);

                if (currentPage >= pathList.size()) {
                    viewPager.setCurrentItem(pathList.size() - 1);
                    currentPage = pathList.size() - 1;
                } else {
                    viewPager.setCurrentItem(currentPage);
                }
                viewPager.setOnPageChangeListener(new OnPageChangeListener() {

                    @Override
                    public void onPageSelected(int arg0) {
                        currentPage = arg0;
                        index.setText(arg0 + 1 + "/" + pathList.size());
                    }

                    @Override
                    public void onPageScrolled(int arg0, float arg1, int arg2) {
                    }

                    @Override
                    public void onPageScrollStateChanged(int arg0) {
                    }
                });
                index.setText(currentPage + 1 + "/" + pathList.size());
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            overridePendingTransition(0, 0);
        }
    }

    public class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;// 构造方法，参数是我们的页卡，这样比较方便。
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));// 删除页卡
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) { // 这个方法用来实例化页卡
            container.addView(mListViews.get(position), 0);// 添加页卡
            View view1 = mListViews.get(position);
            ImageView imageView = (ImageView) view1
                    .findViewById(R.id.showImage);
            ImageView loading = (ImageView) view1
                    .findViewById(R.id.loading_img);
            final ImageView present = (ImageView) view1
                    .findViewById(R.id.loading_present);

            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBack(v);
                }
            });

            BitmapAjaxCallback callback = new BitmapAjaxCallback() {

                @Override
                protected void callback(String url, ImageView iv, Bitmap bm,
                                        AjaxStatus status) {
                    super.callback(url, iv, bm, status);
                    present.setVisibility(View.GONE);
                }
            };
            callback.rotate(true);

            if (defaultImage) {
                loading.setVisibility(View.GONE);
                aQuery.id(imageView).image(resid);
            } else {
                File file = new File(pathList.get(position));
                if (file.exists()) {// 本地读取大图
                    aQuery.id(imageView).progress(loading)
                            .image(file, true, 500, callback);
                } else {// 去加载大图
                    if (isLongURL) {
                        if (presetList != null) {
                            Bitmap preset = aQuery.getCachedImage(presetList
                                    .get(position));
                            if (present != null) {
                                callback.preset(preset);
                            }
                        }
                        aQuery.id(imageView)
                                .progress(loading)
                                .image(pathList.get(position), true, true, 500,
                                        0, callback);
                    } else {
                        if (presetList != null) {
                            Bitmap preset = aQuery
                                    .getCachedImage(CommConstants.URL_DOWN
                                            + presetList.get(position));
                            if (present != null) {
                                callback.preset(preset);
                            }
                        }
                        MFAQueryHelper.setBigImageView(loading, present, imageView, CommConstants.URL_DOWN + pathList.get(position), 500, R.drawable.zone_pic_default);
                    }
                }
            }
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();// 返回页卡的数量
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;// 官方提示这样写
        }
    }

    public void onBack(View arg0) {
        if (isDelete) {
            Intent data = new Intent();
            data.putStringArrayListExtra(
                    "IMAGEVIEWPAGE_DELETE", deleteList);
            setResult(IMAGEVIEWPAGE_DELETE, data);
        }else{
            Intent data = new Intent();
            data.putExtra("BACK_FROM_PAGER",getIntent().getIntExtra("itemPosition",0));
            setResult(RESULT_OK, data);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isDelete) {
            Intent data = new Intent();
            data.putStringArrayListExtra("IMAGEVIEWPAGE_DELETE", deleteList);
            setResult(IMAGEVIEWPAGE_DELETE, data);
        }
        finish();
    }

    @Override
    public void onDestroy() {
        BitmapAjaxCallback.clearCache();
        super.onDestroy();
    }

}
