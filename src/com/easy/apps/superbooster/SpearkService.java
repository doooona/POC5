package com.easy.apps.superbooster;

import com.db2.apps.networkbooster.R;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.audiofx.Equalizer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SpearkService extends Service{
	
	private final Messenger messenger = new Messenger(new UpdateHandler());
	private Equalizer eq = null;
	private short bands;
	private short rHigh;
	private short rLow;
	
	@Override
	public void onCreate(){
		try{
			this.eq = new Equalizer(1234567, 0);
			bands = eq.getNumberOfBands();
			this.rLow = this.eq.getBandLevelRange()[0];
	        this.rHigh = this.eq.getBandLevelRange()[1];
	        
	        Toast.makeText(this,this.getString(R.string.equalizer_on),Toast.LENGTH_LONG).show();
	        
		}catch(Exception e){
			e.printStackTrace();
			eq = null;
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return this.messenger.getBinder();
	}
	
	public class UpdateHandler extends Handler{
		
	   @Override
	   public void handleMessage(Message msg){
			 switch(msg.what){
			 	case 1:
			 		
			 		updateEqualizer();
			 		
			 		break;
			 }
			 
			 super.handleMessage(msg);
	   }
	}
	
	@Override
	public void onDestroy(){
		
		if(this.eq != null){
			this.eq.setEnabled(false);
			this.eq.release();
			this.eq = null;
		}
	}
	
	private void updateEqualizer(){
		Log.d("@@@", "updateEqualizer...");
		
		if(eq != null){
			
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int boostValue = prefs.getInt("boostValue", 60);
			short addValue = (short)((750 + boostValue * this.rHigh) / 1500);
			if(addValue<0)
				addValue = 0;
			if(addValue > this.rHigh)
				addValue = this.rHigh;
			
			if(addValue != 0){
				this.eq.setEnabled(true);
				short band = 0;
				while(band < this.bands){
					
					short add = addValue;
					int sp;
					
					try{
						
						sp = this.eq.getCenterFreq(band);
						
						if(sp < 150)
							add = 0;
						
						if(sp < 250)
							add = (short) (add / 2);
						
						if(sp <= 8000)
							add = (short) (add * 3 / 4);
						
						Log.d("@@@", "setBandLevel..."+ band +"," + add);
						
						this.eq.setBandLevel(band, add);
						
						band = (short) ((short)band +1);
						
						
					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
			}
		}
	}

}
