package info.duanye.dusdk;

import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.google.gson.Gson;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.SystemClock;

public class BDLocPlugin extends CordovaPlugin {
	private LocationClient loccli;
	LocationClientOption lco;
	private Context context;
	private SQLiteDatabase db;
	private PendingIntent locationPendingIntent;
	private BDLocReceiver recv;
	
	private AlarmManager am;

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

		context = this.cordova.getActivity().getApplicationContext();

		db = openTraceDb();

		Intent intent = new Intent(Commons.BACKGROUND_LOCATE_ACTION);
		locationPendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		am = (AlarmManager) context.getSystemService(Service.ALARM_SERVICE);
//		am.setRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis(), 15000, null);
		
		lco = new LocationClientOption();
		lco.setLocationMode(LocationMode.Hight_Accuracy);
		lco.setOpenGps(true);

		loccli = new LocationClient(context);
		
		loccli.registerLocationListener(new BDLocationListener() {

			@Override
			public void onReceiveLocation(BDLocation arg0) {

			}
		});
		
		loccli.setDebug(true);
		loccli.start();
		
		recv = new BDLocReceiver(context, loccli, db);
		context.registerReceiver(recv, new IntentFilter(Commons.BACKGROUND_LOCATE_ACTION));
	}

	private JSONObject voidParam = new JSONObject();

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
		try {
			System.out.println("Cordova : call " + action);
			JSONObject param = args.optJSONObject(0);
			if (param == null)
				param = voidParam;

			if ("getLocation".equals(action)) {
				new OnetimeListenerForExec(callbackContext);
				loccli.requestLocation();
				return true;
			} else if ("startBackground".equals(action)) {

				int span = param.optInt("span", 15000);
				
				am.cancel(locationPendingIntent);
				am.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(), span, locationPendingIntent);
				
				String modeStr = param.optString("mode", "Hight_Accuracy");
				LocationMode mode = LocationMode.valueOf(modeStr);
				
				lco.setLocationMode(mode);
				loccli.setLocOption(lco);
				System.out.println("Cordova locate span : " + span + ", mode: " + mode.toString());
				callbackContext.success();
				return true;
			} else if ("stopBackground".equals(action)) {
				am.cancel(locationPendingIntent);
				callbackContext.success();
				return true;
			} else if ("getBackgroundTrace".equals(action)) {
				//				System.out.println("Cordova : " + "getBackgroundTrace with :" + args.getJSONObject(0).toString());

				callbackContext.success(getTrace(param.optString("from"), param.optString("to")));
				return true;
			}
			// We don't have a match, so it must be an invalid action
			callbackContext.error("Invalid Action");
			return false;
		}
		catch (Exception e) {
			// If we get here, then something horrible has happened
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}

	private JSONArray getTrace(String from, String to) {
		if (from == null || "".equals(from))
			from = "0";
		if (to == null || "".equals(to))
			to = "A";
		
		Cursor cur = db.rawQuery("select * from BD_TRACE where TIME > '" + from + "' and TIME < 'to'", new String[] {});
		JSONArray arr = Commons.cursorToJson(cur);
		System.out.println("Cordova : BackgroundTrace between :" + from + " and " + to);
		cur.close();
		return arr;
	}

	private SQLiteDatabase openTraceDb() {
		File dbFile = context.getDatabasePath("trace.db");
		if (!dbFile.exists())
			dbFile.getParentFile().mkdirs();
		SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(dbFile, null);
		Commons.createTable(db);
		return db;
	}

	private class OnetimeListenerForExec implements BDLocationListener {

		private CallbackContext callback;

		public OnetimeListenerForExec(CallbackContext callback) {
			this.callback = callback;
			loccli.registerLocationListener(this);
		}

		@Override
		public void onReceiveLocation(BDLocation arg0) {
			System.out.println("Cordova : " + new Gson().toJson(arg0));
			JSONObject ret = Commons.locToJson(arg0);
			System.out.println("Cordova : " + ret);
			callback.success(ret);

			loccli.unRegisterLocationListener(this);
		}
	}

}
