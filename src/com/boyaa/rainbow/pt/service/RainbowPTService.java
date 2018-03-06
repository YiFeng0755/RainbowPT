package com.boyaa.rainbow.pt.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.boyaa.rainbow.pt.activity.MainActivity;
import com.boyaa.rainbow.pt.tools.CSVToExcel;
import com.boyaa.rainbow.pt.tools.ChartView;
import com.boyaa.rainbow.pt.tools.CpuInfo;
import com.boyaa.rainbow.pt.tools.CreateLineChart;
import com.boyaa.rainbow.pt.tools.CurrentInfo;
import com.boyaa.rainbow.pt.tools.EncryptData;
import com.boyaa.rainbow.pt.tools.ExcelParserConstants;
import com.boyaa.rainbow.pt.tools.FpsInfo;
import com.boyaa.rainbow.pt.tools.MailSender;
import com.boyaa.rainbow.pt.tools.MemoryInfo;
import com.boyaa.rainbow.pt.tools.RainbowApplication;
import com.boyaa.rainbow.pt.tools.Settings;
import com.boyaa.rainbow.pt.R;

/**
 * Service running in background
 * 
 *  rainbow
 */
public class RainbowPTService extends Service {

	private final static String LOG_TAG = "Rainbow-" + RainbowPTService.class.getSimpleName();

	private static final String BLANK_STRING = "";
	
	private WindowManager windowManager = null;
	private WindowManager.LayoutParams wmParams = null;
	private View viFloatingWindow;
	private float mTouchStartX;
	private float mTouchStartY;
	private float x;
	private float y;
	private TextView txtTotalMem;
	private TextView txtUnusedMem;
	private TextView txtTraffic;
	private TextView txtfps;
	private Button btnStop;
	private Button btnWifi;
	private int delaytime;
	private DecimalFormat fomart;
	private MemoryInfo memoryInfo;
	private WifiManager wifiManager;
	private Handler handler = new Handler();
	private CpuInfo cpuInfo;
	private FpsInfo fpsInfo;
	private boolean isRoot;
	private boolean isFloating;
	private String processName, packageName, startActivity;
	private int pid, uid;
	private boolean isServiceStop = false;
	private String sender, password, recipients, smtp;
	private String[] receivers;
	private EncryptData des;

	public static BufferedWriter bw;
	public static FileOutputStream out;
	public static OutputStreamWriter osw;
	public static String resultDir;
	public static String resultFileName;
	public static String resultFilePath;
	public static boolean isStop = false;

	private String totalBatt;
	private String temperature;
	private String voltage;
	private CurrentInfo currentInfo;
	private BatteryInfoBroadcastReceiver batteryBroadcast = null;

	// get start time
	private static final int MAX_START_TIME_COUNT = 5;
	private static final String START_TIME = "#startTime";
	private int getStartTimeCount = 0;
	private boolean isGetStartTime = true;
	private String startTime = "";
	public static final String SERVICE_ACTION = "com.boyaa.rainbow.pt.action.rainbowPTService";

	@Override
	public void onCreate() {
		Log.i(LOG_TAG, "service onCreate");		
		super.onCreate();
		isServiceStop = false;
		isStop = false;
		isRoot = upgradeRootPermission(getPackageCodePath());
		memoryInfo = new MemoryInfo();
		fomart = new DecimalFormat();
		fomart.setMaximumFractionDigits(2);
		fomart.setMinimumFractionDigits(0);
		des = new EncryptData("rainbow");
		fpsInfo = new FpsInfo();
		currentInfo = new CurrentInfo();
		batteryBroadcast = new BatteryInfoBroadcastReceiver();
		registerReceiver(batteryBroadcast, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
		//Modify by Jack
		emailConfig();//初始化Email SMTP服务配置		
	}

	/**
	 * 电池信息监控监听器
	 * 
	 *  rainbow
	 * 
	 */
	public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
				int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
				int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
				totalBatt = String.valueOf(level * 100 / scale);
				voltage = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) * 1.0 / 1000);
				temperature = String.valueOf(intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) * 1.0 / 10);
			}

		}

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(LOG_TAG, "service onStart");
		PendingIntent contentIntent = PendingIntent.getActivity(getBaseContext(), 0, new Intent(this, MainActivity.class), 0);
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
		builder.setContentIntent(contentIntent).setSmallIcon(R.drawable.icon).setWhen(System.currentTimeMillis()).setAutoCancel(true)
				.setContentTitle("Rainbow");
		startForeground(startId, builder.build());

		pid = intent.getExtras().getInt("pid");
		uid = intent.getExtras().getInt("uid");
		processName = intent.getExtras().getString("processName");
		packageName = intent.getExtras().getString("packageName");
		startActivity = intent.getExtras().getString("startActivity");

		cpuInfo = new CpuInfo(getBaseContext(), pid, Integer.toString(uid));
		readSettingInfo();
		if (isFloating) {
			viFloatingWindow = LayoutInflater.from(this).inflate(R.layout.floating, null);
			txtUnusedMem = (TextView) viFloatingWindow.findViewById(R.id.memunused);
			txtTotalMem = (TextView) viFloatingWindow.findViewById(R.id.memtotal);
			txtTraffic = (TextView) viFloatingWindow.findViewById(R.id.traffic);
			txtfps = (TextView) viFloatingWindow.findViewById(R.id.fps);
			btnWifi = (Button) viFloatingWindow.findViewById(R.id.wifi);

			wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			if (wifiManager.isWifiEnabled()) {
				btnWifi.setText(R.string.close_wifi);
			} else {
				btnWifi.setText(R.string.open_wifi);
			}
			txtUnusedMem.setText(getString(R.string.calculating));
			txtUnusedMem.setTextColor(android.graphics.Color.RED);
			txtTotalMem.setTextColor(android.graphics.Color.RED);
			txtTraffic.setTextColor(android.graphics.Color.RED);
			if(!isRoot){
				txtfps.setVisibility(View.GONE);
			}else{
				txtfps.setTextColor(android.graphics.Color.RED);
			}
			btnStop = (Button) viFloatingWindow.findViewById(R.id.stop);
			btnStop.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent();
					intent.putExtra("isServiceStop", true);
					intent.setAction(SERVICE_ACTION);
					sendBroadcast(intent);
					stopSelf();
				}
			});
			createFloatingWindow();
		}
		createResultCsv();
		handler.postDelayed(task, 1000);
		return START_NOT_STICKY;
	}

	/**
	 * read configuration file.
	 * 
	 * @throws IOException
	 */
	private void readSettingInfo() {
		SharedPreferences preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		int interval = preferences.getInt(Settings.KEY_INTERVAL, 5);
		delaytime = interval * 1000;
		isFloating = preferences.getBoolean(Settings.KEY_ISFLOAT, true);
		sender = preferences.getString(Settings.KEY_SENDER, BLANK_STRING);
		password = preferences.getString(Settings.KEY_PASSWORD, BLANK_STRING);
		recipients = preferences.getString(Settings.KEY_RECIPIENTS, BLANK_STRING);
		receivers = recipients.split("\\s+");
		smtp = preferences.getString(Settings.KEY_SMTP, BLANK_STRING);
	}

	/**
	 * write the test result to csv format report.
	 */
	private void createResultCsv() {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
		String mDateTime;
		if ((Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk")))
			mDateTime = formatter.format(cal.getTime().getTime() + 8 * 60 * 60 * 1000);
		else
			mDateTime = formatter.format(cal.getTime().getTime());
		resultFileName = "RainbowPTRes_" + mDateTime + ".csv";
		//Modify By jack
		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
			// 在4.0以下的低版本上/sdcard连接至/mnt/sdcard，而4.0以上版本则连接至/storage/sdcard0，所以有外接sdcard，/sdcard路径一定存在			
			resultDir = "/sdcard/RPT";			
						// resultFilePath =
			// android.os.Environment.getExternalStorageDirectory() +
			// File.separator + "RainbowRes_" + mDateTime + ".csv";
		} else {
			resultDir = getBaseContext().getFilesDir().getPath() + File.separator + "RPT";
		}		
		resultFilePath = resultDir + File.separator + resultFileName;		
		try {
			File resultFile = new File(resultFilePath);
			File resultFileParent = resultFile.getParentFile(); 
			if(resultFileParent != null && !resultFileParent.exists()){ 
				resultFileParent.mkdirs(); 
			} 
			resultFile.createNewFile();
			out = new FileOutputStream(resultFile);
			osw = new OutputStreamWriter(out, "UTF-8");
			bw = new BufferedWriter(osw);
			long totalMemorySize = memoryInfo.getTotalMemory();
			//String totalMemory = fomart.format((double) totalMemorySize / 1024);
			//Modify by Jack 
			//因为数字分组中有逗号所以改为如下的方法进行format
			DecimalFormat df = new DecimalFormat("####.00"); 
			String totalMemory = df.format((double) totalMemorySize / 1024);
			String multiCpuTitle = BLANK_STRING;
			// titles of multiple cpu cores
			ArrayList<String> cpuList = cpuInfo.getCpuList();
			for (int i = 0; i < cpuList.size(); i++) {
				multiCpuTitle += "," + cpuList.get(i) + getString(R.string.total_usage);
			}
			bw.write(getString(R.string.result_title) + "\r\n");
			bw.write(getString(R.string.process_package) + ": ," + packageName + "\r\n" + getString(R.string.process_name) + ": ," + processName
					+ "\r\n" + getString(R.string.process_pid) + ": ," + pid + "\r\n" + getString(R.string.mem_size) + "： ," + totalMemory + "MB\r\n"
					+ getString(R.string.cpu_type) + ": ," + cpuInfo.getCpuName() + "\r\n" + getString(R.string.android_system_version) + ": ,"
					+ memoryInfo.getSDKVersion() + "\r\n" + getString(R.string.mobile_type) + ": ," + memoryInfo.getPhoneType() + "\r\n" + "UID"
					+ ": ," + uid + "\r\n");

			if (isGrantedReadLogsPermission()) {
				bw.write(START_TIME);
			}
			//Modify by Jack
			/*bw.write(getString(R.string.timestamp) + "," + getString(R.string.used_mem_PSS) + "," + getString(R.string.used_mem_ratio) + ","
					+ getString(R.string.mobile_free_mem) + "," + getString(R.string.app_used_cpu_ratio) + ","  + getString(R.string.traffic) + ","
					+ getString(R.string.total_used_cpu_ratio) + multiCpuTitle + ","
					+ getString(R.string.battery) + "," + getString(R.string.current) + "," + getString(R.string.temperature) + ","
					+ getString(R.string.voltage) + "\r\n");*/
			
			bw.write(getString(R.string.timestamp) + "," + getString(R.string.top_activity) + "," + getString(R.string.used_mem_PSS) + "," + getString(R.string.used_mem_ratio) + ","
					+ getString(R.string.mobile_free_mem) + "," + getString(R.string.app_used_cpu_ratio) + ","  + getString(R.string.traffic) + ","
					+ getString(R.string.total_send_traffic) + "," +  getString(R.string.total_receive_traffic) + "," + getString(R.string.fps)  + ","
					+ getString(R.string.total_used_cpu_ratio) + multiCpuTitle + ","
					+ getString(R.string.battery) + "," + getString(R.string.current) + "," + getString(R.string.temperature) + ","
					+ getString(R.string.voltage) + "\r\n");
		} catch (IOException e) {
			Log.e(LOG_TAG, e.getMessage());
		}
	}

	
	/**
	 * create a floating window to show real-time data.
	 */
	private void createFloatingWindow() {
		Log.i(LOG_TAG, "createFloatingWindow");
		SharedPreferences shared = getSharedPreferences("float_flag", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = shared.edit();
		editor.putInt("float", 1);
		editor.commit();
		windowManager = (WindowManager) getApplicationContext().getSystemService("window");
		wmParams = ((RainbowApplication) getApplication()).getMywmParams();
		wmParams.type = 2002;
		wmParams.flags |= 8;
		wmParams.gravity = Gravity.LEFT | Gravity.TOP;
		wmParams.x = 0;
		wmParams.y = 0;
		wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		wmParams.format = 1;
		windowManager.addView(viFloatingWindow, wmParams);
		viFloatingWindow.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				x = event.getRawX();
				y = event.getRawY() - 25;
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mTouchStartX = event.getX();
					mTouchStartY = event.getY();
					break;
				case MotionEvent.ACTION_MOVE:
					updateViewPosition();
					break;
				case MotionEvent.ACTION_UP:
					updateViewPosition();					
					mTouchStartX = mTouchStartY = 0;
					break;
				}
				return true;
			}
		});

		btnWifi.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					btnWifi = (Button) viFloatingWindow.findViewById(R.id.wifi);
					String buttonText = (String) btnWifi.getText();
					String wifiText = getResources().getString(R.string.open_wifi);
					if (buttonText.equals(wifiText)) {
						wifiManager.setWifiEnabled(true);
						btnWifi.setText(R.string.close_wifi);
					} else {
						wifiManager.setWifiEnabled(false);
						btnWifi.setText(R.string.open_wifi);
					}
				} catch (Exception e) {
					Toast.makeText(viFloatingWindow.getContext(), getString(R.string.wifi_fail_toast), Toast.LENGTH_LONG).show();
					Log.e(LOG_TAG, e.toString());
				}
			}
		});
	}

	private Runnable task = new Runnable() {

		public void run() {
			if (!isServiceStop) {
				dataRefresh();
				handler.postDelayed(this, delaytime);
				if (isFloating) {
					windowManager.updateViewLayout(viFloatingWindow, wmParams);
				}
				// get app start time from logcat on every task running
				getStartTimeFromLogcat();
			} else {
				Intent intent = new Intent();
				intent.putExtra("isServiceStop", true);
				intent.setAction(SERVICE_ACTION);
				sendBroadcast(intent);
				stopSelf();
			}
		}
	};

	/**
	 * Try to get start time from logcat.
	 */
	private void getStartTimeFromLogcat() {
		if (!isGetStartTime || getStartTimeCount >= MAX_START_TIME_COUNT) {
			return;
		}
		try {
			// filter logcat by Tag:ActivityManager and Level:Info
			String logcatCommand = "logcat -v time -d ActivityManager:I *:S";
			Process process = Runtime.getRuntime().exec(logcatCommand);
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
			StringBuilder strBuilder = new StringBuilder();
			String line = BLANK_STRING;

			while ((line = bufferedReader.readLine()) != null) {
				strBuilder.append(line);
				strBuilder.append("\r\n");
				String regex = ".*Displayed.*" + startActivity + ".*\\+(.*)ms.*";
				if (line.matches(regex)) {
					Log.w("my logs", line);
					if (line.contains("total")) {
						line = line.substring(0, line.indexOf("total"));
					}
					startTime = line.substring(line.lastIndexOf("+") + 1, line.lastIndexOf("ms") + 2);
					Toast.makeText(RainbowPTService.this, getString(R.string.start_time) + startTime, Toast.LENGTH_LONG).show();
					isGetStartTime = false;
					break;
				}
			}
			getStartTimeCount++;
		} catch (IOException e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	/**
	 * Above JellyBean, we cannot grant READ_LOGS permission...
	 * 
	 * @return
	 */
	private boolean isGrantedReadLogsPermission() {
		int permissionState = getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, getPackageName());
		return permissionState == PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * refresh the performance data showing in floating window.
	 * 
	 * @throws FileNotFoundException
	 * 
	 * @throws IOException
	 */
	private void dataRefresh() {
		int pidMemory = memoryInfo.getPidMemorySize(pid, getBaseContext());
		long freeMemory = memoryInfo.getFreeMemorySize(getBaseContext());
		String freeMemoryKb = fomart.format((double) freeMemory / 1024);
		String processMemory = fomart.format((double) pidMemory / 1024);
		String currentBatt = String.valueOf(currentInfo.getCurrentValue());
		String fps = String.valueOf(fpsInfo.fps());
		// 异常数据过滤
		try {
			if (Math.abs(Double.parseDouble(currentBatt)) >= 500) {
				currentBatt = "N/A";
			}
		} catch (Exception e) {
			currentBatt = "N/A";
		}
		ArrayList<String> processInfo = cpuInfo.getCpuRatioInfo(totalBatt, currentBatt, temperature, voltage,fps);
		if (isFloating) {
			String processCpuRatio = "0.00";
			String totalCpuRatio = "0.00";
			String trafficSize = "0";
			long tempTraffic = 0L;
			double trafficMb = 0;
			boolean isMb = false;
			if (!processInfo.isEmpty()) {
				processCpuRatio = processInfo.get(0);
				totalCpuRatio = processInfo.get(1);
				trafficSize = processInfo.get(2);
				if (!(BLANK_STRING.equals(trafficSize)) && !("-1".equals(trafficSize))) {
					tempTraffic = Long.parseLong(trafficSize);
					if (tempTraffic > 1024) {
						isMb = true;
						trafficMb = (double) tempTraffic / 1024;
					}
				}
				//Modify by Jack
				// 如果cpu使用率存在且都不小于0，则输出
				if (processCpuRatio != null && totalCpuRatio != null) {				
					txtUnusedMem.setText(getString(R.string.process_free_mem) + processMemory + "/" + freeMemoryKb + "MB");
					txtTotalMem.setText(getString(R.string.process_overall_cpu) + processCpuRatio + "%/" + totalCpuRatio + "%");
					txtfps.setText(getString(R.string.process_fps)+fps);
					String batt = getString(R.string.current) + currentBatt;
					if(Double.parseDouble(totalCpuRatio) > 100){
						Toast.makeText(this,"总CPU异常: " +totalCpuRatio, Toast.LENGTH_LONG).show();
						Log.e(LOG_TAG, "总CPU异常: " + totalCpuRatio);
					}
					if ("-1".equals(trafficSize)) {
						txtTraffic.setText(batt + "," + getString(R.string.traffic) + "N/A");
					} else if (isMb)
						txtTraffic.setText(batt + "," + getString(R.string.traffic) + fomart.format(trafficMb) + "MB");
					else
						txtTraffic.setText(batt + "," + getString(R.string.traffic) + trafficSize + "KB");
				}
			
				// 当内存为0切cpu使用率为0时则是被测应用退出
				if ("0".equals(processMemory)) {
					closeOpenedStream();
					isServiceStop = true;
					return;
				}
			}

		}
	}

	/**
	 * update the position of floating window.
	 */
	private void updateViewPosition() {
		wmParams.x = (int) (x - mTouchStartX);
		wmParams.y = (int) (y - mTouchStartY);
		windowManager.updateViewLayout(viFloatingWindow, wmParams);
	}

	/**
	 * close all opened stream.
	 */
	public void closeOpenedStream() {
		try {
			if (bw != null) {
				bw.write("\r\n" + getString(R.string.comment2) + "\r\n" + getString(R.string.comment3) + "\r\n"
						+ getString(R.string.comment4) + "\r\n");
				bw.close();
			}
			if (osw != null)
				osw.close();
			if (out != null)
				out.close();
			//Modify by Jack
			CSVToExcel.convertToXLS(resultFilePath);//第一步先转换csv到xls
			ExcelParserConstants.initialConstants();//初始化Excel字体的单元格位置
			convertChartToImage(CSVToExcel.xlsFilePath);//第二步向xls添加图
			try{
		    //CSVToExcel.totalStatistical("/sdcard/RPT/RainbowPTRes_20150310_hlmj.xls");//用于调试
			CSVToExcel.totalStatistical(CSVToExcel.xlsFilePath);//第三步是增加相应的统计信息		
			}catch(Exception e){
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			resultFilePath = CSVToExcel.xlsFilePath;						
		} catch (Exception e) {
			Log.d(LOG_TAG, e.getMessage());			
		}
	}

	@Override
	public void onDestroy() {
		Log.i(LOG_TAG, "service onDestroy");
		if (windowManager != null)
			windowManager.removeView(viFloatingWindow);
		handler.removeCallbacks(task);
		closeOpenedStream();
		// replace the start time in file
		if (isGrantedReadLogsPermission()) {
			if (!BLANK_STRING.equals(startTime)) {
				replaceFileString(resultFilePath, START_TIME, getString(R.string.start_time) + startTime + "\r\n");
			} else {
				replaceFileString(resultFilePath, START_TIME, BLANK_STRING);
			}
		}
		isStop = true;
		unregisterReceiver(batteryBroadcast);
		boolean isSendSuccessfully = false;
		try {
			isSendSuccessfully = MailSender.sendTextMail(sender, des.decrypt(password), smtp, "Rainbow Performance Test Report", "see attachment",
					resultFilePath, receivers);
		} catch (Exception e) {
			isSendSuccessfully = false;
		}
		if (isSendSuccessfully) {
			Toast.makeText(this, getString(R.string.send_success_toast) + recipients, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, getString(R.string.send_fail_toast) + RainbowPTService.resultFilePath, Toast.LENGTH_LONG).show();			
		}
		super.onDestroy();
		stopForeground(true);
	}

	/**
	 * Replaces all matches for replaceType within this replaceString in file on
	 * the filePath
	 * 
	 * @param filePath
	 * @param replaceType
	 * @param replaceString
	 */
	private void replaceFileString(String filePath, String replaceType, String replaceString) {
		try {
			File file = new File(filePath);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line = BLANK_STRING;
			String oldtext = BLANK_STRING;
			while ((line = reader.readLine()) != null) {
				oldtext += line + "\r\n";
			}
			reader.close();
			// replace a word in a file
			String newtext = oldtext.replaceAll(replaceType, replaceString);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8"));
			writer.write(newtext);
			writer.close();
		} catch (IOException e) {
			Log.d(LOG_TAG, e.getMessage());
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	/**
	 * @author JackWang
	 * @param excelFilePath
	 */
	public void convertChartToImage(String excelFilePath){
		CreateLineChart.excelFilePath = excelFilePath;		
		CreateLineChart.makeChat();
		Bitmap cpuBitmap = null;
		Bitmap memoryBitmap = null;
		Bitmap netBitmap = null;
		Bitmap netSndRcvBitmap = null;
		Bitmap fpsBitmap = null;
		ChartView chartView = new ChartView(getBaseContext());
		cpuBitmap = chartView.drawChart(CreateLineChart.cpuChart);
		chartView = new ChartView(getBaseContext());
		memoryBitmap = chartView.drawChart(CreateLineChart.memoryChart);
		chartView = new ChartView(getBaseContext());
		netBitmap = chartView.drawChart(CreateLineChart.netChart);
		chartView = new ChartView(getBaseContext());
		netSndRcvBitmap = chartView.drawChart(CreateLineChart.netSndRcvChart);
		chartView = new ChartView(getBaseContext());
		fpsBitmap = chartView.drawChart(CreateLineChart.fpsChart);
		try {
			CreateLineChart.chartToImage("CPU", cpuBitmap);			
			CreateLineChart.chartToImage("Memory", memoryBitmap);
			CreateLineChart.chartToImage("Net", netBitmap);
			CreateLineChart.chartToImage("NetSndRcv", netSndRcvBitmap);
			CreateLineChart.chartToImage("FPS", fpsBitmap);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
   }
   
   /**
    * @author JackWang
    */
   public void  emailConfig(){
	   SharedPreferences preferences = Settings.getDefaultSharedPreferences(getApplicationContext());
		Editor editor = preferences.edit();
		editor.putString(Settings.KEY_SENDER, "noReply@boyaa.com");	
	    try {
			editor.putString(Settings.KEY_PASSWORD, des.encrypt("noReply#boyaa"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		editor.putString(Settings.KEY_SMTP, "mail.boyaa.com");
		editor.commit();
   }
   
   public static boolean upgradeRootPermission(String pkgCodePath) {
		Process process = null;
		DataOutputStream os = null;
		try {
			String cmd = "chmod 777 " + pkgCodePath;
			process = Runtime.getRuntime().exec("su"); // 切换到root帐号
			os = new DataOutputStream(process.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			int existValue = process.waitFor();
			if (existValue == 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception e) {
			Log.w(LOG_TAG, "upgradeRootPermission exception=" + e.getMessage());
			return false;
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
	}
}