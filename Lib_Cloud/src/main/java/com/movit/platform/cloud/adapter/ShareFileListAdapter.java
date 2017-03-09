package com.movit.platform.cloud.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.movit.platform.cloud.R;
import com.movit.platform.cloud.application.CloudApplication;
import com.movit.platform.cloud.model.Share;
import com.movit.platform.cloud.utils.FileType;
import com.movit.platform.cloud.view.DonutProgress;
import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.framework.utils.DateUtils;
import com.movit.platform.framework.utils.FileUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by air on 16/2/26.
 *
 * 文件列表适配器
 */
public class ShareFileListAdapter extends BaseAdapter {

    private Context context;
    private ArrayList<Share> data;

    private OpenFileListener openFileListener = null;
    private DownFileListener downFileListener = null;

    public ShareFileListAdapter(Context context, ArrayList<Share> data){
        this.context = context;
        this.data = data == null ? new ArrayList<Share>() : data;
    }


    @Override
    public int getCount() {
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
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.ivAvatar = (ImageView)convertView.findViewById(R.id.iv_avatar);
        holder.tvFileName = (TextView)convertView.findViewById(R.id.tv_file_name);
        holder.tvFileTime = (TextView)convertView.findViewById(R.id.tv_file_time);
        holder.tvFileSize = (TextView)convertView.findViewById(R.id.tv_file_size);
        holder.tvFileOpen = (TextView)convertView.findViewById(R.id.tv_file_open);
        holder.progressBar = (DonutProgress)convertView.findViewById(R.id.cloud_item_numberCircleProgressBar);

        final Share document = (Share)getItem(position);

        final String listFileName =  document.getNbsName();

        final String fileSuffix = FileType.getFileTYpe(holder.ivAvatar,listFileName);

//        holder.tvFileName.setText(fileName.replace(fileSuffix,""));
        holder.tvFileName.setText(listFileName);

        Date createDate = DateUtils.str2Date(document.getCreateDate());
        String createTime = DateUtils.date2Str(createDate,"yyyy-MM-dd HH:mm");
        holder.tvFileTime.setText(createTime);

        final String uuid = document.getShareUuid();

        final String path = CommConstants.SD_DOCUMENT+"okmshare/"+uuid+fileSuffix;
        File dir = new File(path);
        if (dir.exists()){
                holder.tvFileOpen.setText(context.getString(R.string.sky_drive_open));
                holder.tvFileOpen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(null != openFileListener){
                            openFileListener.openFile(path, FileUtils.getInstance().getMIMEType(path));
                        }
                    }
                });
        }else {
            holder.tvFileOpen.setText(context.getString(R.string.sky_drive_download));
            holder.tvFileOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                if(CloudApplication.downloadIdMap.size() >= 2){
                    HashMap<String,String> map = new HashMap<>();
                    map.put("docId",document.getShareUuid());
                    map.put("fileName",uuid+fileSuffix);
                    map.put("filePath","okmshare");
                    map.put("listFileName",listFileName);
                    CloudApplication.waitList.add(map);
                    document.setDownload(3);
                    notifyDataSetChanged();
                }else {
                    if(null != downFileListener){
                        downFileListener.downLoad(listFileName,uuid,uuid+fileSuffix,
                                "okmshare",document.getSize());
                    }
                }
                }
            });
        }



        double size_b = document.getSize();

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
            holder.progressBar.setVisibility(View.VISIBLE);
            holder.progressBar.setProgress(document.getProgress());
        }else if(document.isDownload() == 3){//wait
            holder.tvFileOpen.setText(context.getString(R.string.sky_drive_wait));
            holder.tvFileOpen.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
        }else {
            holder.tvFileOpen.setVisibility(View.VISIBLE);
            holder.progressBar.setVisibility(View.GONE);
            if (dir.exists()){//open
                holder.tvFileOpen.setText(context.getString(R.string.sky_drive_open));
            }else {//download
                holder.tvFileOpen.setText(context.getString(R.string.sky_drive_download));
            }
        }

        return convertView;
    }


    public void setOpenFileListener(OpenFileListener openFileListener){
        this.openFileListener = openFileListener;
    }

    public void setDownFileListener(DownFileListener downFileListener){
        this.downFileListener = downFileListener;
    }

    public interface OpenFileListener{
        void openFile(String path, String type);
    }

    public interface DownFileListener{
        void downLoad(String listFileName,String docId, String fileName, String filePath, double totalSize);
    }

    public final class ViewHolder {

        public ImageView ivAvatar;
        public TextView tvFileName;
        public TextView tvFileTime;
        public TextView tvFileSize;
        public TextView tvFileOpen;
        public DonutProgress progressBar;
    }

    public void clear(){
        this.data.clear();
        notifyDataSetChanged();
    }

    public void add(List<Share> documents){
        this.data.addAll(documents);
        notifyDataSetChanged();
    }

    public void replaceAll(ArrayList<Share> documents){
        this.data.clear();
        this.data.addAll(documents);
        notifyDataSetChanged();
    }

    public ArrayList<Share> getAll(){
        return data;
    }
}
