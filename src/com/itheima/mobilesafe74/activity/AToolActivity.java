package com.itheima.mobilesafe74.activity;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.itheima.mobilesafe74.R;
import com.itheima.mobilesafe74.engine.SmsBackUp;
import com.itheima.mobilesafe74.engine.SmsBackUp.CallBack;

public class AToolActivity extends Activity {
	private TextView tv_query_phone_address, tv_sms_backup,tv_common_number_query,tv_app_lock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_atool);

		// 电话归属地查询的方法
		initPhoneAddress();
		// 短信备份的方法
		initSmsBackUp();
		//常用号码查询的方法
		initCommonNumberQuery();
		//程序锁
		initAppLock();

	}

	/**
	 * 程序锁
	 */
	private void initAppLock() {
		tv_app_lock = (TextView) findViewById(R.id.tv_app_lock);
		tv_app_lock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						AppLockActivity.class));
			}
		});
	}

	/**
	 * 常用号码查询的方法
	 */
	private void initCommonNumberQuery() {
		tv_common_number_query = (TextView) findViewById(R.id.tv_common_number_query);
		tv_common_number_query.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						CommonNumberQueryActivity.class));
			}
		});
	}

	/**
	 * 短信备份的方法
	 */
	private void initSmsBackUp() {
		tv_sms_backup = (TextView) findViewById(R.id.tv_sms_backup);
		tv_sms_backup.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showSmsBackUpDialog();
			}
		});
	}

	/**
	 * 弹出进度条对话框
	 */
	protected void showSmsBackUpDialog() {
		// 1.创建进度条对话框
		final ProgressDialog progressDialog = new ProgressDialog(this);
		progressDialog.setIcon(R.drawable.ic_launcher);
		progressDialog.setTitle("短信备份");
		// 2.指定进度条的样式为水平
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		// 3.展示进度条对话框
		progressDialog.show();
		// 4.直接调用备份短信的方法
		new Thread() {
			public void run() {
				String path = Environment.getExternalStorageDirectory().getAbsolutePath()+File.separator+"sms74.xml";
				SmsBackUp.backup(getApplicationContext(), path, new CallBack() {
					@Override
					public void setProgress(int index) {
						progressDialog.setProgress(index);
					}
					
					@Override
					public void setMax(int max) {
						progressDialog.setMax(max);
					}
				});

				progressDialog.dismiss();
			};
		}.start();

	}

	/**
	 * 电话归属地查询的方法
	 */
	private void initPhoneAddress() {
		tv_query_phone_address = (TextView) findViewById(R.id.tv_query_phone_address);
		tv_query_phone_address.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(getApplicationContext(),
						QueryAddressActivity.class));
			}
		});
	}
}
