package com.chuanshuo.cooper.daohang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;  

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import cn.bmob.Bmob;
import cn.bmob.BmobException;
import cn.bmob.BmobObject;
import cn.bmob.BmobQuery;
import cn.bmob.FindCallback;

public class UserListActivity extends Activity {
	ListView listView ;
	List<String> dataList;
    ArrayAdapter<String> adapter=null;  
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bmob.initialize(this,"ef3c3cfd50837afca6106d7db925f878");
		
		
		listView = new ListView(this);

		dataList = new ArrayList<String>();

		BmobQuery userQuery = BmobQuery.getUserQuery();
		userQuery.whereEqualTo("userType", 0);
		userQuery.whereEqualTo("state", 1);
		userQuery.findInBackground(new FindCallback() {
			@Override
			public void done(List<BmobObject> arg0, BmobException arg1) {
				if(arg1==null){
					adapter.clear();
					for(BmobObject obj:arg0){ 
						adapter.add(obj.getString("username")); 
					}
					adapter.notifyDataSetChanged();
				}else{
//		            Toast.makeText(UserListActivity.this, "读取用户列表失败!", Toast.LENGTH_SHORT).show();
		            Toast.makeText(UserListActivity.this, "Failed to read the user list!", Toast.LENGTH_SHORT).show();
				}
			}
		}); 
		 
		adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,dataList);
		listView.setAdapter(adapter);
		setContentView(listView);
		
	}

}