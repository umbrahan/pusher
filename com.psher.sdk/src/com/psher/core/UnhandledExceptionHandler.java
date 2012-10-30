package com.psher.core;

import java.util.HashMap;

/**
 * 未捕获异常处理类
 * 
 * @author Administrator
 * 
 */
public class UnhandledExceptionHandler implements Thread.UncaughtExceptionHandler //UncaughtExceptionHandler接口用来捕获运行时未被程序捕获异常. 
{

	private static final String				TAG				= UnhandledExceptionHandler.class.getName();
	private static HashMap<Thread, Boolean>	_handledThreads	= new HashMap<Thread, Boolean>();

	private Thread.UncaughtExceptionHandler	_oldHandler;

	private boolean							_bSuperCall		= true;

	UnhandledExceptionHandler(boolean bSuperCall)
	{
		this._oldHandler = Thread.getDefaultUncaughtExceptionHandler(); // 获取默认处理对象
		_bSuperCall = bSuperCall; // 真假
	}

	private static String _throwable2str(Throwable throwable)
	{
		String msg = "";

		String detail = throwable.getMessage();
		if (null == detail) detail = throwable.toString();
		msg += "MSG:" + detail + "\n";
		msg += "STACKTRACE:" + "\n";
		StackTraceElement[] stackTraceElements = throwable.getStackTrace();
		if (stackTraceElements != null && stackTraceElements.length > 0)
		{
			for (StackTraceElement stackTraceElement : stackTraceElements)
			{
				if (stackTraceElement != null)
				{
					msg += stackTraceElement.toString() + "\n";
				}
			}
		}
		return msg;
	}

	/**
	 *  指定线程抛出未捕获的异常时，回调此方法记录日志־
	 */
	@Override
	public void uncaughtException(Thread thread, Throwable throwable)
	{
		String msg = "";

		if (thread != null)
		{
			msg = "*** THREAD:" + thread.getName() + "\n";
		}
		if (throwable != null)
		{
			msg += _throwable2str(throwable);
		}
		Logger.e(TAG, msg);

		if (null != _oldHandler && _bSuperCall)
		{
			_oldHandler.uncaughtException(thread, throwable);
		}
	}

	/**
	 * 为指定线程安装UnhandledExceptionHandler 当此线程抛出未捕获到的异常时，回调uncaughtException方法
	 */
	public static void install()
	{
		Thread thread = Thread.currentThread(); // 获取当前线程
		if (_handledThreads.containsKey(thread)) //判断当前线程，是否在hashMap中，如果在，直接return 如果不在，添加到Map中
		{
			return;
		}
		_handledThreads.put(thread, true); // 添加当前线程到Map中
		thread.setUncaughtExceptionHandler(new UnhandledExceptionHandler(/*
																		 * bSuperCall
																		 * ||
																		 */true));
	}
}
