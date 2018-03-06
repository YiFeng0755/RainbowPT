package com.boyaa.rainbow.pt.activity;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.*;
import android.widget.CompoundButton.OnCheckedChangeListener;

//import com.boyaa.rainbow.pt.adapter.AppListAdapter;
import com.boyaa.rainbow.pt.service.RainbowPTService;
import com.boyaa.rainbow.pt.tools.AppProcessInfo;
import com.boyaa.rainbow.pt.tools.AppInfo;
import com.boyaa.rainbow.pt.R;


/**
 * Main 
 * 
 *  rainbow
 */
public class MainActivity extends Activity {

	private static final String LOG_TAG = "Rainbow-" + MainActivity.class.getSimpleName();

	private static final int TIMEOUT = 20000;

	private List<AppInfo> appList;
	private AppProcessInfo appProcessInfo;
	private Intent monitorService;
	private ListView lstViProgramme;
	private Button btnTest;
	private int pid, uid;
	private boolean isServiceStop = false;
	private UpdateReceiver receiver;

	private TextView nbTitle;
	private ImageView ivGoBack;
	private ImageView ivBtnSet;
	private LinearLayout layBtnSet;
	private Long mExitTime = (long) 0;
	public Context mContext;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(LOG_TAG, "MainActivity---onCreate");
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initTitleLayout();
		mContext = this;
		appProcessInfo = new AppProcessInfo();
		btnTest.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				monitorService = new Intent();
				monitorService.setClass(MainActivity.this, RainbowPTService.class);
				if (getString(R.string.start_test).equals(btnTest.getText().toString())) {
					//AppListAdapter adapter = (AppListAdapter) lstViProgramme.getAdapter();
					ListAdapter adapter = (ListAdapter) lstViProgramme.getAdapter();
					if (adapter.checkedProg != null) {
						String packageName = adapter.checkedProg.getPackageName();
						String processName = adapter.checkedProg.getProcessName();
						Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);
						String startActivity = "";
						Log.d(LOG_TAG, packageName);
						// clear logcat
						try {
							Runtime.getRuntime().exec("logcat -c");
						} catch (IOException e) {
							Log.d(LOG_TAG, e.getMessage());
						}
						try {
							startActivity = intent.resolveActivity(getPackageManager()).getShortClassName();
							startActivity(intent);
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, getString(R.string.can_not_start_app_toast), Toast.LENGTH_LONG).show();
							return;
						}
						waitForAppStart(packageName);
						monitorService.putExtra("processName", processName);
						monitorService.putExtra("pid", pid);
						monitorService.putExtra("uid", uid);
						monitorService.putExtra("packageName", packageName);
						monitorService.putExtra("startActivity", startActivity);
						startService(monitorService);
						isServiceStop = false;
						btnTest.setText(getString(R.string.stop_test));
					} else {
						Toast.makeText(MainActivity.this, getString(R.string.choose_app_toast), Toast.LENGTH_LONG).show();
					}
				} else {
					btnTest.setText(getString(R.string.start_test));
					/*Toast.makeText(MainActivity.this, getString(R.string.test_result_file_toast) + RainbowPTService.resultFilePath,
							Toast.LENGTH_LONG).show();*/
					Toast.makeText(MainActivity.this, getString(R.string.test_result_file_toast) + RainbowPTService.resultFilePath,
							Toast.LENGTH_LONG).show();
					stopService(monitorService);
				}
			}
		});
		//lstViProgramme.setAdapter(new AppListAdapter(this, appProcessInfo));
		lstViProgramme.setAdapter(new ListAdapter());
		lstViProgramme.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				RadioButton rdBtn = (RadioButton) ((LinearLayout) view).getChildAt(0);
				rdBtn.setChecked(true);
			}
		});

		nbTitle.setText(getString(R.string.app_name));
		ivGoBack.setVisibility(ImageView.INVISIBLE);
		ivBtnSet.setImageResource(R.drawable.settings_button);
		layBtnSet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				goToSettingsActivity();
			}
		});
		receiver = new UpdateReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(RainbowPTService.SERVICE_ACTION);
		registerReceiver(receiver, filter);
	}

	private void initTitleLayout() {
		ivGoBack = (ImageView) findViewById(R.id.go_back);
		nbTitle = (TextView) findViewById(R.id.nb_title);
		ivBtnSet = (ImageView) findViewById(R.id.btn_set);
		lstViProgramme = (ListView) findViewById(R.id.processList);
		btnTest = (Button) findViewById(R.id.test);
		layBtnSet = (LinearLayout) findViewById(R.id.lay_btn_set);
	}

	/**
	 * customized BroadcastReceiver
	 * 
	 *  rainbow
	 */
	public class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceStop = intent.getExtras().getBoolean("isServiceStop");
			if (isServiceStop) {
				btnTest.setText(getString(R.string.start_test));
			}
		}
	}

	@Override
	protected void onStart() {
		Log.d(LOG_TAG, "onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(LOG_TAG, "onResume");
		if (isServiceStop) {
			btnTest.setText(getString(R.string.start_test));
		}
	}

	/**
	 * wait for test application started.
	 * 
	 * @param packageName
	 *            package name of test application
	 */
	private void waitForAppStart(String packageName) {
		Log.d(LOG_TAG, "wait for app start");
		boolean isProcessStarted = false;
		long startTime = System.currentTimeMillis();
		while (System.currentTimeMillis() < startTime + TIMEOUT) {
			appList = appProcessInfo.getRunningApp(getBaseContext());
			for (AppInfo programe : appList) {
				if ((programe.getPackageName() != null) && (programe.getPackageName().equals(packageName))) {
					pid = programe.getPid();
					Log.d(LOG_TAG, "pid:" + pid);
					uid = programe.getUid();
					if (pid != 0) {
						isProcessStarted = true;						
						break;
					}
				}				
			}
			if (isProcessStarted) {				
				break;
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * show a dialog when click return key.
	 * 
	 * @return Return true to prevent this event from being propagated further,
	 *         or false to indicate that you have not handled this event and it
	 *         should continue to be propagated.
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, R.string.quite_alert, Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			} else {
				if (monitorService != null) {
					Log.d(LOG_TAG, "stop service");
					stopService(monitorService);
				}
				Log.d(LOG_TAG, "exit Rainbow");
				finish();
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void goToSettingsActivity() {
		Intent intent = new Intent();
		intent.setClass(MainActivity.this, SettingsActivity.class);
		startActivityForResult(intent, Activity.RESULT_FIRST_USER);
	}
	
	/**
	 * 
	
	 */
	private class ListAdapter extends BaseAdapter {
		List<AppInfo> appList;
		AppInfo checkedProg;
		int lastCheckedPosition = -1;

		public ListAdapter() {
			appList = appProcessInfo.getRunningApp(getBaseContext());
		}

		@Override
		public int getCount() {
			return appList.size();
		}

		@Override
		public Object getItem(int position) {
			return appList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AppInfo pr = (AppInfo) appList.get(position);
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.list_item, parent, false);
			Viewholder holder = (Viewholder) convertView.getTag();
			if (holder == null) {
				holder = new Viewholder();
				convertView.setTag(holder);
				holder.imgViAppIcon = (ImageView) convertView.findViewById(R.id.image);
				holder.txtAppName = (TextView) convertView.findViewById(R.id.text);
				holder.rdoBtnApp = (RadioButton) convertView.findViewById(R.id.rb);
				holder.rdoBtnApp.setFocusable(false);
				holder.rdoBtnApp.setOnCheckedChangeListener(checkedChangeListener);
			}
			holder.imgViAppIcon.setImageDrawable(pr.getIcon());
			holder.txtAppName.setText(pr.getProcessName());
			holder.rdoBtnApp.setId(position);
			holder.rdoBtnApp.setChecked(checkedProg != null && getItem(position) == checkedProg);
			return convertView;
		}

		OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					final int checkedPosition = buttonView.getId();
					if (lastCheckedPosition != -1) {
						RadioButton tempButton = (RadioButton) findViewById(lastCheckedPosition);
						if ((tempButton != null) && (lastCheckedPosition != checkedPosition)) {
							tempButton.setChecked(false);
						}
					}
					checkedProg = appList.get(checkedPosition);
					lastCheckedPosition = checkedPosition;
				}
			}
		};
	}
	
	/**
	 * save status of all installed processes
	 * 
	 *  rainbow
	 */
	public static class Viewholder {
		public TextView txtAppName;
		public ImageView imgViAppIcon;
		public RadioButton rdoBtnApp;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	
}

