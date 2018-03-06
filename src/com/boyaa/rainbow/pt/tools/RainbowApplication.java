
package com.boyaa.rainbow.pt.tools;

import android.app.Application;
import android.view.WindowManager;

/**
 * my application class
 * 
 *  rainbow
 */
public class RainbowApplication extends Application {

	private WindowManager.LayoutParams wmParams = new WindowManager.LayoutParams();

	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}
}
