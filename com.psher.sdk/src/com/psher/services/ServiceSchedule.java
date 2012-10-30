package com.psher.services;

import java.util.Calendar;

import com.psher.core.Logger;
import com.psher.core.Settings;
import com.psher.core.Utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Messenger;

public class ServiceSchedule extends Service
{
	private static final String TAG = ServiceSchedule.class.getName();

	//this is action for alarm 
	public static final String ____ = "com.google.services.palm";
	
	private static Messenger	_messenger	= null;
	private Messenger _getMessenger()
	{
		if (null == _messenger)
		{
			ServiceHandler si = new ServiceHandler(this);
			_messenger = new Messenger(si);
		}
		return _messenger;
	}
	private void _safeCheck()
	{
		_getMessenger();
	}

	@Override
	public void onCreate()
	{
		//Logger.d(TAG, "onCrate");
		_safeCheck();
		//Logger.d(TAG, "onCrated");
	}

	private void _scheduleAlarm()
	{
		//Logger.d(TAG, "_scheduleAlarm");
		String _lastScheduleKey = "pls";
		
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());

		String pls = Settings.get(this, _lastScheduleKey);
		long lpls = Utils.parseLong(pls);
		if (lpls > c.getTimeInMillis())
		{
			c.setTimeInMillis(lpls);
		}

		int ho = c.get(Calendar.HOUR_OF_DAY);
		int mi = c.get(Calendar.MINUTE);
		int se = c.get(Calendar.SECOND);

		int sche_base_secs = 2 * 60; 
		int sche_secs = sche_base_secs + Utils.RandIn(10, 50);
		
		int dh = sche_secs / 60 / 60; sche_secs -= dh * 60 * 60;
		int dm = sche_secs / 60; sche_secs -= dm * 60;
		int ds = sche_secs % 60;

		ho += dh;
		mi += dm;
		se += ds;
		c.set(Calendar.HOUR_OF_DAY, ho);
		c.set(Calendar.MINUTE, mi);
		c.set(Calendar.SECOND, se);
		
		Logger.d(TAG, String.format("schedule %d:%d:%d", ho, mi, se));
		
		Settings.set(this, _lastScheduleKey, Long.toString(c.getTimeInMillis()));
		
		//设置消息的响应
		Intent ia = new Intent(this, ReceiverCommon.class);
		ia.setAction(____);
		PendingIntent pii = PendingIntent.getBroadcast(this, 0, ia, 0);
		AlarmManager am = (AlarmManager)this.getSystemService(Service.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pii);
	}

	@Override
	public void onStart(Intent intent, int startId)
	{
		super.onStart(intent, startId);
		
		if (Utils.getAPILevel() >= 7)
		{
			// onStartCommand will be called
		}
		else
		{
			_safeCheck();
			_scheduleAlarm();
		}
		return;
	}
	
	// @Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		int ret = super.onStartCommand(intent, flags, startId);
		
		//Logger.d(TAG, "onStartCommand(flags=" + flags + ", startId=" + startId);

		_safeCheck();
		_scheduleAlarm();
		
		return ret;
		//return START_REDELIVER_INTENT;
		//return 1; /* START_STICKY *//* for building API Level 4 package */
	}

	@Override
	public IBinder onBind(Intent intent)
	{
		_safeCheck();
		return _getMessenger().getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent)
	{
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
	}
}
