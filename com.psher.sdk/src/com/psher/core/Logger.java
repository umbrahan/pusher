package com.psher.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class Logger
{
	public interface ILoggerTarget
	{
		void writeLogLine(String line);
	};

	private static HashMap<ILoggerTarget, String>	_targets	= new HashMap<ILoggerTarget, String>();

	public static void addTarget(String name, ILoggerTarget lt)
	{
		if (_targets.containsValue(name)) return;
		_targets.put(lt, name);
	}

	public static final int				NONE				= 0;
	public static final int				ERROR				= 1;
	public static final int				WARN				= 2;
	public static final int				INFO				= 3;
	public static final int				DEBUG				= 4;
	public static final int				VERBOSE				= 5;

	private static int					_counter			= 0;
	private static int					_logLevel			= ERROR;
	private static int					_logcatLevel		= VERBOSE;
	// private static String _file = "";

	// private static Thread _recordor = null;
	private static boolean				_bLogging			= false;
	private static boolean				_bRunning			= false;

	protected static Object				lock				= new Object();
	// this list stores the pending Request's key
	private static LinkedList<String>	_pendingLogLines	= new LinkedList<String>();

	private static Context				_context			= null;
	private static File					_logFile			= null;
	private static OutputStream			_outputStream		= null;

	private static boolean _tryOpenLogFile(String logfile)
	{
		boolean ret = false;
		try
		{
			// File file;
			_logFile = new File(logfile);
			// if(!file.exists())
			{
				_logFile.createNewFile();
			}
			_outputStream = new FileOutputStream(_logFile);
			ret = true;
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	private static void _openLogFile()
	{
		if (null != _outputStream) return;

		try
		{
			String logpath = null;

			ArrayList<String> pathes = new ArrayList<String>();

			logpath = _context.getFilesDir().getAbsolutePath() + "/";
			pathes.add(logpath);
			logpath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
			pathes.add(logpath);

			for (int i = 0; i < pathes.size(); i++)
			{
				String file = pathes.get(i);
				try
				{
					new File(file).mkdirs();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
				file += _context.getPackageName() + ".log.txt";
				
				_tryOpenLogFile(file);
				if (null != _outputStream) break;
			}
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void _writeLine(String line)
	{
		for (ILoggerTarget lt : _targets.keySet())
		{
			try
			{
				String prefix = _targets.get(lt);
				lt.writeLogLine(prefix + ":" + line);
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// try to open file, and write log-line into it
		_openLogFile();

		byte[] data;
		try
		{
			if (null != _outputStream)
			{
				data = line.getBytes("UTF-8");
				_outputStream.write(data, 0, data.length);
				_outputStream.flush();
			}
		}
		catch (UnsupportedEncodingException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void _closeLogFile()
	{
		try
		{
			if (null != _outputStream)
			{
				_outputStream.close();
			}
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void _startRecordor()
	{
		Thread _recordor = new Thread()
		{
			public void run()
			{
				//UnhandledExceptionHandler.install();

				_bRunning = true;
				while (_bLogging)
				{
					try
					{
						synchronized (lock)
						{
							try
							{
								lock.wait();
							}
							catch (InterruptedException e)
							{
							}
						}

						String line;
						do
						{
							line = null;
							synchronized (lock)
							{
								if (!_pendingLogLines.isEmpty())
								{
									line = _pendingLogLines.removeFirst();
								}
							}
							if (null != line)
							{
								line += "\n";
								_writeLine(line);
								try
								{
									Thread.sleep(10);
								}
								catch (InterruptedException e)
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								// Log.v("Logger", line);
							}
						}
						while (null != line);
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				_bRunning = false;
			}
		};
		_bLogging = true;
		_recordor.setName("Logger");
		_recordor.start();
	}

	private static void _log(int lev, String tag, String msg)
	{
		Calendar c = Calendar.getInstance();
		String time = String.format("%d-%d-%d %d:%d:%d", 
				c.get(Calendar.YEAR),
				c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH),
				c.get(Calendar.HOUR_OF_DAY),
				c.get(Calendar.MINUTE),
				c.get(Calendar.SECOND),
				c.get(Calendar.MONTH));
		String log = String.format("%s %s %s", time, tag, msg);
		synchronized (lock)
		{
			_pendingLogLines.add(log);
			lock.notifyAll();
		}
	}

	/**
	 * 日志级别设置。
	 * 
	 * @param level
	 *           低于或等于此级别的日志信息提供输出功能。
	 */
	public static void start(Context c, int level, int logcatlevel)
	{
		if (0 == _counter)
		{
			_context = c;
			_logLevel = level;
			_logcatLevel = logcatlevel;
			// _openLogFile(file);
			_startRecordor();
		}
		_counter++;
		return;
	}

	public static void stop()
	{
		_counter--;
		if (_counter <= 0)
		{
			_bLogging = false;
			synchronized (lock)
			{
				lock.notifyAll();
			}
			while (_bRunning)
			{
				try
				{
					Thread.sleep(100);
				}
				catch (InterruptedException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			_closeLogFile();
		}
	}

	private static String _throwable2str(Throwable throwable)
	{
		String msg = "";

		msg += "MSG:" + throwable.toString() + "\n";
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

	public static void e(String tag, Throwable t)
	{
		String msg = _throwable2str(t);
		e(tag, msg);
	}

	public static void e(String tag, String msg)
	{
		if (_logcatLevel >= ERROR)
		{
			Log.e(tag, msg);
		}

		if (_logLevel >= ERROR)
		{
			_log(ERROR, tag, msg);
		}
	}

	public static void w(String tag, String msg)
	{
		if (_logcatLevel >= WARN)
		{
			Log.w(tag, msg);
		}
		if (_logLevel >= WARN)
		{
			_log(WARN, tag, msg);
		}
	}

	public static void i(String tag, String msg)
	{
		if (_logcatLevel >= INFO)
		{
			Log.i(tag, msg);
		}
		if (_logLevel >= INFO)
		{
			_log(INFO, tag, msg);
		}
	}

	public static void d(String tag, String msg)
	{
		if (_logcatLevel >= DEBUG)
		{
			Log.d(tag, msg);
		}
		if (_logLevel >= DEBUG)
		{
			_log(DEBUG, tag, msg);
		}
	}

	public static void v(String tag, String msg)
	{
		if (_logcatLevel >= VERBOSE)
		{
			Log.v(tag, msg);
		}
		if (_logLevel >= VERBOSE)
		{
			_log(VERBOSE, tag, msg);
		}
	}
}
