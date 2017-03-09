package com.movit.platform.cloud.net;


import com.movit.platform.framework.core.okhttp.callback.Callback;
import com.movit.platform.framework.core.okhttp.callback.CloudListCallback;
import com.movit.platform.framework.core.okhttp.callback.FileCallBack;
import com.movit.platform.framework.core.okhttp.callback.StringCallback;

import java.io.File;

;

/**
 * author: Qian Ping
 * En_Name: Zoro Qian
 * E-mail: qianping1220@gmail.com
 * version: 4.0.2
 * Created Time: 2016年3月17日 下午4:56:01
 * Description: interface property.
 **/
public interface INetHandler {

    /** 登录**/
    void okmLogin(StringCallback responseCallback);

    /** 获取文件根目录**/
    void getRootFolder(StringCallback responseCallback);

    /** 获取部门目录**/
    void getDeptFolder(StringCallback responseCallback);

    /** 获取文件根目录**/
    void getPersonalFolder(StringCallback responseCallback);

    /** 获取共享文件根目录**/
    void getShareFolder(CloudListCallback responseCallback);

    /** 分享文档**/
    void postShareDocument(String uuid, String shareUser, StringCallback responseCallback);

    /** 获取目录**/
    void getFolderChildren(String fldId, Callback responseCallback);

    /** 获取文件**/
    void getDocumentChildren(String fldId, Callback responseCallback);

    /** 下载文档**/
    void downDocument(String docId, FileCallBack fileCallBack);

    /** 下载文档**/
    long downDocument(String docId, String filePath, String fileName);

    /** 下载文档**/
    long downKnowDocument(String fileName, String filePath,String downFilePath);

    /** 分享给我的搜索**/
    void getShareByName(String name,CloudListCallback responseCallback);

    /** 我的文档搜索**/
    void getFindAll(String name,String path,StringCallback responseCallback);

    /** 新建文件夹**/
    void createFolder(String path,StringCallback responseCallback);

    /** 上传文件**/
    void uploadFile(String path,String fileName,File file, FileCallBack responseCallback);

    /** 下载文档日志**/
    void saveDownloadFile(String fileId,String fileName,String userId);
}
