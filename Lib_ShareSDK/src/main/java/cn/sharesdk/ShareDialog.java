package cn.sharesdk;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;

/**
 * Created by air on 16/6/6.
 * ShareDialog
 */
public class ShareDialog implements PlatformActionListener {

    private AlertDialog dialog;
    private GridView gridView;
    private RelativeLayout cancelButton;

    private Context context;

    public ShareDialog(Context context){
        this.context = context;
    }

    public void show3Item(){
        int[] image = {R.drawable.icon_collage,R.drawable.icon_wechat,R.drawable.icon_moments};
        String[] name = new String[image.length];
        name[0] = context.getString(R.string.share_sc);
        name[1] = context.getString(R.string.share_wechat);
        name[2] = context.getString(R.string.share_moments);
        showItem(image,name);
        gridView.setNumColumns(3);
    }

    public void show2Item(){
        int[] image = {R.drawable.icon_wechat,R.drawable.icon_moments};
        String[] name = new String[image.length];
        name[0] = context.getString(R.string.share_wechat);
        name[1] = context.getString(R.string.share_moments);
        showItem(image,name);
        gridView.setNumColumns(2);
    }

    public void showItem(int[] image,String[] name){
        dialog = new android.app.AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.share_dialog);
        gridView = (GridView) window.findViewById(R.id.share_gridView);
        cancelButton = (RelativeLayout) window.findViewById(R.id.share_cancel);
        List<HashMap<String, Object>> shareList=new ArrayList<>();
        for(int i=0;i<image.length;i++){
            HashMap<String, Object> map = new HashMap<>();
            map.put("ItemImage", image[i]);//添加图像资源的ID
            map.put("ItemText", name[i]);//按序号做ItemText
            shareList.add(map);
        }

        SimpleAdapter saImageItems =new SimpleAdapter(context, shareList, R.layout.share_item, new String[] {"ItemImage","ItemText"}, new int[] {R.id.imageView1,R.id.textView1});
        gridView.setAdapter(saImageItems);
    }

    public void setCancelButtonOnClickListener(View.OnClickListener Listener){
        cancelButton.setOnClickListener(Listener);
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener){
        gridView.setOnItemClickListener(listener);
    }


    /**
     * 关闭对话框
     */
    public void dismiss() {
        dialog.dismiss();
    }

    public void shareToWeChat(){
        //2、设置分享内容
        Wechat.ShareParams sp = new Wechat.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);//非常重要：一定要设置分享属性
        sp.setTitle("云拓");  //分享标题
        sp.setText("[链接]云拓-企业移动门户解决方案");   //分享文本
        //sp.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        sp.setImageData(bitmap);
        sp.setUrl("http://eop.movitech.cn/");   //网友点进链接后，可以看到分享的详情
        //3、非常重要：获取平台对象
        Platform wechat = ShareSDK.getPlatform(Wechat.NAME);
        wechat.setPlatformActionListener(this); // 设置分享事件回调
        // 执行分享
        wechat.share(sp);
    }
    public void shareToMoments(){
        //2、设置分享内容
        WechatMoments.ShareParams sp = new WechatMoments.ShareParams();
        sp.setShareType(Platform.SHARE_WEBPAGE);//非常重要：一定要设置分享属性
        sp.setTitle("云拓-企业移动门户解决方案");  //分享标题
        //sp.setText("一个APP连接企业所有工作场景围绕四大场景");   //分享文本
        //sp.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        sp.setImageData(bitmap);
        sp.setUrl("http://eop.movitech.cn/");   //网友点进链接后，可以看到分享的详情
        //3、非常重要：获取平台对象
        Platform wechat = ShareSDK.getPlatform(WechatMoments.NAME);
        wechat.setPlatformActionListener(this); // 设置分享事件回调
        // 执行分享
        wechat.share(sp);
    }
    public void shareToQQ(){
        //2、设置分享内容
        QQ.ShareParams sp = new QQ.ShareParams();
        sp.setTitle("云拓");  //分享标题
        sp.setText("[链接]云拓-企业移动门户解决方案");   //分享文本
        //sp.setImageUrl("http://7sby7r.com1.z0.glb.clouddn.com/CYSJ_02.jpg");//网络图片rul
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon);
        sp.setImageData(bitmap);
        sp.setTitleUrl("http://eop.movitech.cn/");  //网友点进链接后，可以看到分享的详情
        //3、非常重要：获取平台对象
        Platform qq = ShareSDK.getPlatform(QQ.NAME);
        qq.setPlatformActionListener(this); // 设置分享事件回调
        // 执行分享
        qq.share(sp);
    }

    @Override
    public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        handler.sendEmptyMessage(11);
    }

    @Override
    public void onError(Platform platform, int i, Throwable throwable) {
        throwable.printStackTrace();
        Message msg = new Message();
        msg.what = 12;
        msg.obj = throwable.getMessage();
        handler.sendMessage(msg);
    }

    @Override
    public void onCancel(Platform platform, int i) {
        handler.sendEmptyMessage(13);
    }

    android.os.Handler handler = new android.os.Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 11:
                    Toast.makeText(context, "分享成功", Toast.LENGTH_LONG).show();
                    break;
                case 12:
                    if(null == msg.obj){
                        Toast.makeText(context, "分享失败: 未安装微信", Toast.LENGTH_LONG).show();
                    }else {
                        Toast.makeText(context, "分享失败"+ msg.obj, Toast.LENGTH_LONG).show();
                    }
                    break;
                case 13:
                    Toast.makeText(context, "分享取消", Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

    };
}
