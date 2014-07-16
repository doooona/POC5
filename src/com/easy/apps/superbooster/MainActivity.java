package com.easy.apps.superbooster;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.db2.apps.networkbooster.R;
import com.google.ads.AdRequest;
import com.google.ads.AdView;

import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements ServiceConnection{

	private TextView tv_network;
	private TextView tv_speark;
	private SeekBar sk_speark;
	private ImageView img_reloading;
	private LinearLayout btn_network_booster;
	private SharedPreferences mPrefs;
	private AudioManager am;
	private Messenger messenger;
	private final static int TYPE_3G_START = 0;
	private final static int TYPE_WIFI_START = 2;
	private final static int TYPE_3G_DONE = 1;
	private final static int TYPE_WIFI_DONE = 3;
	private final static int TYPE_FLY_START = 4;
	private final static int TYPE_FLY_DONE = 5;
	private AdView adView;
	
	private Handler mHandler = new Handler(){
		@Override
		   public void handleMessage(Message msg){
			MainActivity.this.tv_network.setTextColor(Color.RED);
			updateNetworBoostStatus(msg.what);
		}
	};
	
	private void updateNetworBoostStatus(int what){
		switch(what){
		    case TYPE_3G_START:
		    	this.tv_network.setText(this.getString(R.string.status_network_3g_boosting));
		    	break;
		    case TYPE_3G_DONE:
		    	this.tv_network.setText(this.getString(R.string.status_network_3g_boosted));
		    	break;
		    case TYPE_WIFI_START:
		    	this.tv_network.setText(this.getString(R.string.status_network_wifi_boosting));
		    	break;
		    case TYPE_WIFI_DONE:
		    	this.tv_network.setText(this.getString(R.string.status_network_wifi_boosted));
		    	break;
		    case TYPE_FLY_START:
		    	this.tv_network.setText(this.getString(R.string.status_network_fly_boosting));
		    	break;
		    case TYPE_FLY_DONE:
		    	this.tv_network.setText(this.getString(R.string.status_network_fly_boosted));
		    	break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		 adView = (AdView)findViewById(R.id.adView);
	        adView.loadAd(new AdRequest());
		
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		this.am = ((AudioManager)getSystemService("audio"));
		initUI();
		
		needServices(true);
		
	}
	
	private void update2SpearkService(){
		if(this.messenger!=null){
			try{
				messenger.send(Message.obtain(null, 1, 0, 0));
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	private void startLoadingAnimation(){
		Animation anim = AnimationUtils.loadAnimation(this, R.anim.round_loading); 
		img_reloading.startAnimation(anim);
	}
	
	private void stopLoadingAnimation(){
		img_reloading.clearAnimation();
	}
	
	private void boostNetwork(){
		try{
			if(is3GOn()){
				Log.d("@@@", "1");
				this.mHandler.sendEmptyMessage(0);
				refrest3G(false);
				
				Thread.sleep(1000);
				refrest3G(true);
				Log.d("@@@", "2");
				this.mHandler.sendEmptyMessage(1);
			}if(isWiFiOn()){
				Log.d("@@@", "3");
				Thread.sleep(500);
				this.mHandler.sendEmptyMessage(2);
				WifiManager wifiManager = (WifiManager)this.getSystemService("wifi");
				wifiManager.setWifiEnabled(false);
				
				Thread.sleep(1000);
				wifiManager.setWifiEnabled(true);
				Log.d("@@@", "4");
				this.mHandler.sendEmptyMessage(3);
				
			}if(isAirplaneModeOn()){
				//this.mHandler.sendEmptyMessage(4);
				android.provider.Settings.System.putInt(
	                    this.getContentResolver(),"airplane_mode_on",0);
				Intent localIntent =  new Intent("android.intent.action.AIRPLANE_MODE");
				localIntent.putExtra("state", false);
				this.sendBroadcast(localIntent);
				
				Thread.sleep(1000);
				android.provider.Settings.System.putInt(
	                    this.getContentResolver(),"airplane_mode_on",1);
				Intent localIntent2 =  new Intent("android.intent.action.AIRPLANE_MODE");
				localIntent2.putExtra("state", true);
				this.sendBroadcast(localIntent2);
				//this.mHandler.sendEmptyMessage(5);
	
			}
				
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void refrest3G(boolean flag){
		try{
			  ConnectivityManager localConnectivityManager = (ConnectivityManager)this.getSystemService("connectivity");
		      Field localField = Class.forName(localConnectivityManager.getClass().getName()).getDeclaredField("mService");
		      localField.setAccessible(true);
		      Object localObject = localField.get(localConnectivityManager);
		      Class localClass = Class.forName(localObject.getClass().getName());
		      Class[] arrayOfClass = new Class[1];
		      arrayOfClass[0] = Boolean.TYPE;
		      Method localMethod = localClass.getDeclaredMethod("setMobileDataEnabled", arrayOfClass);
		      localMethod.setAccessible(true);
		      Object[] arrayOfObject = new Object[1];
		      arrayOfObject[0] = Boolean.valueOf(flag);
		      localMethod.invoke(localObject, arrayOfObject);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private boolean is3GOn(){
		ConnectivityManager localConnectivityManager = (ConnectivityManager)this.getSystemService("connectivity");
		Boolean flag = false;
		try
	    {
		    Method localMethod = Class.forName(localConnectivityManager.getClass().getName()).
					  getDeclaredMethod("getMobileDataEnabled", new Class[0]);
		    localMethod.setAccessible(true);
		    flag = (Boolean)localMethod.invoke(localConnectivityManager, new Object[0]);
		   
	    }catch(Exception e){
			e.printStackTrace();
		}
		
		return flag;
	}
	
	private boolean isAirplaneModeOn()
	{
		return android.provider.Settings.System.getInt(this.getContentResolver(),
				android.provider.Settings.System.AIRPLANE_MODE_ON, 0) != 0;
	}
	
	private boolean isWiFiOn(){
		
		int status = ((WifiManager)this.getSystemService("wifi")).getWifiState();
		if((status != WifiManager.WIFI_STATE_DISABLED && status != WifiManager.WIFI_STATE_DISABLING) &&
				((WifiManager)this.getSystemService("wifi")).isWifiEnabled())
			return true;
		else
			return false;
		
	}
	@Override
	public void onDestroy(){
		
		this.unbindService(this);
		if(adView!=null)
			 adView.destroy();
		
		super.onDestroy();
	}
	
	private void initUI(){
		tv_network = (TextView) this.findViewById(R.id.textView2);
		tv_speark = (TextView) this.findViewById(R.id.textView1);
		sk_speark = (SeekBar)this.findViewById(R.id.seekBar1);
		img_reloading = (ImageView)this.findViewById(R.id.imageView1);
		btn_network_booster = (LinearLayout) this.findViewById(R.id.ll1);
		btn_network_booster.setOnClickListener(new OnClickListener(){

			@SuppressWarnings({ "unchecked", "rawtypes" })
			@Override
			public void onClick(View v) {
				
				new AsyncTask(){
					
					@Override
		            protected void onPreExecute() {
						startLoadingAnimation();
					}
					
					@Override
					protected Object doInBackground(Object... arg0) {
						// TODO Auto-generated method stub
						
						boostNetwork();
						
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						return null;
					}
					@Override
					protected void onPostExecute(Object result) {
						
						stopLoadingAnimation();
						MainActivity.this.tv_network.setText(MainActivity.this.getString(R.string.status_network_all_ok));
						MainActivity.this.tv_network.setTextColor(Color.BLACK);
						Toast.makeText(MainActivity.this,MainActivity.this.getString(R.string.status_network_all_ok),
								Toast.LENGTH_LONG).show();
					}
				}.execute();
			}
			
		});
		
		sk_speark.setMax(60 *100);
		
		setDefaultSpearkerValue(60 *100);
		
		sk_speark.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
			
		int maxVol = am.getStreamMaxVolume(3);	
			
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				if(arg2){
					 int addVol = (maxVol * arg1 + 10000 / 2) / 10000;	
					 am.setStreamVolume(3, addVol, 0);
					 String speedMax = (arg1*100 + 5000)/ 10000 +"%";
					 String msg = String.format(MainActivity.this.getString(R.string.msg_already_add_vol), speedMax);
					 tv_speark.setText(MainActivity.this.getString(R.string.txt_speark) +" "+ msg);
					 
					 update2SpearkService();
				}
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		showSeekBar();
	}
	
	private void needServices(boolean flag){
		stopService();
		
		this.startService(new Intent(this,SpearkService.class));
		
		this.bindService(new Intent(this,SpearkService.class), this, 0);
	}
	
	void stopService()
	{
	    if (this.messenger != null)
	    {
	      unbindService(this);
	      this.messenger = null;
	    }
	    stopService(new Intent(this, SpearkService.class));
	 }
	
	private void showSeekBar(){
		int now = (((this.am.getStreamVolume(3) - 0) * 10000 + (this.am.getStreamMaxVolume(3) - 0)) / 2) / (this.am.getStreamMaxVolume(3) - 0);
		Log.d("@@@", "now:"+ now);
		this.sk_speark.setProgress(now);
		
		String speedMax = (now*100 + 5000)/ 10000 +"%";
		String msg = String.format(MainActivity.this.getString(R.string.msg_already_add_vol), speedMax);
		tv_speark.setText(MainActivity.this.getString(R.string.txt_speark) +" "+ msg);
		
	}
	
	private void setDefaultSpearkerValue(int max){
		
		int defaultboostValue = mPrefs.getInt("boostValue", -1);
		
		if (defaultboostValue > max * 15)
	    {
			mPrefs.edit().putInt("boostValue", max * 15);
	    }
	}

	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
	    	 super.onCreateOptionsMenu(menu);
	    	 
	    	 MenuItem item2=menu.add(1,3,0,this.getString(R.string.rate));
	         item2.setIcon(android.R.drawable.ic_menu_rotate);
	    	 
	         MenuItem item1=menu.add(1,2,0,this.getString(R.string.more_app));
	         item1.setIcon(android.R.drawable.ic_menu_more);
	         
	         MenuItem item3=menu.add(1,4,0,this.getString(R.string.download_network));
	         item3.setIcon(android.R.drawable.ic_menu_add);
	         return true;
	    }
	 
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	    	switch (item.getItemId()) {
	    		
	    		case 2:
	    			
	    			Intent intent = new Intent("android.intent.action.VIEW");
	                intent.setData(Uri.parse("market://search?q=gogogogo1101&c=apps"));
	                startActivity(intent);
	    			
	    			break;
	    			
	    		case 3:
	    			
	    			Intent intent2 = new Intent("android.intent.action.VIEW");
	                intent2.setData(Uri.parse("market://details?id=com.db2.apps.networkbooster"));
	                startActivity(intent2);
	    			break;
	    		
	    		case 4:
	    			
	    			Intent intent3 = new Intent("android.intent.action.VIEW");
	                intent3.setData(Uri.parse("market://details?id=com.easy.apps.networkflowwindows"));
	                startActivity(intent3);
	    			break;
	    	}
	    	
	    	return true;
	 }

	@Override
	public void onServiceConnected(ComponentName arg0, IBinder arg1) {
		// TODO Auto-generated method stub
		this.messenger = new Messenger(arg1);
	}

	@Override
	public void onServiceDisconnected(ComponentName arg0) {
		// TODO Auto-generated method stub
		this.messenger = null;
	}

}
