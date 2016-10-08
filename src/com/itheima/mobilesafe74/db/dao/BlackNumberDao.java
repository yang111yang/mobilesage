package com.itheima.mobilesafe74.db.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.itheima.mobilesafe74.db.BlackNumberOpenHelper;
import com.itheima.mobilesafe74.domain.BlackNumberInfo;
/**
 * 数据库的增删改查
 * @author 刘建阳
 * @date 2016-9-28 上午10:39:13
 */
public class BlackNumberDao {
	
	private BlackNumberOpenHelper blackNumberOpenHelper;

	//BlackNumberDao 单例模式
	
	//1.私有化构造方法
	private BlackNumberDao(Context context){
		//创建数据库及其表结构
		blackNumberOpenHelper = new BlackNumberOpenHelper(context);
	}
	
	//2.声明一个当前类的对象
	private static BlackNumberDao blackNumberDao = null;
	
	//3.提供一个方法，如果当前类的对象为空，创建一个新的
	public static BlackNumberDao getInstance(Context context){
		if (blackNumberDao == null) {
			blackNumberDao = new BlackNumberDao(context);
		}
		return blackNumberDao;
	}
	
	/**
	 * 增加一个条目
	 * @param phone 拦截的电话号码
	 * @param mode  拦截的类型(1:短信  2:电话  3:所有)
	 */
	public void insert(String phone,String mode){
		
		//1.开启数据库，准备做写入操作
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put("phone", phone);
		values.put("mode", mode);
		db.insert("blacknumber", null, values);
		
		//关闭数据库
		db.close();
		
	}
	
	/**
	 * 从数据库中删除一条电话号码
	 * @param phone 删除的电话号码
	 */
	public void delete(String phone){
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		db.delete("blacknumber", "phone = ?", new String[]{phone});
		db.close();
		
	}
	
	/**
	 * 根据电话号码，更新拦截模式
	 * @param phone 更新的电话号码
	 * @param mode  更新后的模式
	 */
	public void update(String phone,String mode){
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("mode", mode);
		db.update("blacknumber", values, "phone = ?", new String[]{phone});
		db.close();
	}
	
	/**
	 * @return 查询到数据库中所有的号码及拦截模式所在的集合
	 */
	public List<BlackNumberInfo> findAll() {
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		Cursor cursor = db.query("blacknumber", new String[]{"phone","mode"}, null, null, null, null, "_id desc");
		List<BlackNumberInfo> blackNumberList = new ArrayList<BlackNumberInfo>();
		while (cursor.moveToNext()) {
			BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
			blackNumberInfo.phone = cursor.getString(0);
			blackNumberInfo.mode = cursor.getString(1);
			blackNumberList.add(blackNumberInfo);
		}
		cursor.close();
		db.close();
		return blackNumberList;
	}
	
	/**
	 * 每次查询20条数据
	 * @param index 查询的索引值
	 */
	public List<BlackNumberInfo> find(int index){
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		Cursor cursor = db.rawQuery("select phone,mode from blacknumber order by _id desc limit ?,20;", new String[]{index+""});
		List<BlackNumberInfo> blackNumberList = new ArrayList<BlackNumberInfo>();
		while (cursor.moveToNext()) {
			BlackNumberInfo blackNumberInfo = new BlackNumberInfo();
			blackNumberInfo.phone = cursor.getString(0);
			blackNumberInfo.mode = cursor.getString(1);
			blackNumberList.add(blackNumberInfo);
		}
		cursor.close();
		db.close();
		return blackNumberList;
	}
	
	/**
	 * @return 数据库中数据总条目的个数，返回0代表数据库中没有数据或异常
	 */
	public int getCount(){
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		int count = 0;
		Cursor cursor = db.rawQuery("select count(*) from blacknumber", null);
		while (cursor.moveToNext()) {
			count = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return count;
	}
	
	
	/**
	 * @param phone 作为查询条件的电话号码
	 * @return 传入电话号码的拦截模式	1：短信	2：电话	3：所有	0：没有此条数据
	 */
	public int getMode(String phone){
		SQLiteDatabase db = blackNumberOpenHelper.getWritableDatabase();
		int mode = 0;
		Cursor cursor = db.query("blacknumber", new String[]{"mode"}, "phone = ?", new String[]{phone}, null, null, null);
		while (cursor.moveToNext()) {
			mode = cursor.getInt(0);
		}
		cursor.close();
		db.close();
		return mode;
	}
	
}









