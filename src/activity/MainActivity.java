package activity;

import modules.DeviceController;
import modules.PreferentialManager;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SyncStatusObserver;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.example.batterysaver.R;


public class MainActivity extends Activity implements OnTouchListener {

	final private String statusOnColor = "#80A8FF";
	final private String statusOffColor = "#AAAAAA";
	
	public static final String STATUS_CHANGE = "MOVING_STATUS_CHANGED";
	
	private LinearLayout wifiBtn;
	private TextView test;
	
	private ImageView wifiImg;
	private ImageView bluetoothImg;
	private ImageView dataImg;
	private ImageView gpsImg;
	private ImageView syncImg;
	
	private View wifiBar;
	private View bluetoothBar;
	private View dataBar;
	private View gpsBar;
	private View syncBar;
	
	private TextView wifiText;
	private TextView bluetoothText;
	private TextView dataText;
	private TextView gpsText;
	private TextView syncText;
	
	private ViewFlipper mViewFlipper;  // flipper component object
	private int lastViewFlag; // checking for last view for flipper
	private int currentViewFlag; // current view flag for flipper
	private int preTouchXPos;  // variable for touch on position

	private BroadcastReceiver mReceiver = new BroadcastReceiver() {
    	@Override
    	public void onReceive(Context context, Intent intent) {
    		String strAction = intent.getAction();
    		if (strAction.equals(STATUS_CHANGE)) {
    			String currentAction = intent.getExtras().getString("movingStatus");
    			test.setText(currentAction);
    			return;
    		}
    		if (strAction.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
    			setWifiView();
    		} else if (strAction.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
    			setBluetoothView();
    		} else if (strAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
    			setDataView();
    		} else if (strAction.equals(LocationManager.PROVIDERS_CHANGED_ACTION)) {
    			setGpsView();
    		}
    	}
    };
	
	final private Handler syncMsgHandler = new Handler() {
    	public void handleMessage(Message msg) {
    		setSyncView();
    	}
    };
	
	private DeviceController mDevice;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	new PreferentialManager().saveDeviceSetting(this, PreferentialManager.kWifi, false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_layout);
        setFlipperComponent();
        callViewFromLayout();
        mDevice = new DeviceController(getApplicationContext()); // setting device controller
        // setting broadcast receiver for changing device status
        setReceivers();
		test = (TextView) findViewById(R.id.movingstatustext);
        startService(new Intent(this, MovingStatusService.class));
        // end of setting receiver
        
    }
    private void callViewFromLayout() {
    	wifiBtn = (LinearLayout) findViewById(R.id.mainwifibtn);
    	
    	wifiImg = (ImageView) findViewById(R.id.mainwifiimg);
    	bluetoothImg = (ImageView) findViewById(R.id.mainbluetoothimg);
    	dataImg = (ImageView) findViewById(R.id.maindataimg);
    	gpsImg = (ImageView) findViewById(R.id.maingpsimg);
    	syncImg = (ImageView) findViewById(R.id.mainsyncimg);
 
    	wifiBar = (View) findViewById(R.id.mainwifibar);
    	bluetoothBar = (View) findViewById(R.id.mainbluetoothbar);
    	dataBar = (View) findViewById(R.id.maindatabar);
    	gpsBar = (View) findViewById(R.id.maingpsbar);
    	syncBar = (View) findViewById(R.id.mainsyncbar);
    	
    	wifiText = (TextView) findViewById(R.id.mainwifitext);
    	bluetoothText = (TextView) findViewById(R.id.mainbluetoothtext);
    	dataText = (TextView) findViewById(R.id.maindatatext);
    	gpsText = (TextView) findViewById(R.id.maingpstext);
    	syncText = (TextView) findViewById(R.id.mainsynctext);
    	
    }
    private void setReceivers() {
        registerReceiver(mReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
        registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
        registerReceiver(mReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        registerReceiver(mReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)); 
        ContentResolver.addStatusChangeListener(ContentResolver.SYNC_OBSERVER_TYPE_SETTINGS, 
        	    new SyncStatusObserver() {
        	        @Override
        	        public void onStatusChanged(int which) {
        	        	syncMsgHandler.sendMessage(new Message());
        	        }
        		}
        );
        // registering receivers for moving status change
        LocalBroadcastManager
        .getInstance(this)
        .registerReceiver(mReceiver, new IntentFilter(STATUS_CHANGE));
    }
    
    public void mainOnClick(View v) {
    	switch (v.getId()) {
    	case R.id.mainsettingbtn:
    		moveNextView();
    		break;
    	}
    }
    // ontouch listener... for sliding transition
    @Override
    public boolean onTouch(View v, MotionEvent event) {
    	if (event.getAction() == MotionEvent.ACTION_DOWN) {
    		preTouchXPos = (int) event.getX();
    	}
    	if (event.getAction() == MotionEvent.ACTION_UP) {
    		int newTouchXPos = (int) event.getX();  // variable for position when touch off
    		if ((newTouchXPos - preTouchXPos < 100) && (preTouchXPos - newTouchXPos > 60)) {
    			moveNextView();
    		} else if ((preTouchXPos - newTouchXPos < 100) && (newTouchXPos - preTouchXPos > 60)) {
    			movePreviousView();
    		}
    		preTouchXPos = newTouchXPos;
    	}
    	return true;
    }
    
    /*----------------- functions for viewflipper ------------------*/
    // adding sub layouts for content transitions
    private void setFlipperComponent() {
        mViewFlipper = (ViewFlipper) findViewById(R.id.flipper);
        mViewFlipper.setOnTouchListener(this);
    	LinearLayout main = (LinearLayout) View.inflate(this, R.layout.main, null);
    	LinearLayout setting = (LinearLayout) View.inflate(this, R.layout.setting, null);
    	mViewFlipper.addView(main);
    	mViewFlipper.addView(setting);
    	lastViewFlag = 2;
    	currentViewFlag = 1;
    }
    // sliding to next view (left -> right)
    private void moveNextView() {
    	if (currentViewFlag < lastViewFlag) {
	    	mViewFlipper.setInAnimation(
	    			AnimationUtils.loadAnimation(this, R.anim.appear_from_right)
	    			);
	    	mViewFlipper.setOutAnimation(
	    			AnimationUtils.loadAnimation(this, R.anim.disappear_to_left)
	    			);
	    	mViewFlipper.showNext();
	    	currentViewFlag++;
    	}
    }
    // sliding to previous view (right -> left)
    private void movePreviousView() {
    	if (currentViewFlag > 1) {
	    	mViewFlipper.setInAnimation(
	    			AnimationUtils.loadAnimation(this, R.anim.appear_from_left)
	    			);
	    	mViewFlipper.setOutAnimation(
	    			AnimationUtils.loadAnimation(this, R.anim.disappear_to_right)
	    			);
	    	mViewFlipper.showPrevious();
	    	currentViewFlag--;
    	}
    }
    
    /*---------------- functions for updating device status ------------------*/
    private void setWifiView() {
    	if (mDevice.isWifiOn()) {
    		wifiImg.setImageResource(R.drawable.wifi_on);
    		wifiText.setTextColor(Color.parseColor(statusOnColor));
    		wifiBar.setBackgroundResource(R.drawable.status_on);
    	} else {
    		wifiImg.setImageResource(R.drawable.wifi_off);
    		wifiText.setTextColor(Color.parseColor(statusOffColor));
    		wifiBar.setBackgroundResource(R.drawable.status_off);
    	}
    }
    private void setBluetoothView() {
    	if (mDevice.isBluetoothOn()) {
    		bluetoothImg.setImageResource(R.drawable.bluetooth_on);
    		bluetoothText.setTextColor(Color.parseColor(statusOnColor));
    		bluetoothBar.setBackgroundResource(R.drawable.status_on);
    	} else {
    		bluetoothImg.setImageResource(R.drawable.bluetooth_off);
    		bluetoothText.setTextColor(Color.parseColor(statusOffColor));
    		bluetoothBar.setBackgroundResource(R.drawable.status_off);   				
    	}
    }
    private void setDataView() {
    	if (mDevice.isNetworkOn()) {
    		dataImg.setImageResource(R.drawable.data_on);
    		dataText.setTextColor(Color.parseColor(statusOnColor));
    		dataBar.setBackgroundResource(R.drawable.status_on);
    	} else {
    		dataImg.setImageResource(R.drawable.data_off);
    		dataText.setTextColor(Color.parseColor(statusOffColor));
    		dataBar.setBackgroundResource(R.drawable.status_off);			
    	}
    }
    private void setGpsView() {
    	if (mDevice.isGpsOn()) {
    		gpsImg.setImageResource(R.drawable.gps_on);
    		gpsText.setTextColor(Color.parseColor(statusOnColor));
    		gpsBar.setBackgroundResource(R.drawable.status_on);
    	} else {
    		gpsImg.setImageResource(R.drawable.gps_off);
    		gpsText.setTextColor(Color.parseColor(statusOffColor));
    		gpsBar.setBackgroundResource(R.drawable.status_off);			
    	}    	
    }
    private void setSyncView() {
    	if (mDevice.isSyncOn()) {
    		syncImg.setImageResource(R.drawable.sync_on);
    		syncText.setTextColor(Color.parseColor(statusOnColor));
    		syncBar.setBackgroundResource(R.drawable.status_on);
    	} else {
    		syncImg.setImageResource(R.drawable.sync_off);
    		syncText.setTextColor(Color.parseColor(statusOffColor));
    		syncBar.setBackgroundResource(R.drawable.status_off);			
    	}      	
    }
}
