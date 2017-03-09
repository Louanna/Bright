package com.movit.platform.cloud.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.cloud.R;
import com.movit.platform.cloud.activity.WebViewActivity;
import com.movit.platform.cloud.application.CloudApplication;
import com.movit.platform.cloud.model.Document;
import com.movit.platform.cloud.model.DocumentVersion;
import com.movit.platform.cloud.utils.FileType;
import com.movit.platform.cloud.view.DonutProgress;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.DateUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by air on 16/2/26.
 *
 * 文件列表适配器
 */
public class FileListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Document> data;

    private OpenFileListener openFileListener = null;
    private DownFileListener downFileListener = null;
    private ShareFileListener shareFileListener = null;

    private boolean isPersonal;
    private String isChat;

    //private boolean isNull = false;

    public FileListAdapter(Context context,ArrayList<Document> data,boolean isPersonal){
        this.context = context;
        this.data = data == null ? new ArrayList<Document>() : data;
        this.isPersonal = isPersonal;
    }

    public void setChat(String chat){
        this.isChat = chat;
    }

    @Override
    public int getCount() {
//        return 12;
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (null == convertView) {
            convertView = View.inflate(context, R.layout.item_file_list, null);
            holder = new ViewHolder();
            holder.ivAvatar = (ImageView)convertView.findViewById(R.id.iv_avatar);
            holder.tvFileName = (TextView)convertView.findViewById(R.id.tv_file_name);
            holder.tvFileTime = (TextView)convertView.findViewById(R.id.tv_file_time);
            holder.tvFileSize = (TextView)convertView.findViewById(R.id.tv_file_size);
            holder.tvFileOpen = (TextView)convertView.findViewById(R.id.tv_file_open);
            holder.tvFilePreview = (TextView)convertView.findViewById(R.id.tv_file_preview);
            holder.progressBar = (DonutProgress)convertView.findViewById(R.id.cloud_item_numberCircleProgressBar);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final Document document = (Document)getItem(position);
        holder.tvFileOpen.setTag(document.getUuid());

        final String listFileName =  document.getPath().substring(document.getPath().lastIndexOf("/")+1);
        holder.tvFileName.setText(listFileName);

        Date createDate = DateUtils.str2Date(document.getCreateDate());
        String createTime = DateUtils.date2Str(createDate,"yyyy-MM-dd HH:mm");
        holder.tvFileTime.setText(createTime);

        final DocumentVersion actualVersion = document.getActualVersion();
        if(null != actualVersion){

            final String fileSuffix = FileType.getFileTYpe(holder.ivAvatar,listFileName);

//            final String path = CommConstants.SD_DOCUMENT+document.getPath().substring(1).replace(":","");
            final String path = CommConstants.SD_DOCUMENT+document.getUuid()+fileSuffix;
            File dir = new File(path);
            if (dir.exists()){
                holder.tvFileOpen.setText(context.getString(R.string.sky_drive_open));
                holder.tvFileOpen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        if(isNull){
//                            ToastUtils.showToast(context,"暂不支持该格式预览");
//                        }else {
                        if(null != openFileListener){
                            openFileListener.openFile(path,document.getMimeType());
                        }
//                        }
                    }
                });
            }else {
                holder.tvFileOpen.setText(context.getString(R.string.sky_drive_download));
                holder.tvFileOpen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        String rootPath = document.getPath().substring(1).replace(":","");
                        String fileName = document.getUuid()+fileSuffix;
                        if(CloudApplication.downloadIdMap.size() >= 2){
                            HashMap<String,String> map = new HashMap<>();
                            map.put("docId",document.getUuid());
                            map.put("fileName",fileName);
                            map.put("filePath","");
                            map.put("listFileName",listFileName);
                            CloudApplication.waitList.add(map);
                            document.setDownload(3);
                            notifyDataSetChanged();
                        }else {
                            if(null != downFileListener){
                                downFileListener.downLoad(listFileName,document.getUuid(),fileName,
                                        "",actualVersion.getSize());
                            }
                        }

                    }
                });
            }


            holder.tvFileOpen.setVisibility(View.VISIBLE);
//            if(isPersonal){
//                holder.tvFilePreview.setVisibility(View.GONE);
//            }else {
//                holder.tvFilePreview.setVisibility(View.VISIBLE);
//            }

            //holder.tvFileName.setText(fileName.replace(fileSuffix,""));

            double size_b = actualVersion.getSize();

            String size;
            if(size_b > 1024){
                double size_kb = size_b/1024;
                if(size_kb > 1024){
                    double size_m = size_kb/1024;
                    DecimalFormat df = new DecimalFormat("#.#");
                    double get_double = Double.parseDouble(df.format(size_m));
                    size = get_double + "M";
                }else {
                    size = (int)Math.rint(size_kb) + "KB";
                }
            }else {
                size = (int)Math.rint(size_b) + "B";
            }

            holder.tvFileSize.setText(size);

            if(document.isDownload() == 2) {//downloading
                holder.tvFileOpen.setVisibility(View.GONE);
                //holder.tvFilePreview.setVisibility(View.GONE);
                holder.progressBar.setVisibility(View.VISIBLE);
                holder.progressBar.setProgress(document.getProgress());
            }else if(document.isDownload() == 3){//wait
                holder.tvFileOpen.setText(context.getString(R.string.sky_drive_wait));
                holder.tvFileOpen.setVisibility(View.VISIBLE);
                //holder.tvFilePreview.setVisibility(View.VISIBLE);
                holder.progressBar.setVisibility(View.GONE);
            }else {
                holder.tvFileOpen.setVisibility(View.VISIBLE);
//                if(isPersonal){
//                    holder.tvFilePreview.setVisibility(View.GONE);
//                }else {
//                    holder.tvFilePreview.setVisibility(View.VISIBLE);
//                }
                holder.progressBar.setVisibility(View.GONE);
                if (dir.exists()){//open
                    holder.tvFileOpen.setText(context.getString(R.string.sky_drive_open));
                }else {//download
                    holder.tvFileOpen.setText(context.getString(R.string.sky_drive_download));
                }
            }


//                if(CloudApplication.buttonMap.containsKey(document.getUuid())){
//                    ProgressButton button = CloudApplication.buttonMap.get(document.getUuid());
//                    if(button.getButton().getTag().equals(holder.tvFileOpen.getTag())){
//                        button.setButton(holder.tvFileOpen);
//                        button.setFilePath(path);
//                        button.setTotalSize(size_b);
//                        CloudApplication.buttonMap.put(document.getUuid(),button);
//                    }else {
//                       holder.tvFileOpen.setProgress(0);
//                    }
//                }else {
//                    //holder.tvFileOpen.setProgress(0);
//                    ProgressButton button = new ProgressButton();
//                    button.setButton(holder.progressBar);
//                    button.setFilePath(path);
//                    button.setTotalSize(size_b);
//                    CloudApplication.buttonMap.put(document.getUuid(),button);
//                    Log.d(FileListAdapter.class.getSimpleName(), "getView ---> uuid :"+document.getUuid());
//                    Log.d(FileListAdapter.class.getSimpleName(), "getView ---> button :"+holder.tvFileOpen.getTag());
//                    Log.d(FileListAdapter.class.getSimpleName(), "getView ---> hashCode :"+holder.tvFileOpen.hashCode());
//                }




            holder.tvFileSize.setVisibility(View.VISIBLE);
        }else {
            holder.tvFileSize.setVisibility(View.GONE);
            holder.tvFileOpen.setVisibility(View.GONE);
            //holder.tvFilePreview.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
            holder.ivAvatar.setImageResource(R.drawable.folder);
        }

        /**
         * 发送文件，下载，打开按钮不显示
         */
        if(!TextUtils.isEmpty(isChat)){
            holder.tvFileOpen.setVisibility(View.GONE);
            holder.progressBar.setVisibility(View.GONE);
        }

        //个人区域才允许分享
        holder.ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(null != shareFileListener && null != actualVersion && isPersonal){
                    shareFileListener.shareFile(document.getUuid());
                }
            }
        });

        holder.tvFilePreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String urlString = CommConstants.HTTP_API_URL+"/static/"+document.getUuid()+"/preview.html";
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra("URL", urlString);
                intent.putExtra("title", listFileName);
                context.startActivity(intent);
            }
        });

        return convertView;
    }


    public void setOpenFileListener(OpenFileListener openFileListener){
        this.openFileListener = openFileListener;
    }

    public void setDownFileListener(DownFileListener downFileListener){
        this.downFileListener = downFileListener;
    }

    public void setShareFileListener(ShareFileListener shareFileListener){
        this.shareFileListener = shareFileListener;
    }

    public interface OpenFileListener{
        void openFile(String path, String type);
    }

    public interface DownFileListener{
        void downLoad(String listFileName,String docId, String fileName, String filePath, double totalSize);
    }

    public interface ShareFileListener{
        void shareFile(String uuid);
    }

    public final class ViewHolder {

        public ImageView ivAvatar;
        public TextView tvFileName;
        public TextView tvFileTime;
        public TextView tvFileSize;
        public TextView tvFileOpen;
        public TextView tvFilePreview;
        public DonutProgress progressBar;
    }

    public void clear(){
        this.data.clear();
        notifyDataSetChanged();
    }

    public void add(ArrayList<Document> documents){
        for (Document document : documents){
            String uuid = document.getUuid();
            for (HashMap<String,String> map : CloudApplication.waitList){
                if(map.get("docId").equals(uuid)){
                    document.setDownload(3);//wait
                }
            }
        }
        this.data.addAll(documents);
        notifyDataSetChanged();
    }

    public void replaceAll(ArrayList<Document> documents){
        this.data.clear();
        for (Document document : documents){
            String uuid = document.getUuid();
            for (HashMap<String,String> map : CloudApplication.waitList){
                if(map.get("docId").equals(uuid)){
                    document.setDownload(3);//wait
                }
            }
        }
        this.data.addAll(documents);
        notifyDataSetChanged();
    }

    public ArrayList<Document> getAll(){
        return data;
    }


}
