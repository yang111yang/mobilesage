package com.itheima.mobilesafe74.service;

import com.itheima.mobilesafe74.engine.ProcessInfoProvider;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class LockScreenService extends Service {
	
	private IntentFilter intentFilter;
	private InnerReceiver innerReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		innerReceiver = new InnerReceiver();
		registerReceiver(innerReceiver, intentFilter);
	}
	
	class InnerReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			//清理手机正在运行的内存
			ProcessInfoProvider.killAll(context);
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (innerReceiver != null) {
			unregisterReceiver(innerReceiver);
		}
	}

}
