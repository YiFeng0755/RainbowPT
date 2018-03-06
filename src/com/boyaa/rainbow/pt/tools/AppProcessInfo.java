
package com.boyaa.rainbow.pt.tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * get information of processes
 * 
 *  rainbow
 */
public class AppProcessInfo {

	private static final String LOG_TAG = "Rainbow-" + AppProcessInfo.class.getSimpleName();

	private static final String PACKAGE_NAME = "com.boyaa.rainbow.pt";

	/**
	 * get information of all running processes,including package name ,process
	 * name ,icon ,pid and uid.
	 * 
	 * @param context
	 *            context of activity
	 * @return running processes list
	 */
	public List<AppInfo> getRunningApp(Context context) {
		Log.i(LOG_TAG, "get running apps");
		int api = android.os.Build.VERSION.SDK_INT; 

		ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> run = am.getRunningAppProcesses();
		List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();
		PackageManager pm = context.getPackageManager();
		List<AppInfo> progressList = new ArrayList<AppInfo>();

		for (ApplicationInfo appinfo : getPackagesInfo(context)) {
			AppInfo programe = new AppInfo();
			if (((appinfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) || ((appinfo.processName != null) && (appinfo.processName.equals(PACKAGE_NAME)))) {
				continue;
			}
			if(api<21){
				for (RunningAppProcessInfo runningProcess : run) {
					if ((runningProcess.processName != null) && runningProcess.processName.equals(appinfo.processName)) {
						programe.setPid(runningProcess.pid);
						programe.setUid(runningProcess.uid);
						break;
					}
				}
			}else{
				for (AndroidAppProcess runningProcess : processes) {
					if ((runningProcess.getPackageName() != null) && runningProcess.getPackageName().equals(appinfo.packageName)) {
						programe.setPid(runningProcess.pid);
						programe.setUid(runningProcess.uid);
						break;
					}
				}
			}
			
			programe.setPackageName(appinfo.processName);
			programe.setProcessName(appinfo.loadLabel(pm).toString());
			programe.setIcon(appinfo.loadIcon(pm));
			progressList.add(programe);
		}
		Collections.sort(progressList);
		return progressList;
	}

	/**
	 * get information of all applications.
	 * 
	 * @param context
	 *            context of activity
	 * @return packages information of all applications
	 */
	private List<ApplicationInfo> getPackagesInfo(Context context) {
		PackageManager pm = context.getApplicationContext().getPackageManager();
		List<ApplicationInfo> appList = pm.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
		return appList;
	}
	
	/**
	 * get top activity name
	 * 
	 * @param context
	 *            context of activity
	 * @return top activity name
	 */
	public static String getTopActivity(Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> runningTaskInfos = manager.getRunningTasks(1);
		if (runningTaskInfos != null)
			return (runningTaskInfos.get(0).topActivity).toString();
		else
			return null;
	}
	
}
