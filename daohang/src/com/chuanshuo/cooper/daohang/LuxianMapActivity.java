package com.chuanshuo.cooper.daohang;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import cn.bmob.Bmob;
import cn.bmob.BmobException;
import cn.bmob.BmobObject;
import cn.bmob.BmobQuery;
import cn.bmob.BmobUser;
import cn.bmob.FindCallback;
import cn.bmob.SaveCallback;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class LuxianMapActivity extends MapActivity {
	
	private int[][] points = new int[][]{
			null,
			null,
			null
	};
	List<GeoPoint> path;
	String pathJuli;
	LocationManager locMan;
	
	private MapController mapController;
    private GeoPoint geoPoint; //这个表示的是定位中心点在代码中有实现
    private MapView mapView;
    private TextView txtJuli;
    private boolean run = true;  
    private boolean first=true;
    private boolean isShowing=false;
    
    private Button btnJiaru;
    private Button btnLikai;
    private Button btnSearch;
    
    private Handler handler = new Handler(); 
    private Runnable task = new Runnable() {  
    	  
        public void run() {  
            if (run) {  
            	updateUI();
            }  
        }  
    };  
    
    @SuppressWarnings("unchecked")
	@Override
    protected void onCreate(Bundle icicle) {
		Bmob.initialize(this,"ef3c3cfd50837afca6106d7db925f878");
    	super.onCreate(icicle);
    	
    	setContentView(R.layout.luxian_map);
    	mapView = (MapView)findViewById(R.id.mapview);
    	txtJuli = (TextView)findViewById(R.id.txt_juli);
    	btnJiaru = (Button)findViewById(R.id.btn_jiaru);
    	btnLikai = (Button)findViewById(R.id.btn_likai);
    	btnSearch = (Button)findViewById(R.id.btn_search);
    	
    	mapController = mapView.getController();
    	mapView.setEnabled(true);
    	mapView.setClickable(true);
    	mapView.setBuiltInZoomControls(false);
    	updateUI();
    	if(BmobUser.getCurrentUser().getNumber("state").intValue()==0){
    		btnJiaru.setEnabled(true);
    		btnLikai.setEnabled(false);
    	}else{
    		btnJiaru.setEnabled(false);
    		btnLikai.setEnabled(true);
    	}
    	
    	btnJiaru.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BmobUser.getCurrentUser().put("state", 1);
	    		BmobUser.getCurrentUser().saveInBackground(new SaveCallback() {
					@Override
					public void done(BmobException arg0) {
			    		btnJiaru.setEnabled(false);
			    		btnLikai.setEnabled(true);
					}
				});
			}
		});
    	
    	btnLikai.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BmobUser.getCurrentUser().put("state", 0);
	    		BmobUser.getCurrentUser().saveInBackground(new SaveCallback() {
					@Override
					public void done(BmobException arg0) {
			    		btnJiaru.setEnabled(true);
			    		btnLikai.setEnabled(false);
			    		finish();
					}
				});
			}
		});
    	
    	btnSearch.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(LuxianMapActivity.this,SearchUserActivity.class);
				startActivity(intent);
			}
		});
    	
    	new Thread(){
    		public void run() {
    			while(true){
					BmobQuery UserSelQuery = new BmobQuery("user_rel");
					UserSelQuery.whereEqualTo("yaoqing", BmobUser.getCurrentUser().getUsername());
					UserSelQuery.whereEqualTo("yaoqingState",0);
					UserSelQuery.findInBackground(new FindCallback() {
						@Override
						public void done(final List<BmobObject> arg0, BmobException arg1) {
							if(arg1!=null){
								return;
							}
							if(arg0.size()>0){
								if(isShowing){
									return;
								}
								isShowing=true;
								handler.post(new Runnable(){
		                            public void run(){
		                            	 Dialog alertDialog = new AlertDialog.Builder(LuxianMapActivity.this). 
//		                               		setTitle("邀请提醒？"). 
		                                	setTitle("Invite reminder?"). 
//		                                 	setMessage("是否接受'"+arg0.get(0).getString("username")+"'的邀请？"). 
		                                	setMessage("Whether or not to accept the invitation of '"+arg0.get(0).getString("username")+"'？"). 
//		                               		setPositiveButton("接受", new DialogInterface.OnClickListener() { 
		                                 	setPositiveButton("Accepted", new DialogInterface.OnClickListener() { 
		                                             @Override 
		                                             public void onClick(DialogInterface dialog, int which) { 
		                                            	 arg0.get(0).put("yaoqingState", 1);
		                                            	 arg0.get(0).saveInBackground(new SaveCallback() {
															@Override
															public void done(BmobException arg0) {
																if(arg0!=null){
//														            Toast.makeText(LuxianMapActivity.this, "网络错误!", Toast.LENGTH_SHORT).show();  
														            Toast.makeText(LuxianMapActivity.this, "Network error!", Toast.LENGTH_SHORT).show();  
														            return;
																}
				                                            	isShowing=false;
															}
														});
		                                             } 
		                                         }). 
//		                                  		setNegativeButton("拒绝", new DialogInterface.OnClickListener() { 
		                                    	setNegativeButton("Refuse", new DialogInterface.OnClickListener() { 
		                                             @Override 
		                                             public void onClick(DialogInterface dialog, int which) { 
		                                            	 arg0.get(0).put("yaoqingState", 2);
		                                            	 arg0.get(0).saveInBackground(new SaveCallback() {
															@Override
															public void done(BmobException arg0) {
																if(arg0!=null){
//														            Toast.makeText(LuxianMapActivity.this, "网络错误!", Toast.LENGTH_SHORT).show();  
														            Toast.makeText(LuxianMapActivity.this, "Network error!", Toast.LENGTH_SHORT).show();  
														            return;
																}
				                                            	isShowing=false;
															}
														});
		                                             } 
		                                         }). 
		                                         create(); 
		                                 alertDialog.show();
		                        	}
		                     	});
							}
						}
					});
    				
    				if(points[1]!=null){
        				BmobQuery queryMy = BmobQuery.getUserQuery();
        				queryMy.whereEqualTo("username", BmobUser.getCurrentUser().getUsername());
        				BmobObject currUser = null;
        				try {
							currUser = queryMy.find().get(0);
						} catch (BmobException e1) {
							e1.printStackTrace();
						}
        				currUser.put("weizhiX", points[1][0]/1000000f);
        				currUser.put("weizhiY", points[1][1]/1000000f);
    					try {
    						currUser.save();
						} catch (BmobException e) {
							e.printStackTrace();
						}
    				}
    				try {
						sleep(30000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
    			}
    		};
    	}.start();
    }
    
    private void updateUI(){
    	points[1] = getGPRStr();
    	if(points[1]==null){
//        	Toast.makeText(LuxianMapActivity.this, "无法定位，请检查网络和GPS!", Toast.LENGTH_SHORT).show();  
            Toast.makeText(LuxianMapActivity.this, "Unable to locate, check the network and GPS!", Toast.LENGTH_SHORT).show();  
            return;
    	}
    	new AsyncTask(){
    		@Override
    		protected Object doInBackground(Object... params) {
    			
    			BmobQuery query = BmobQuery.getUserQuery();
    			query.whereEqualTo("userType", 1);
    			ArrayList<BmobObject> adminList = null;
    			try {
					adminList = query.find();
				} catch (BmobException e) {
					e.printStackTrace();
		            return null;
				}
    			if(adminList.size()<1){
		            return null;
    			}
    			BmobObject adminUser = adminList.get(0);
    			points[0]=new int[]{(int) (adminUser.getNumber("weizhiX").floatValue()*1000000),(int) (adminUser.getNumber("weizhiY").floatValue()*1000000)};
    			
    			BmobQuery queryDuiwu = new BmobQuery("duiwu");
    			ArrayList<BmobObject> duiwuList = null;
    			try {
    				duiwuList = queryDuiwu.find();
				} catch (BmobException e) {
					e.printStackTrace();
		            return null;
				}
    			if(duiwuList.size()<1){
		            return null;
    			}
    			BmobObject duiwu = duiwuList.get(0);
    			points[2]=new int[]{(int) (duiwu.getNumber("mudidiX").floatValue()*1000000),(int) (duiwu.getNumber("mudidiY").floatValue()*1000000)};
    			path = decodePoly(getPath()); 
    			return null;
    		}
    		@Override
    		protected void onPostExecute(Object result) { 
    			if(points[0]!=null&&points[2]!=null&&points[1]!=null){
        	    	geoPoint = new GeoPoint((points[1][0]+points[2][0])/2,(points[1][1]+points[2][1])/2);
        	    	if(first){
	        	    	mapController.animateTo(geoPoint);
	        	    	mapController.setZoom(getZoomLevel());
	        	    	first = false;
        	    	}
    		    	txtJuli.setText("距离:"+(int)distance(points[1][0]/1000000f,points[1][1]/1000000f,points[0][0]/1000000f,points[0][1]/1000000f)+"米");
    			}
    			mapView.getOverlays().removeAll(mapView.getOverlays());
		    	mapView.getOverlays().add(new MyLocationOverlay());
                mapView.invalidate();  
                handler.postDelayed(task, 30000); 
    			super.onPostExecute(result);
    		}
    	}.execute(""); 
    }
    
    private int[] getGPRStr(){
    	String[] gpsStrs =  getGps(this);
    	if(gpsStrs==null){
        	gpsStrs= getGpsFromNetwork(this);
    	}
    	if(gpsStrs!=null){
    		return new int[]{(int)(Float.parseFloat(gpsStrs[1])*1000000),(int)(Float.parseFloat(gpsStrs[0])*1000000)};
    	}
    	return null;
    }
	
	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	class MyLocationOverlay extends Overlay
    {
        @Override
        public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when)
        {
            super.draw(canvas, mapView, shadow);
            
            Paint paint = new Paint();
            
            if(path!=null){
	            Paint paint2 = new Paint();
	            paint2.setARGB(100,0,0,255);
	            paint2.setAlpha(150);
	            paint2.setAntiAlias(true);
	            paint2.setStyle(Paint.Style.FILL_AND_STROKE);
	            paint2.setStrokeWidth(4);
	            Projection projection = mapView.getProjection();
				if (path != null && path.size() >= 2) {// ����
					Point start = new Point();
					projection.toPixels(path.get(0), start);// ��Ҫת�����
					for (int i = 1; i < path.size(); i++) {
						Point end = new Point();
						projection.toPixels(path.get(i), end);
						canvas.drawLine(start.x, start.y, end.x, end.y, paint2);// ���Ƶ�canvas�ϼ���
						start = end;
					}
				}
            }

            if(points[0]!=null){
	            Point myScreenCoords = new Point();
	            mapView.getProjection().toPixels(new GeoPoint(points[0][0],points[0][1]), myScreenCoords);
	            paint.setStrokeWidth(5);
	            paint.setARGB(255, 255, 0, 0);
	            paint.setStyle(Paint.Style.FILL_AND_STROKE);
	            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_locr_normal);
	            canvas.drawBitmap(bmp, myScreenCoords.x-bmp.getWidth()/2, myScreenCoords.y-bmp.getHeight()/2, paint);
            }
            
            if(points[2]!=null){
	            Point myScreenCoords3 = new Point();
	            mapView.getProjection().toPixels(new GeoPoint(points[2][0],points[2][1]), myScreenCoords3);
	            Bitmap bmp3 = BitmapFactory.decodeResource(getResources(), R.drawable.icon_gcoding);
	            canvas.drawBitmap(bmp3, myScreenCoords3.x-bmp3.getWidth()/3, myScreenCoords3.y-bmp3.getHeight(), paint);
	            
            }
            
            if(points[1]!=null){
	            Point myScreenCoords = new Point();
	            mapView.getProjection().toPixels(new GeoPoint(points[1][0],points[1][1]), myScreenCoords);
	            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.icon_loc_normal);
	            double jiaodu = Math.atan((points[0][0]-points[1][0]+1f) / (points[0][1]-points[1][1]+1f)) / 3.14 * 180;
	            if((points[0][1]-points[1][1]+1f)<0){
	            	jiaodu = 180+90-(int)jiaodu;
	            }else{
	            	jiaodu = 90-(int)jiaodu;
	            } 
	            canvas.drawBitmap(rotate(bmp,(int)jiaodu), myScreenCoords.x-bmp.getWidth()/2, myScreenCoords.y-bmp.getWidth()/2, paint);
            }
            

            if(points[0]!=null&&points[1]!=null){
	            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.jiantou);
	            double jiaodu = Math.atan((points[0][0]-points[1][0]+1f) / (points[0][1]-points[1][1]+1f)) / 3.14 * 180;
	            if((points[0][1]-points[1][1]+1f)<0){
	            	jiaodu = 180+90-(int)jiaodu;
	            }else{
	            	jiaodu = 90-(int)jiaodu;
	            } 
	            Bitmap newbmp  = rotate(bmp,(int)jiaodu);
	            canvas.drawBitmap(newbmp, canvas.getWidth()-bmp.getWidth()-(newbmp.getWidth()-bmp.getWidth())/2,-(newbmp.getHeight()-bmp.getHeight())/2, paint);
            }
			
            return true;
        }
    }
	
	private String getPath(){
		String url = "http://maps.google.com/maps/api/directions/xml?origin="+points[0][0]/1000000f+","+points[0][1]/1000000f+"&destination="+points[2][0]/1000000f+","+points[2][1]/1000000f+"&sensor=false";  
          
        HttpGet get = new HttpGet(url);  
        String strResult = "";   
        try {  
            HttpParams httpParameters = new BasicHttpParams();  
            HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);  
            HttpClient httpClient = new DefaultHttpClient(httpParameters);   
              
            HttpResponse httpResponse = null;  
            httpResponse = httpClient.execute(get);  
              
            if (httpResponse.getStatusLine().getStatusCode() == 200){  
                strResult = EntityUtils.toString(httpResponse.getEntity());  
            }  
        } catch (Exception e) {  
            return "";  
        }  
          
        if (-1 == strResult.indexOf("<status>OK</status>")){  
//       	Toast.makeText(this, "获取导航路线失败!", Toast.LENGTH_SHORT).show();  
            Toast.makeText(this, "Failed to obtain the navigation routes!", Toast.LENGTH_SHORT).show();  
            this.finish();  
            return "";  
        }  
        int pos = strResult.indexOf("<overview_polyline>");   
        pos = strResult.indexOf("<points>", pos + 1);   
        int pos2 = strResult.indexOf("</points>", pos);   
        String pathStr = strResult.substring(pos + 8, pos2);   
        
        pos = strResult.lastIndexOf("<distance>");  
        pos = strResult.indexOf("<text>", pos + 1);  
        pos2 = strResult.indexOf("</text>", pos);   
        pathJuli = strResult.substring(pos + 6, pos2);  
        
        return pathStr;
	}
	 
	
	/** 
     * 解析返回xml中overview_polyline的路线编码 
     *  
     * @param encoded 
     * @return 
     */  
    private List<GeoPoint> decodePoly(String encoded) {  
  
        List<GeoPoint> poly = new ArrayList<GeoPoint>();  
        int index = 0, len = encoded.length();  
        int lat = 0, lng = 0;  
  
        while (index < len) {  
            int b, shift = 0, result = 0;  
            do {  
                b = encoded.charAt(index++) - 63;  
                result |= (b & 0x1f) << shift;  
                shift += 5;  
            } while (b >= 0x20);  
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));  
            lat += dlat;  
  
            shift = 0;  
            result = 0;  
            do {  
                b = encoded.charAt(index++) - 63;  
                result |= (b & 0x1f) << shift;  
                shift += 5;  
            } while (b >= 0x20);  
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));  
            lng += dlng;  
  
            GeoPoint p = new GeoPoint((int) (((double) lat / 1E5) * 1E6),  
                 (int) (((double) lng / 1E5) * 1E6));  
            poly.add(p);  
        }  
  
        return poly;  
    }  
    
    private int getZoomLevel(){
    	int x =Math.abs(points[1][0]-points[2][0]);
    	int y =Math.abs(points[1][1]-points[2][1]);
    	if( x<500 && y<500){
        	return 21;
    	}
    	else if( x<1000 && y<1000){
        	return 20;
    	}
    	else if( x<2000 && y<2000){
        	return 19;
    	}
    	else if( x<4000 && y<4000){
        	return 18;
    	}
    	else if( x<8000 && y<8000){
        	return 17;
    	}
    	else if( x<16000 && y<16000){
    		return 16;
    	}
    	else if( x<32000 && y<32000){
    	return 15;
    	}
    	else if( x<64000 && y<64000){
        	return 14;
    	}
    	else if( x<130000 && y<130000){
        	return 13;
    	}
    	else if( x<260000 && y<260000){
        	return 12;
    	}
    	else if( x<500000 && y<500000){
        	return 11;
    	}
    	else if( x<1000000 && y<1000000){
        	return 10;
    	}
    	else if( x<2000000 && y<2000000){
        	return 9;
    	}
    	else if( x<4000000 && y<4000000){
        	return 8;
    	}
    	else if( x<8000000 && y<8000000){
        	return 7;
    	}
    	else if( x<16000000 && y<16000000){
        	return 6;
    	}
    	return 5;
    }
    

	/**
	 * 通过GPS获取经纬度 适用于Gps
	 * 
	 * @return String[] 0:longitude,1:latitude
	 */
	public String[] getGps(Context context)
	{

		String[] gpsInfo = null;
		try
		{
			double lat = 0.0;
			double lon = 0.0;
			LocationManager lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			LocationListener locationListener = new LocationListener()
			{

				// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras)
				{

				}

				// Provider被enable时触发此函数，比如GPS被打开
				@Override
				public void onProviderEnabled(String provider)
				{

				}

				// Provider被disable时触发此函数，比如GPS被关闭
				@Override
				public void onProviderDisabled(String provider)
				{

				}

				// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
				@Override
				public void onLocationChanged(Location location)
				{

				}

			};
			lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 60000, 5f,
					locationListener);

			Location location = lm
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null)
			{
				lat = location.getLatitude();
				lon = location.getLongitude();
				gpsInfo = new String[2];
				gpsInfo[0] = String.valueOf(lon);
				gpsInfo[1] = String.valueOf(lat);
			}

		} catch (Exception e)
		{
			Log.e("exception", e.getMessage());
			Message msg=new Message();
			msg.what=999;
			msg.obj="getGps() "+e.getMessage();
		}
		return gpsInfo;
	}
	
	/**
	 * 通过网络获取经纬度 适用于wifi和GPRS
	 * 
	 * @return String[] 0:longitude,1:latitude
	 */
	public String[] getGpsFromNetwork(Context context)
	{

		String[] gpsInfo = null;
		try
		{
			double lat = 0.0;
			double lon = 0.0;
			locMan = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			LocationListener locationListener = new LocationListener()
			{
				// Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
				@Override
				public void onStatusChanged(String provider, int status,
						Bundle extras)
				{

				}

				// Provider被enable时触发此函数，比如GPS被打开
				@Override
				public void onProviderEnabled(String provider)
				{

				}

				// Provider被disable时触发此函数，比如GPS被关闭
				@Override
				public void onProviderDisabled(String provider)
				{

				}

				// 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
				@Override
				public void onLocationChanged(Location location)
				{

				}

			};
			locMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
					60000, 5f, locationListener);
			Location location = locMan.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null)
			{
				Log.e("tag", "network!=null");
				lat = location.getLatitude();
				lon = location.getLongitude();
				gpsInfo = new String[2];
				gpsInfo[0] = String.valueOf(lon);
				gpsInfo[1] = String.valueOf(lat);
				// loInfo = new LocationInfo(lat, lon);
				Log.i("getGpsFromNetwork()", "Longitude=" + gpsInfo[0]
						+ " Latitude=" + gpsInfo[1]);
			}

		} catch (Exception e)
		{
			Log.e("exception", e.getMessage());
			Message msg=new Message();
			msg.what=999;
			msg.obj="getGpsFromNetwork() "+e.getMessage();
		}
		return gpsInfo;

	}
	
	public static Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();
            m.setRotate(degrees,
                    (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();   
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
            	ex.printStackTrace();
            }
        }
        return b;
    }
	
	public double distance(double lat1, double lon1, double lat2, double lon2) {  
        double theta = lon1 - lon2;  
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2))  
                                + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))  
                                * Math.cos(deg2rad(theta));  
        dist = Math.acos(dist);  
        dist = rad2deg(dist);  
        double miles = dist * 60 * 1.1515;  
        return miles*1609.344;
	}
	//将角度转换为弧度  
	public static double deg2rad(double degree) {  
	        return degree / 180 * Math.PI;  
	}  
	//将弧度转换为角度  
	public static double rad2deg(double radian) {  
	        return radian * 180 / Math.PI;  
	}  
	
}