package info.duanye.dusdk;

import java.util.Date;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;

public class BDLocReceiver extends BroadcastReceiver {

	private LocationClient loccli;
	private SQLiteDatabase db;
	private OneTimeListenerToDb listener;
	private Context context;

	public BDLocReceiver(Context context, LocationClient loccli, SQLiteDatabase db) {
		this.loccli = loccli;
		this.db = db;
		this.listener = new OneTimeListenerToDb();
		this.context = context;
	}

	public void onReceive(Context arg0, Intent arg1) {
		System.out.println(getClass().getName() + "On receive --- Cordova");
		if (Commons.BACKGROUND_LOCATE_ACTION.equals(arg1.getAction())) {
			loccli.registerLocationListener(listener);
			loccli.requestLocation();
		}
	}

	private class OneTimeListenerToDb implements BDLocationListener {

		
		public void onReceiveLocation(BDLocation arg0) {
			if ("INVALID".equals(Commons.getLocTypeString(arg0)))
				showNotification();
			else
				Commons.saveLocation(db, arg0);
			System.out.println("Cordova : listenerBack got : " + arg0.getLatitude() + " " + arg0.getLongitude()
					+ " at " + Commons.stdFormat(Commons.bdParse(arg0.getTime())) + " when "
					+ Commons.stdFormat(new Date()));
			loccli.unRegisterLocationListener(this);
		}
	}
	
	public void showNotification(){
		NotificationManager nm = (NotificationManager) context.getSystemService(Service.NOTIFICATION_SERVICE);
		Notification no = new Notification();
		no.icon = android.R.drawable.ic_menu_mylocation;
		no.tickerText = "后台定位失败, 请给应用授权或尝试开启GPS";
		no.when = System.currentTimeMillis();
		no.defaults |= Notification.DEFAULT_VIBRATE;
		no.setLatestEventInfo(context, "后台定位失败", "请给应用授权或尝试开启GPS", null);
		nm.notify(0, no);
		System.out.println(no.toString() + " --- Cordova");
	}
	
}