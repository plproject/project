package activity;

import modules.DeviceController;
import modules.PreferentialManager;
import kr.sclab.contextaware.SclabRecognizer;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class MovingStatusService extends Service {
	
	SclabRecognizer mRecognizer;
	String[] movingStatus;
	DeviceController mDevice;
	
	@Override
	public void onCreate() {
		mRecognizer = new SclabRecognizer(this);
		mDevice = new DeviceController(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		mRecognizer.start();
		new Thread() {
			@Override
			public void run() {
				movingStatus = mRecognizer.getStatus();
				while(true) {
					try {
						sleep(1000);
					} catch(Exception e) {
						e.printStackTrace();
					}
					boolean test = new PreferentialManager().fetchDeviceSetting(MovingStatusService.this, PreferentialManager.kWifi);
					if (test) {
						Log.e("preferential", "in service");
					} else {
						Log.e("preferential", "in service false");
					}
					String[] currentMovingStatus = mRecognizer.getStatus();
					if (checkStatusChanged(currentMovingStatus)) {
						updateMovingStatus(currentMovingStatus);
						Intent status = new Intent(MainActivity.STATUS_CHANGE);
						status.putExtra("movingStatus", currentMovingStatus[1]);
						LocalBroadcastManager
						.getInstance(MovingStatusService.this)
						.sendBroadcast(status);
					}
				} 
			} 
		}.start(); 
		return START_STICKY;
	}
	@Override
	public void onDestroy() {
		mRecognizer.stop();
		super.onDestroy();
	}
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}
	
	private boolean checkStatusChanged(String[] currentStatus) {
		for (int i=0; i < 3; i++) {
			if (currentStatus[i] == "Not Yet") {
				return false;
			}
			if (movingStatus[i] != currentStatus[i]) {
				return true;
			}
		}
		return false;
	}
	private void updateMovingStatus(String[] newMovingStatus) {
		for (int i=0; i < 3; i++) {
			movingStatus[i] = newMovingStatus[i];
		}
	}
}
