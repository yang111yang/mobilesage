package com.itheima.mobilesafe74.service;

import java.lang.reflect.Method;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import com.android.internal.telephony.ITelephony;
import com.itheima.mobilesafe74.db.dao.BlackNumberDao;

public class BlackNumberService extends Service {

	private InnerSmsReceiver mInnerSmsReceiver;

	private BlackNumberDao mDao;

	private TelephonyManager mTM;

	private MyPhoneStateListener mPhoneStateListener;

	private MyContentObserver mContentObserver;

	@Override
	public void onCreate() {
		super.onCreate();
		
		mDao = BlackNumberDao.getInstance(getApplicationContext());
		//拦截短信
		IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
		intentFilter.setPriority(1000);
		mInnerSmsReceiver = new InnerSmsReceiver();
		registerReceiver(mInnerSmsReceiver, intentFilter);
		//拦截电话
		//监听电话状态的改变
		// 1.获取电话的管理者对象
		mTM = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		// 2.监听电话状态
		mPhoneStateListener = new MyPhoneStateListener();
		mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

		
	}
	
	class MyPhoneStateListener extends PhoneStateListener {

		// 3.手动重写，电话状态发生改变时触发的方法
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:
				// 空闲状态，没有任何活动
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				// 摘机状态，至少有个电话活动
				break;
			case TelephonyManager.CALL_STATE_RINGING:
				// 响铃状态
				//挂断电话
				endCall(incomingNumber);
				break;
			default:
				break;
			}
		}
	}
	

	public void endCall(String phone){
		int mode = mDao.getMode(phone);
		if (mode == 2 || mode == 3) {
			/*ITelephony.Stub.asInterface(ServiceManager.getService(Context.TELEPHONY_SERVICE));
			ServiceManager此类android对开发者隐藏，所以不能直接调用其方法，所以需要反射调用*/
			try {
				//1.获取ServiceManager的字节码文件
				Class<?> clazz = Class.forName("android.os.ServiceManager");
				//2.获取方法
				Method method = clazz.getMethod("getService", String.class);
				//3.反射调用此方法
				IBinder iBinder = (IBinder) method.invoke(null, Context.TELEPHONY_SERVICE);
				//4.调用获取aidl文件对象的方法
				ITelephony iTelephony = ITelephony.Stub.asInterface(iBinder);
				//5.调用aidl中隐藏的endCall方法
				iTelephony.endCall();
			} catch (Exception e) {
				e.printStackTrace();
			} 
			/*//6.删除此被拦截号码的通话记录(权限)
			getContentResolver().delete(Uri.parse("content://call_log/calls"), "number=?", new String[]{phone});*/
			//6.在内容解析器上，去注册内容观察者，通过内容观察者，观察数据库(Uri决定那张表那个库)的变化,
			mContentObserver = new MyContentObserver(new Handler(),phone);
			getContentResolver().registerContentObserver(Uri.parse("content://call_log/calls"), true, mContentObserver);
		}
	}
	
	class MyContentObserver extends ContentObserver{
		
		private String phone;

		public MyContentObserver(Handler handler,String phone) {
			super(handler);
			this.phone = phone;
		}
		
		//数据库中指定calls表发生改变时调用的方法
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			//插入一条数据后，在进行删除
			getContentResolver().delete(Uri.parse("content://call_log/calls"), "number=?", new String[]{phone});
		}
		
	}
	
	
	class InnerSmsReceiver extends BroadcastReceiver{


		@Override
		public void onReceive(Context context, Intent intent) {
			//1.获取短信的内容
			Object[] objects = (Object[]) intent.getExtras().get("pdus");
			//2.循环遍历短信
			for (Object object : objects) {
				//3.获取短信对象
				SmsMessage sms = SmsMessage.createFromPdu((byte[])object);
				//4.获取短信对象的基本信息
				String originatingAddress = sms.getOriginatingAddress();
				
				
				int mode = mDao.getMode(originatingAddress);
				if (mode == 1 || mode == 3) {
					//拦截短信
					abortBroadcast();
				}
			}
		}
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//注销广播
		if (mInnerSmsReceiver != null) {
			unregisterReceiver(mInnerSmsReceiver);
		}
		//注销内容观察者
		if (mContentObserver != null) {
			getContentResolver().unregisterContentObserver(mContentObserver);
		}
		//取消对电话状态的监听
		if (mPhoneStateListener != null) {
			mTM.listen(mPhoneStateListener, PhoneStateListener.LISTEN_NONE);
		}
	}
	
	
}
