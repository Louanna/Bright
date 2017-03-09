package com.movit.platform.sc.view.clipview;

import android.content.Context;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class ClickedSpanListener extends ClickableSpan {
	String name;
	Context context;

	public ClickedSpanListener(String nameString, Context context) {
		name = nameString;
		this.context = context;
	}

	@Override
	public void onClick(View v) {
		// Toast.makeText(context, name, 1000).show();
	}

	@Override
	public void updateDrawState(TextPaint ds) {
		ds.setUnderlineText(false);
		ds.setColor(0xff6187ab);
	}

	public void onLongClick(View view) {
		// Toast.makeText(context, "long:" + name, 1000).show();
	};
}
