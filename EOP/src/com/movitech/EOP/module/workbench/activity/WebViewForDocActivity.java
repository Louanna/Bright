package com.movitech.EOP.module.workbench.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.DownloadListener;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.movitech.EOP.Test.R;

public class WebViewForDocActivity extends Activity {
	private WebView mWebView;
	TextView topTitle;
	ImageView topLeft;
	ImageView topRight;
	Context context;

	@SuppressLint({ "SetJavaScriptEnabled", "JavascriptInterface" })
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_view);
		context = this;
		mWebView = (WebView) findViewById(R.id.webview);
		topTitle = (TextView) findViewById(R.id.common_top_title);
		topLeft = (ImageView) findViewById(R.id.common_top_img_left);
		topRight = (ImageView) findViewById(R.id.common_top_img_right);
		topRight.setImageResource(R.drawable.ico_chat);
		topRight.setVisibility(View.GONE);
		topLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				try {
					if (mWebView.canGoBack()) {
						mWebView.goBack();
					} else {
						onBackPressed();
					}
				} catch (Exception e) {
					e.printStackTrace();
					onBackPressed();
				}
			}
		});

		topTitle.setEms(5);
		topTitle.setSingleLine(true);
		topTitle.setEllipsize(TruncateAt.END);
		Intent intent = getIntent();
		String url = intent.getStringExtra("URL");
		String title = intent.getStringExtra("TITLE");
		topTitle.setText(title);
		
		mWebView.stopLoading();
		mWebView.clearHistory();
		mWebView.getSettings().setJavaScriptEnabled(true);
		/*** 打开本地缓存提供JS调用 **/
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);
		String appCachePath = getApplicationContext().getCacheDir()
				.getAbsolutePath();
		mWebView.getSettings().setAppCachePath(appCachePath);
		mWebView.getSettings().setAllowFileAccess(true);// 设置允许访问文件数据
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		// mWebView.getSettings().setRenderPriority(RenderPriority.HIGH);
		mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
		mWebView.addJavascriptInterface(new WebViewFuncion(), "eoopWebView");

		mWebView.clearCache(true);

		Log.v("url", url);

		mWebView.loadUrl(url);
		mWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				System.out.println("shouldOverrideUrlLoading");
				// 在点击请求的是链接是才会调用，重写此方法返回true表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边。
				view.loadUrl(url);
				return true;

			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				super.onReceivedError(view, errorCode, description, failingUrl);
				// WebViewDialog.this.dismiss();
				try {
					view.stopLoading();
					view.clearView();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				// Message msg = handler.obtainMessage();// 发送通知，加入线程
				// msg.what = 1;// 通知加载自定义404页面
				// handler.sendMessage(msg);// 通知发送！
			}

			@Override
			public void onReceivedSslError(WebView view,
					SslErrorHandler handler, SslError error) {
				super.onReceivedSslError(view, handler, error);
				handler.proceed();
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);

			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);

			}

			@Override
			public void onLoadResource(WebView view, String url) {
				// TODO Auto-generated method stub
				super.onLoadResource(view, url);
			}

			@Override
			public WebResourceResponse shouldInterceptRequest(WebView view,
					String url) {
				// TODO Auto-generated method stub
				return super.shouldInterceptRequest(view, url);
			}

		});

		WebChromeClient wvcc = new WebChromeClient() {
			@Override
			public void onReceivedTitle(WebView view, String title) {
				super.onReceivedTitle(view, title);
			}

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				// TODO Auto-generated method stub
				super.onProgressChanged(view, newProgress);
			}
		};
		// 设置setWebChromeClient对象
		mWebView.setWebChromeClient(wvcc);

		mWebView.setDownloadListener(new DownloadListener() {

			@Override
			public void onDownloadStart(String url, String userAgent,
					String contentDisposition, String mimetype,
					long contentLength) {
				Uri uri = Uri.parse(url);
				Intent intent = new Intent(Intent.ACTION_VIEW, uri);
				startActivity(intent);
			}
		});
	}

	class WebViewFuncion {

		public WebViewFuncion() {
			super();
		}

	}
}
