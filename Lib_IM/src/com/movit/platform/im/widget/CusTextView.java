package com.movit.platform.im.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

import com.movit.platform.common.constants.CommConstants;
import com.movit.platform.im.helper.GifHelper;

public class CusTextView extends TextView {
	TextRunnable r; // 生成Runnable对象

	public static String zhengze = "\\[(\\S+?)\\]"; // 正则表达式，用来判断消息内是否有表情

	/**
	 * @author Dragon SpanInfo
	 *         类用于存储一个要显示的图片（动态或静态）的信息，包括分解后的每一帧mapList、替代文字的起始位置、终止位置
	 *         、帧的总数、当前需要显示的帧、帧与帧之间的时间间隔
	 */
	private class SpanInfo {
		ArrayList<Bitmap> mapList;
		int start, end, frameCount, currentFrameIndex, delay;

		public SpanInfo() {
			mapList = new ArrayList<Bitmap>();
			start = end = frameCount = currentFrameIndex = delay = 0;
		}
	}

	/**
	 * spanInfoList 是一个SpanInfo的list ,用于处理一个TextView中出现多个要匹配的图片的情况
	 */
	private ArrayList<SpanInfo> spanInfoList = null;
	private Handler handler; // 用于处理从子线程TextView传来的消息
	private String myText; // 存储textView应该显示的文本

	/**
	 * 这三个构造方法一个也不要少，否则会产生CastException，注意在这三个构造函数中都为spanInfoList实例化，可能有些浪费
	 * ，但保证不会有空指针异常
	 * 
	 * @param context
	 * @param attrs
	 * @param defStyle
	 */
	public CusTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		spanInfoList = new ArrayList<SpanInfo>();
	}

	public CusTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		spanInfoList = new ArrayList<SpanInfo>();
	}

	public CusTextView(Context context) {
		super(context);
		spanInfoList = new ArrayList<SpanInfo>();
	}

	/**
	 * 对要显示在textView上的文本进行解析，看看是否文本中有需要与Gif或者静态图片匹配的文本 若有，那么调用parseGif
	 * 对该文本对应的Gif图片进行解析 或者嗲用parseBmp解析静态图片
	 * 
	 * @param inputStr
	 */
	private void parseText(String inputStr) {
		myText = inputStr;
		Pattern mPattern = Pattern.compile(zhengze);
		Matcher mMatcher = mPattern.matcher(inputStr);
		while (mMatcher.find()) {
			String key = mMatcher.group();
			Integer gifId = null;
			/**
			 * 这里匹配时用到了图片库，即一个专门存放图片id和其匹配的名称的静态对象，这两个静态对象放在了FaceData.java
			 * 中，并采用了静态块的方法进行了初始化，不会有空指针异常
			 */
			if ((gifId = CommConstants.mFaceGifMap.get(key)) != null) {
				parseGif(gifId, mMatcher.start(), mMatcher.end());
			} else {
				gifId = CommConstants.mFaceMap.get(key);
				parseBmp(gifId, mMatcher.start(), mMatcher.end());
			}

		}

	}

	/**
	 * 对静态图片进行解析：
	 * 创建一个SpanInfo对象，帧数设为1，按照下面的参数设置，最后不要忘记将SpanInfo对象添加进spanInfoList中， 否则不会显示
	 * 
	 * @param resourceId
	 * @param start
	 * @param end
	 */
	private void parseBmp(int resourceId, int start, int end) {
		Bitmap bitmap = BitmapFactory.decodeResource(getContext()
				.getResources(), resourceId);
		ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
		SpanInfo spanInfo = new SpanInfo();
		spanInfo.currentFrameIndex = 0;
		spanInfo.frameCount = 1;
		spanInfo.start = start;
		spanInfo.end = end;
		spanInfo.delay = 100;
		spanInfo.mapList.add(bitmap);
		spanInfoList.add(spanInfo);

	}

	/**
	 * 解析Gif图片，与静态图片唯一的不同是这里需要调用GifOpenHelper类读取Gif返回一系一组bitmap（用for 循环把这一
	 * 组的bitmap存储在SpanInfo.mapList中，此时的frameCount参数也大于1了）
	 * 
	 * @param resourceId
	 * @param start
	 * @param end
	 */
	private void parseGif(int resourceId, int start, int end) {

		GifHelper helper = new GifHelper();
		helper.read(getContext().getResources().openRawResource(resourceId));
		SpanInfo spanInfo = new SpanInfo();
		spanInfo.currentFrameIndex = 0;
		spanInfo.frameCount = helper.getFrameCount();
		int count = helper.getFrameCount();
		if (count == 0) {
			parseBmp(resourceId, start, end);
			return;
		}
		spanInfo.start = start;
		spanInfo.end = end;
		spanInfo.mapList.add(helper.getImage());
		for (int i = 1; i < helper.getFrameCount(); i++) {
			spanInfo.mapList.add(helper.nextBitmap());
		}
		try {
			spanInfo.delay = helper.nextDelay(); // 获得每一帧之间的延迟
			spanInfoList.add(spanInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * MyTextView 与外部对象的接口，以后设置文本内容时使用setSpanText() 而不再是setText();
	 * 
	 * @param handler
	 * @param text
	 */
	public void setSpanText(Handler handler, String text,
			HashMap<String, TextRunnable> map) {
		this.handler = handler; // 获得UI的Handler
		parseText(text); // 对String对象进行解析

		if (r == null) {
			Log.v("run", text);
			r = new TextRunnable();
			handler.post(r); // 利用UI线程的Handler 将r添加进消息队列中。
		}

	}

	public class TextRunnable implements Runnable {

		boolean destory = false;

		public void setDestory(boolean destory) {
			this.destory = destory;
		}

		@Override
		public void run() {
			SpannableString sb = new SpannableString("" + myText); // 获得要显示的文本
			int gifCount = 0;
			SpanInfo info = null;
			for (int i = 0; i < spanInfoList.size(); i++) { // for循环，处理显示多个图片的问题
				info = spanInfoList.get(i);
				if (info.mapList.size() > 1) {
					/*
					 * gifCount用来区分是Gif还是BMP，若是gif gifCount>0 ,否则gifCount=0
					 */
					gifCount++;

				}
				Bitmap bitmap = info.mapList.get(info.currentFrameIndex);
				info.currentFrameIndex = (info.currentFrameIndex + 1)
						% (info.frameCount);
				/**
				 * currentFrameIndex 用于控制当前应该显示的帧的序号，每次显示之后currentFrameIndex
				 * 应该加1 ，加到frameCount后再变成0循环显示
				 */

				if (gifCount != 0) {
					bitmap = Bitmap.createScaledBitmap(bitmap,
							(int) getTextSize() + 5, (int) getTextSize() + 5,
							true);
				} else {
					bitmap = Bitmap.createScaledBitmap(bitmap,
							(int) getTextSize() + 5, (int) getTextSize() + 5,
							true);
				}
				ImageSpan imageSpan = new ImageSpan(getContext(), bitmap);
				sb.setSpan(imageSpan, info.start, info.end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

			}
			// 对所有的图片对应的ImageSpan完成设置后，调用TextView的setText方法设置文本
			CusTextView.this.setText(sb);

			/**
			 * 这一步是为了节省内存而是用，即如果文本中只有静态图片没有动态图片，那么该线程就此终止，不会重复执行 。而如果有动图，那么会一直执行
			 */
			if (gifCount != 0) {
				if (!destory) {
					handler.postDelayed(this, info.delay);
				}
			}
		}
	}

}
