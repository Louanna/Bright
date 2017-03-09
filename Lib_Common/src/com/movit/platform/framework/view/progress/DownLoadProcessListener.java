package com.movit.platform.framework.view.progress;

import android.os.Handler;

public interface DownLoadProcessListener {
	void downLoadProcess(Handler handler,int fileSize, int downSize);
}
