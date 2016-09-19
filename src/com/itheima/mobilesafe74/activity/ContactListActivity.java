package com.itheima.mobilesafe74.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima.mobilesafe74.R;

public class ContactListActivity extends Activity {

	protected static final String tag = "ContactListActivity";
	private ListView lv_contact;
	private List<HashMap<String, String>> contactList = new ArrayList<HashMap<String, String>>();
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			//8.填充数据适配器
			lv_contact.setAdapter(new MyAdapter());
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact_list);

		initUI();
		initData();
	}

	/**
	 * 获取系统联系人数据的方法
	 */
	private void initData() {
		// 因为读取联系人，可能是一个耗时操作，所以要使用子线程
		new Thread() {
			public void run() {
				// 1.获取内容解析器对象
				ContentResolver contentResolver = getContentResolver();
				// 2.做查询系统联系人数据库的过程
				Cursor cursor = contentResolver.query(Uri
						.parse("content://com.android.contacts/raw_contacts"),
						new String[] { "contact_id" }, null, null, null);
				
				//清空集合中的数据
				contactList.clear();
				
				// 3.循环游标，直到没有数据位置
				while (cursor.moveToNext()) {
					String id = cursor.getString(0);
					 Log.i(tag, "id="+id);
					// 4.根据用户唯一性id的值，查询data表和mimetypes表生成的视图，获取data以及mimetype字段
					Cursor indexCursor = contentResolver.query(
							Uri.parse("content://com.android.contacts/data"),
							new String[] { "data1", "mimetype" },
							"raw_contact_id=?", new String[] { id }, null);
					//5.循环获取每一个联系人的电话号码以及姓名，数据类型
					HashMap<String, String> hashMap = new HashMap<String, String>();
					while (indexCursor.moveToNext()) {
						String data = indexCursor.getString(0);
						String type = indexCursor.getString(1);
						//6.区分数据去给hashMap填充数据
						if (type.equals("vnd.android.cursor.item/phone_v2")) {
							//数据的非空检查
							if (!TextUtils.isEmpty(data)) {
								hashMap.put("phone", data);
							}
						}else if (type.equals("vnd.android.cursor.item/name")) {
							if (!TextUtils.isEmpty(data)) {
								hashMap.put("name", data);
							}	
						}
					}
					indexCursor.close();	
					//把hashMap存储到集合中
					contactList.add(hashMap);
					
				}
				// 关闭游标
				cursor.close();
				//7.消息机制
				mHandler.sendEmptyMessage(0);
			};
		}.start();

	}

	private void initUI() {
		lv_contact = (ListView) findViewById(R.id.lv_contact);
	}

	
	//ListView的适配器
	class MyAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return contactList.size();
		}

		@Override
		public HashMap<String, String> getItem(int position) {
			return contactList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = View.inflate(getApplicationContext(), R.layout.list_contact_item, null);
			TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
			TextView tv_phone = (TextView) view.findViewById(R.id.tv_phone);
			
			tv_name.setText(contactList.get(position).get("name"));
			tv_phone.setText(contactList.get(position).get("phone"));
			
			return view;
		}
		
	}
	
}