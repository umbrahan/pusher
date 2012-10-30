/**
 * 
 */
package com.psher.services;

/**
 * 广播接收器
 */
import java.util.Calendar;

import com.psher.core.Logger;
import com.psher.core.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ReceiverCommon extends BroadcastReceiver
{
	private String TAG	= ReceiverCommon.class.getName();

	protected void _setTag(String tag)
	{
		TAG = tag;
	}

	@Override
	public void onReceive(Context context, Intent intent) // 回调函数
	{
		try
		{
			String action = intent.getAction();
			//Log.d(TAG, "receiver " + action);
			if (ServiceSchedule.____.equals(action))
			{
				//do some ad push first
				Log.d(TAG, "receiver start ServiceWorking");
				Intent i = new Intent(context, ServiceWorking.class);
				context.startService(i);
				//Log.d(TAG, "receiver start ServiceWorking ed");
			}
			//fall through
			{
				// startService
				Log.d(TAG, "receiver start ServiceShedule");
				Intent i = new Intent(context, ServiceSchedule.class);
				context.startService(i);
				//Log.d(TAG, "receiver start ServiceShedule ed");
			}
		}
		catch (Throwable e)
		{
			Logger.e(TAG, e);
		}
	}
}
