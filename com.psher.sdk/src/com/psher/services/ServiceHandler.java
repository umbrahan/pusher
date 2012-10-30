package com.psher.services;


import com.psher.core.Logger;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;

public class ServiceHandler extends Handler
{
	private static final String		TAG						= ServiceHandler.class.getName();
	private static final int		RUNSTATE_RUNNING		= 0;
	private static final int		RUNSTATE_NEEDSETTINGS	= 1;
	private int						_nRunningState;
	private Context					_serviceContext			= null;

	/** commands from UI */
	@Override
	public void handleMessage(Message msg)
	{
		int type = msg.what;
		String command;

		try
		{
			command = (String) msg.obj;
		}
		catch (Throwable e)
		{
			command = "";
			return;
		}
		Logger.d(TAG, "Message received, type = " + type + "command = " + command);
	}

	private void _stopAllServices()
	{
	}
	
	private void _resetAllServices()
	{
	}

	public ServiceHandler(Context context)
	{
		super();
		_serviceContext = context;
		_resetAllServices();
	}
}
