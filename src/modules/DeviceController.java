package modules;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;

public class DeviceController {
	
	private Context mContext;
	
	final private WifiManager mWifi;
	final private BluetoothAdapter mBluetooth;
	final private ConnectivityManager mNetwork;
	final private LocationManager mGps;
	
	/*----------- constructor -----------*/
	public DeviceController(Context _mContext) {
		mContext = _mContext;
		mWifi = (WifiManager) mContext.getSystemService (Context.WIFI_SERVICE);
        mGps = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);		
		mBluetooth = BluetoothAdapter.getDefaultAdapter();
		mNetwork = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
	}
	
	/*---------------- switching device status ----------------*/
	// true for enabling, false for disabling
	public void switchWifi(boolean isEnable) {
		if (isEnable && !isWifiOn()) {
			mWifi.setWifiEnabled(true);
		} else if (!isEnable && isWifiOn()) {
			mWifi.setWifiEnabled(false);
		}
	}
	// toggling status
	public void switchWifi() {
		mWifi.setWifiEnabled(!isWifiOn());
	}

	public void switchGps(boolean isEnable) {
	}
	
	public void switchBluetooth(boolean isEnable) {
		if (isEnable && !isBluetoothOn()) {
			mBluetooth.enable();
		} else if (!isEnable && isBluetoothOn()) {
			mBluetooth.disable();
		}
	}
	public void switchBluetooth() {
		if (!isBluetoothOn()) {
			mBluetooth.enable();
		} else {
			mBluetooth.disable();
		}
	}

	public void switchNetwork(boolean isEnable) {
		try {
		    final Class mNetworkClass = Class.forName(mNetwork.getClass().getName());
		    final Field iConnectivityManagerField = mNetworkClass.getDeclaredField("mService");
		    iConnectivityManagerField.setAccessible(true);
		    final Object iConnectivityManager = iConnectivityManagerField.get(mNetwork);
		    final Class iConnectivityManagerClass = Class.forName(
		        iConnectivityManager.getClass().getName());
		    final Method setMobileDataEnabledMethod = iConnectivityManagerClass
		        .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		    setMobileDataEnabledMethod.setAccessible(true);
		    setMobileDataEnabledMethod.invoke(iConnectivityManager, isEnable);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void switchNetwork() {
		try {
		    final Class mNetworkClass = Class.forName(mNetwork.getClass().getName());
		    final Field iConnectivityManagerField = mNetworkClass.getDeclaredField("mService");
		    iConnectivityManagerField.setAccessible(true);
		    final Object iConnectivityManager = iConnectivityManagerField.get(mNetwork);
		    final Class iConnectivityManagerClass = Class.forName(
		        iConnectivityManager.getClass().getName());
		    final Method setMobileDataEnabledMethod = iConnectivityManagerClass
		        .getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
		    setMobileDataEnabledMethod.setAccessible(true);
		    setMobileDataEnabledMethod.invoke(iConnectivityManager, !isNetworkOn());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void switchSync(boolean isEnable) {
		ContentResolver.setMasterSyncAutomatically(isEnable);
	}
	public void switchSync() {
		ContentResolver.setMasterSyncAutomatically(!isSyncOn());
	}
	
	/*------------ functions returning device status ------------*/
	public boolean isWifiOn() {
		return mWifi.isWifiEnabled();
	}
	public boolean isBluetoothOn() {
		return mBluetooth.isEnabled();
	}
	public boolean isNetworkOn () {
		Log.d("network", "isNetworkOn function");
	      NetworkInfo lte = mNetwork.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
	      if (lte != null) {
	    	  Log.e("network", lte.toString());
	    	  return lte.isConnected();
	      } else {
	    	  return false;
	      }
	}
	public boolean isGpsOn() {
		return mGps.isProviderEnabled(LocationManager.GPS_PROVIDER);
   }
	public boolean isSyncOn() {
		return ContentResolver.getMasterSyncAutomatically();
	}		
}
