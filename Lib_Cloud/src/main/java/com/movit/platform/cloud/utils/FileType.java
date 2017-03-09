package com.movit.platform.cloud.utils;

import android.widget.ImageView;

import com.movit.platform.cloud.R;

/**
 * Created by air on 16/4/28.
 * 获取文件类型
 */
public class FileType {
    public static String getFileTYpe(ImageView imageView,String fileName){
        int index = fileName.lastIndexOf(".");
        if(index < 0){
            imageView.setImageResource(R.drawable.icon_null);
            return "";
        }
        /* 取得扩展名 */
        String fileSuffix = fileName.substring(index,
                fileName.length()).toLowerCase();
        if(fileSuffix.contains("pdf")){
            imageView.setImageResource(R.drawable.pdf);
        }else if(fileSuffix.contains("xls")){
            imageView.setImageResource(R.drawable.excel);
        }else if(fileSuffix.contains("ppt")){
            imageView.setImageResource(R.drawable.ppt);
        }else if(fileSuffix.contains("mp3")){
            imageView.setImageResource(R.drawable.icon_mp3);
        }else if(fileSuffix.contains("mp4")){
            imageView.setImageResource(R.drawable.icon_mp4);
        }else if(fileSuffix.contains("jpg")||fileSuffix.contains("jpeg")){
            imageView.setImageResource(R.drawable.icon_jpg);
        }else if(fileSuffix.contains("png")){
            imageView.setImageResource(R.drawable.icon_png);
        }else if(fileSuffix.contains("gif")){
            imageView.setImageResource(R.drawable.icon_gif);
        }else if(fileSuffix.contains("zip")||fileSuffix.contains("rar")){
            imageView.setImageResource(R.drawable.icon_zip);
        }else if(fileSuffix.contains("doc")){
            imageView.setImageResource(R.drawable.icon_word);
        }else if(fileSuffix.contains("txt")){
            imageView.setImageResource(R.drawable.icon_txt);
        }else {
            imageView.setImageResource(R.drawable.icon_null);
        }

        return fileSuffix;
    }
}
