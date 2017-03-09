package com.movit.platform.im.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.movit.platform.framework.utils.FileUtils;
import com.movit.platform.framework.utils.StringUtils;

public class DownloadFiles {
    public static final int TIMEOUT = 6;

    public static void downFile(String fileLink, String SDpath, String username, String password, FileUtils.UpdataBarListerner ubl) {
        String fileName = FileUtils.getInstance().geFileName(fileLink);

        fileName = URLDecoder.decode(URLDecoder.decode(fileName));
        if(StringUtils.empty(fileName)){
        	return;
        }
        // 在这个地方判断文件是否已经存在? 文件是否已经在服务器端更新过了? File.lastModified() 好象不行，一会再看看。
        // 首先先简单处理一下：直接先删除。
        if (FileUtils.getInstance().isFileExist(SDpath + fileName)) {
            File file = new File(SDpath + fileName);
            file.delete();
        }
        InputStream inputStream = null;
        try {
            inputStream = getInputStreamFromUrl(fileLink, username, password);
            FileUtils.getInstance().write2SDFromInput(SDpath, fileName, inputStream, ubl);
        } catch (Exception e) {
            if (FileUtils.getInstance().isFileExist(SDpath + fileName)) {
                File file = new File(SDpath + fileName);
                file.delete();
            }
//            ubl.onError(HomeFragment.ERROR_DOWNLOAD_ATTACHMENT, 0);
            e.printStackTrace();
        } finally {
            try {
                if (null != inputStream) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // }

    /**
     * 根据URL得到输入流
     * 
     * @param urlStr
     * @return
     * @throws MalformedURLException
     * @throws IOException
     */
    public static InputStream getInputStreamFromUrl1(String urlStr) throws MalformedURLException, IOException {

        // InputStream inputStream = HttpsUtil.getInputStreamFromUrl(urlStr);
        // return inputStream;

        URL url = new URL(urlStr);
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setConnectTimeout(TIMEOUT * 1000);
        urlConn.setReadTimeout(TIMEOUT * 1000);
        InputStream inputStream = urlConn.getInputStream();
        return inputStream;
    }

    /*
     * webserviceUrl, url of the web service. webserviceIP, IP of the server.
     * username, Domain username password, Domain password
     */
    public static InputStream getInputStreamFromUrl(String webserviceUrl, String username, String password) {
        InputStream inputStream = null;
        try {
            String deviceIP = null; // FileUtils.getInstance().getLocalIpAddress();
                                    // Device IP
            String domainName = FileUtils.DOMAIN_NAME; // Domain name

            DefaultHttpClient httpclient = new DefaultHttpClient();
            httpclient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
            AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
            AuthScope authScope1 = new AuthScope(null, -1);
            httpclient.getCredentialsProvider().setCredentials(authScope,
                                                               new NTCredentials(username, password, deviceIP, domainName));

            HttpGet httpGet = new HttpGet(webserviceUrl);
            httpGet.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);

            HttpResponse response = httpclient.execute(httpGet);
            // String responseXML = EntityUtils.toString(response.getEntity());

            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
                HttpEntity entity = response.getEntity();
                inputStream = entity.getContent();
                return inputStream;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    public static boolean isNetAvailable(Context activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (null != networkInfo) {
            return networkInfo.isAvailable();
        }
        return false;
    }

}
