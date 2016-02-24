package info.duanye.dusdk;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

public class Commons {
	private static final SimpleDateFormat BD_FORMAT = new SimpleDateFormat("y-M-d H:m:s");
	private static final SimpleDateFormat STD_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static final String BACKGROUND_LOCATE_ACTION = "info.duanye.dusdk.BACKGROUND_LOCATE";

	public static String stdFormat(Date in) {
		return STD_FORMAT.format(in);
	}

	public static String bdFormat(Date in) {
		return BD_FORMAT.format(in);
	}

	public static Date stdParse(String in) {
		try {
			return STD_FORMAT.parse(in);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Date bdParse(String in) {
		try {
			return BD_FORMAT.parse(in);
		}
		catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void createTable(SQLiteDatabase db) {
		db.execSQL("create table if not exists BD_TRACE(LATITUDE TEXT, LONGITUDE TEXT, TIME TEXT PRIMARY KEY, GOT_TIME TEXT, LOC_TYPE TEXT)");
	}

	public static void saveLocation(SQLiteDatabase db, BDLocation arg0) {
		SQLiteStatement stmt = db.compileStatement("insert or replace into BD_TRACE values(?, ?, ?, ?, ?)");
		stmt.bindDouble(1, arg0.getLatitude());
		stmt.bindDouble(2, arg0.getLongitude());
		stmt.bindString(3, stdFormat(new Date()));
		stmt.bindString(4, stdFormat(bdParse(arg0.getTime())));
		stmt.bindString(5, getLocTypeString(arg0));
		stmt.execute();
	}
	
	public static String getLocTypeString(BDLocation arg0){
		switch (arg0.getLocType()){
			case BDLocation.TypeCacheLocation:
				return "CACHE";
			case BDLocation.TypeGpsLocation:
				return "GPS";
			case BDLocation.TypeNetWorkLocation:
				if ("wf".equalsIgnoreCase(arg0.getNetworkLocationType()))
					return "WIFI";
				else
					return "CELL";
			case BDLocation.TypeOffLineLocation:
				return "CACHE";
			default:
				return "INVALID";
		}
	}
	
	public static JSONObject locToJson(BDLocation arg0){
		JSONObject ret = new JSONObject();
		try {
			ret.put("latitude", arg0.getLatitude());
			ret.put("longitude", arg0.getLongitude());
			ret.put("time", Commons.stdFormat(new Date()));
			ret.put("got_time", Commons.stdFormat(Commons.bdParse(arg0.getTime())));
			ret.put("loc_type", Commons.getLocTypeString(arg0));
		}
		catch (JSONException e) {
			e.printStackTrace();
		}
		return ret;
	}
	
	public static JSONObject fetchJsonFromCursor(Cursor cur){
		JSONObject ret = new JSONObject();
		int l = cur.getColumnCount();
		for (int i = 0; i < l; i ++)
			try {
				ret.put(cur.getColumnName(i).toLowerCase(), cur.getString(i));
			}
			catch (JSONException e) {
				e.printStackTrace();
			}
		return ret;
	}
	
	public static JSONArray cursorToJson(Cursor cur){
		JSONArray ret = new JSONArray();
		while (cur.moveToNext()){
			ret.put(fetchJsonFromCursor(cur));
		}
		return ret;
	}
	

}
