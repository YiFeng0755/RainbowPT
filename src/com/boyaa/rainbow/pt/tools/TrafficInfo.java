
package com.boyaa.rainbow.pt.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Map;

import android.net.TrafficStats;
import android.util.Log;

/**
 * information of network traffic
 * 
 *  rainbow
 */
public class TrafficInfo {

	private static final String LOG_TAG = "Rainbow-" + TrafficInfo.class.getSimpleName();

	private String uid;
	//Added by Jack
	long lastTotalTraffic = 0;
	long sendTraffic = 0;
	long receiveTraffic = 0;
	

	public TrafficInfo(String uid) {
		this.uid = uid;
	}

	/**
	 * get total network traffic, which is the sum of upload and download
	 * traffic.
	 * 
	 * @return total traffic include received and send traffic
	 */
	public long getTrafficInfo() {
		Log.i(LOG_TAG, "get traffic information");
		Log.d(LOG_TAG, "uid===" + uid);
		RandomAccessFile rafRcv = null, rafSnd = null;
		String rcvPath = "/proc/uid_stat/" + uid + "/tcp_rcv";
		String sndPath = "/proc/uid_stat/" + uid + "/tcp_snd";
		long rcvTraffic = -1;
		long sndTraffic = -1;
		try {
			rafRcv = new RandomAccessFile(rcvPath, "r");
			rafSnd = new RandomAccessFile(sndPath, "r");
			rcvTraffic = Long.parseLong(rafRcv.readLine());
			sndTraffic = Long.parseLong(rafSnd.readLine());
		} catch (FileNotFoundException e) {
			rcvTraffic = -1;
			sndTraffic = -1;
		} catch (NumberFormatException e) {
			Log.e(LOG_TAG, "NumberFormatException: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			Log.e(LOG_TAG, "IOException: " + e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				if (rafRcv != null) {
					rafRcv.close();
				}
				if (rafSnd != null)
					rafSnd.close();
			} catch (IOException e) {
				Log.i(LOG_TAG, "close randomAccessFile exception: " + e.getMessage());
			}
		}
		Log.d(LOG_TAG, "rcvTraffic===" + rcvTraffic);
		Log.d(LOG_TAG, "sndTraffic===" + sndTraffic);
		if (rcvTraffic == -1 || sndTraffic == -1) {
			return -1;
		} else
			lastTotalTraffic = (rcvTraffic + sndTraffic);
			return lastTotalTraffic;
	}
	
	
	
	/**
	 * get total network traffic by TrafficStats class, which is the sum of upload and download
	 * traffic.
	 * 
	 * @return total traffic include received and send traffic
	 */
	public long getTrafficInfoByTrafficStats() {
		Log.i(LOG_TAG, "get traffic information");
		Log.d(LOG_TAG, "uid===" + uid);	
		long rcvTraffic = -1;
		long sndTraffic = -1;
		try {
			sndTraffic = TrafficStats.getUidTxBytes(Integer.parseInt(uid));//发送的 上传的流量byte  
			rcvTraffic = TrafficStats.getUidRxBytes(Integer.parseInt(uid));//下载的流量 byte 		
		} catch (Exception e) {			
			rcvTraffic = -1;
			sndTraffic = -1;
		}
		Log.d(LOG_TAG, "rcvTraffic===" + rcvTraffic);
		Log.d(LOG_TAG, "sndTraffic===" + sndTraffic);
		if (rcvTraffic == -1 || sndTraffic == -1) {
			return -1;
		} else
			lastTotalTraffic = (rcvTraffic + sndTraffic);
		return lastTotalTraffic;
	}
	
	/**
	 * @author JackWang
	 * get total network traffic by TrafficStats class, which is the sum of upload and download
	 * traffic.
	 * 
	 * @return total traffic include received and send traffic
	 */
	public Long[] getSndAndRcv() {
		Log.i(LOG_TAG, "get traffic information");
		Log.d(LOG_TAG, "uid===" + uid);
		Long trafficArray[] = new Long[3];
		long rcvTraffic = -1;
		long sndTraffic = -1;
		try {
			sndTraffic = TrafficStats.getUidTxBytes(Integer.parseInt(uid));//发送的 上传的流量byte  
			rcvTraffic = TrafficStats.getUidRxBytes(Integer.parseInt(uid));//下载的流量 byte 		
		} catch (Exception e) {			
			rcvTraffic = -1;
			sndTraffic = -1;
		}
		Log.d(LOG_TAG, "rcvTraffic===" + rcvTraffic);
		Log.d(LOG_TAG, "sndTraffic===" + sndTraffic);
		if (rcvTraffic == -1 || sndTraffic == -1) {		
			trafficArray[2] = -1l;			
		} else
			trafficArray[2] = rcvTraffic + sndTraffic;			
		trafficArray[0] = sndTraffic;
		trafficArray[1] = rcvTraffic;
		return trafficArray;
	}
	
}