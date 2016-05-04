package com.chuanshuo.cooper.daohang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;  

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.Bmob;
import cn.bmob.BmobException;
import cn.bmob.BmobObject;
import cn.bmob.BmobQuery;
import cn.bmob.BmobUser;
import cn.bmob.FindCallback;
import cn.bmob.SaveCallback;

public class SearchUserActivity extends Activity {
	TextView txtName = null;
    private Handler handler = new Handler(); 
    private boolean run = false;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bmob.initialize(this,"ef3c3cfd50837afca6106d7db925f878");

		setContentView(R.layout.user_search);

		txtName = (TextView)findViewById(R.id.txt_name);
		Button btnSearch = (Button)findViewById(R.id.btn_search);
		
		btnSearch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final String userName = txtName.getText().toString().trim();
				if(userName.length()==0){
//		            Toast.makeText(SearchUserActivity.this, "请输入用户名!", Toast.LENGTH_SHORT).show();
		            Toast.makeText(SearchUserActivity.this, "Please enter username!", Toast.LENGTH_SHORT).show();
		            return;
				}
				BmobQuery query = BmobQuery.getUserQuery();
				query.whereEqualTo("username", userName);
				query.whereEqualTo("state", 1);
				query.findInBackground(new FindCallback() {
					@Override
					public void done(List<BmobObject> arg0, BmobException arg1) {
						if(arg1!=null){
//				            Toast.makeText(SearchUserActivity.this, "网路错误！", Toast.LENGTH_SHORT).show();
				            Toast.makeText(SearchUserActivity.this, "Network error！", Toast.LENGTH_SHORT).show();
				            return;
						}
						if(arg0.size()==0){
//				            Toast.makeText(SearchUserActivity.this, "用户不存在或未加入线路!", Toast.LENGTH_SHORT).show();
				            Toast.makeText(SearchUserActivity.this, "The user does not exist or join line!", Toast.LENGTH_SHORT).show();
				            return;
						}
						BmobQuery UserSelQuery = new BmobQuery("user_rel");
						UserSelQuery.whereEqualTo("username", BmobUser.getCurrentUser().getUsername());
						UserSelQuery.findInBackground(new FindCallback() {
							@Override
							public void done(List<BmobObject> arg0, BmobException arg1) {
								if(arg1!=null){
//						            Toast.makeText(SearchUserActivity.this, "网路错误！", Toast.LENGTH_SHORT).show();
						            Toast.makeText(SearchUserActivity.this, "Network error！", Toast.LENGTH_SHORT).show();
						            return;
								}
								BmobObject UserSel=null;
								if(arg0.size()>0){
									UserSel = arg0.get(0);
								}else{
									UserSel = new BmobObject("user_rel");
								}
								UserSel.put("username", BmobUser.getCurrentUser().getUsername());
								UserSel.put("yaoqing", userName);
								UserSel.put("yaoqingState", 0);
								UserSel.saveInBackground(new SaveCallback() {
									
									@Override
									public void done(BmobException arg0) {
										if(arg0!=null){
//								            Toast.makeText(SearchUserActivity.this, "网路错误！", Toast.LENGTH_SHORT).show();
								            Toast.makeText(SearchUserActivity.this, "Network error！", Toast.LENGTH_SHORT).show();
								            return;
										}
										run = true;
										new Thread(){
								    		public void run() {
								    			while(run){
													BmobQuery UserSelQuery = new BmobQuery("user_rel");
													UserSelQuery.whereEqualTo("username", BmobUser.getCurrentUser().getUsername());
													UserSelQuery.findInBackground(new FindCallback() {
														@Override
														public void done(final List<BmobObject> arg0, BmobException arg1) {
															if(arg1!=null){
																return;
															}
															if(arg0.size()>0){
								                            	final BmobObject data = arg0.get(0);
								                            	if(data.getNumber("yaoqingState").intValue()==0){
								                            		return;
								                            	}
								                            	run = false;
																handler.post(new Runnable(){
										                            public void run(){
										                            	if(data.getNumber("yaoqingState").intValue()==2){
										                            		new  AlertDialog.Builder(SearchUserActivity.this)    
//										                            		                .setTitle("提示" )  
										                            		                .setTitle("Prompt" )  
//										                            		                .setMessage("‘"+userName+"’拒绝了你的邀请!" )  
										                            		                .setMessage("‘"+userName+"’rejected your invitation!" )  
//										                            		                .setPositiveButton("确定" ,  null )  
										                            		                .setPositiveButton("OK" ,  null )  
										                            		                .show();  
										                    	            return;
										                            	}else if(data.getNumber("yaoqingState").intValue()==1){
										                    				Intent intent = new Intent(SearchUserActivity.this,SearchMapActivity.class);
										                    				startActivity(intent);
										                    				SearchUserActivity.this.finish();
										                            	}
										                        	}
										                     	});
															}
														}
													});
								    				 
								    				try {
														sleep(30000);
													} catch (InterruptedException e) {
														e.printStackTrace();
													}
								    			}
								    		};
								    	}.start();
									}
								});
								
						    	
							}
						}); 
						
					}
				});
//	            Toast.makeText(SearchUserActivity.this, "正在搜索，等待对方接受...", Toast.LENGTH_SHORT).show();
	            Toast.makeText(SearchUserActivity.this, "Are searching for, and waiting for the other to accept...", Toast.LENGTH_SHORT).show();
			}
		});
		
	}

}