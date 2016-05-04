package com.chuanshuo.cooper.daohang;

import java.util.List;

import cn.bmob.Bmob;
import cn.bmob.BmobException;
import cn.bmob.BmobObject;
import cn.bmob.BmobQuery;
import cn.bmob.BmobUser;
import cn.bmob.FindCallback;
import cn.bmob.LogInCallback;
import cn.bmob.SignUpCallback;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class LoginActivity extends Activity implements OnClickListener{

	TextView txtName = null;
	TextView txtPassword = null;
	
	Button btnLogin = null;
	Button btnZhuce = null;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Bmob.initialize(this,"ef3c3cfd50837afca6106d7db925f878");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		txtName = (TextView)findViewById(R.id.txt_name);
		txtPassword = (TextView)findViewById(R.id.txt_password);
		
		btnLogin = (Button)findViewById(R.id.btn_login);
		btnZhuce = (Button)findViewById(R.id.btn_zhuce);
		btnLogin.setOnClickListener(this);
		btnZhuce.setOnClickListener(this);
		
	} 
	
	@Override
	public void onClick(View v) {
		
		String userName = txtName.getText().toString();
		String userPassword = txtPassword.getText().toString();
		
		if(userName.trim().length()==0){
//            Toast.makeText(this, "请输入注册登录名!", Toast.LENGTH_SHORT).show();
            Toast.makeText(this, "Please enter the registered login name!", Toast.LENGTH_SHORT).show();
            return;
		}
		if(userPassword.trim().length()==0){
//			Toast.makeText(this, "请输入注册密码!", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Please enter the registration password!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		switch (v.getId()) {
		case R.id.btn_login:
			
			BmobUser.logInInBackground(userName, userPassword, new LogInCallback() {
				
				@Override
				public void done(final BmobUser user, BmobException exce) {
					if(exce!=null){
						if(exce.getCode() == 3006){
//				            Toast.makeText(LoginActivity.this, "用户名或密码错误!", Toast.LENGTH_SHORT).show();
				            Toast.makeText(LoginActivity.this, "User name or password error!", Toast.LENGTH_SHORT).show();
				            return;
						}else{
//				            Toast.makeText(LoginActivity.this, "登陆失败!", Toast.LENGTH_SHORT).show();
				            Toast.makeText(LoginActivity.this, "Failed login!", Toast.LENGTH_SHORT).show();
				            return;
						}
					}else if(user==null){
//			            Toast.makeText(LoginActivity.this, "登陆失败!", Toast.LENGTH_SHORT).show();
			            Toast.makeText(LoginActivity.this, "Failed login!", Toast.LENGTH_SHORT).show();
			            return;
					}else{
						//管理员用户
						if(user.getNumber("userType").intValue()==1){
							Intent intent = new Intent(LoginActivity.this,AdminMapActivity.class);
							startActivity(intent);
							LoginActivity.this.finish();
							return;
						}
			            BmobQuery query = new BmobQuery("duiwu");
			            query.findInBackground(new FindCallback() {
							@Override
							public void done(List<BmobObject> arg0, BmobException arg1) {
								if(arg1!=null){
//						            Toast.makeText(LoginActivity.this, "网络错误!", Toast.LENGTH_SHORT).show();
						            Toast.makeText(LoginActivity.this, "Network error!", Toast.LENGTH_SHORT).show();
						            return;
								}
								if(arg0.size()==0){
//						            Toast.makeText(LoginActivity.this, "当前没有线路，请联系领队创建线路!", Toast.LENGTH_SHORT).show();
						            Toast.makeText(LoginActivity.this, "The lines have been locked, unable to join!", Toast.LENGTH_SHORT).show();
						            return;
								}
								if(arg0.get(0).getNumber("state").intValue()==1&&user.getInt("state")==0){
//						            Toast.makeText(LoginActivity.this, "线路已经锁定，无法加入!", Toast.LENGTH_SHORT).show();
						            Toast.makeText(LoginActivity.this, "线路已经锁定，无法加入!", Toast.LENGTH_SHORT).show();
						            return;
								} 
								Intent intent = new Intent(LoginActivity.this,LuxianMapActivity.class);
								startActivity(intent);
								LoginActivity.this.finish();
								return; 
							}
						});
					}
				}
			});
//            Toast.makeText(LoginActivity.this, "正在登陆，请稍后...!", Toast.LENGTH_SHORT).show();
            Toast.makeText(LoginActivity.this, "login later...!", Toast.LENGTH_SHORT).show();
			
			break;
			
		case R.id.btn_zhuce:
			BmobUser user = new BmobUser();
			user.setUsername(userName);
			user.setPassword(userPassword);
			user.put("userType", 0);
			user.put("state", 0);
			
			user.signUpInBackground(new SignUpCallback() {
				
				@Override
				public void done(BmobException arg0) {
					if(arg0 == null){
//						Toast.makeText(LoginActivity.this, "注册成功，请登陆", Toast.LENGTH_SHORT).show();
						Toast.makeText(LoginActivity.this, "Successful registration, please login", Toast.LENGTH_SHORT).show();
					}else if(arg0.getCode() == 2002){
//						Toast.makeText(LoginActivity.this, "注册失败，用户名已存在", Toast.LENGTH_SHORT).show();
						Toast.makeText(LoginActivity.this, "Registration fails, the user name already exists", Toast.LENGTH_SHORT).show();
					}else{
//						Toast.makeText(LoginActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
						Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
					}
				}
			});
//			Toast.makeText(this, "正在注册，请稍后...", Toast.LENGTH_SHORT).show();
			Toast.makeText(this, "Being registered, please wait...", Toast.LENGTH_SHORT).show();
			
			break;

		default:
			break;
		}
	}
}
