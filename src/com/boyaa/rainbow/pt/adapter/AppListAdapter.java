package com.boyaa.rainbow.pt.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.boyaa.rainbow.pt.R;
import com.boyaa.rainbow.pt.tools.AppInfo;
import com.boyaa.rainbow.pt.tools.AppProcessInfo;

/**
 * customizing adapter.
 * 
 *  rainbow
 */
public class AppListAdapter extends BaseAdapter {
	
	public List<AppInfo> appList;
	public AppInfo checkedProg;
	public int lastCheckedPosition = -1;
	public Context mContext = null;
	
	public AppListAdapter(Context mContext, AppProcessInfo appProcessInfo) {
		this.mContext = mContext;
		appList = appProcessInfo.getRunningApp(mContext);
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
			convertView = ((Activity)mContext).getLayoutInflater().inflate(R.layout.list_item, parent, false);		
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
					RadioButton tempButton = (RadioButton)(((Activity) mContext).findViewById(lastCheckedPosition));
					if ((tempButton != null) && (lastCheckedPosition != checkedPosition)) {
						tempButton.setChecked(false);
					}
				}
				checkedProg = appList.get(checkedPosition);
				lastCheckedPosition = checkedPosition;
			}
		}
	};
	
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

}


