package modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferentialManager {
	
	public final static String kWifi = "wifi";
	public final static String kNetwork = "network";
	public final static String kGps = "gps";
	public final static String kSync = "sync";
	public final static String kBluetooth = "bluetooth";
	
	public void saveDeviceSetting (Context context, String deviceName, boolean option)
	{
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		SharedPreferences.Editor editor = prefs.edit();
		if (option) {
			editor.putString(deviceName, "yes");
		} else {
			editor.putString(deviceName, "no");
		}
		editor.commit();
	}
	
	public boolean fetchDeviceSetting (Context context, String deviceName)
	{
		SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(context);
		if (prefs.getString(deviceName, null) == null) {
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString(deviceName, "yes");
			return true;
		}
		if (prefs.getString(deviceName, null) == "yes") {
			return true;
		} else {
			return false;
		}
	}
}