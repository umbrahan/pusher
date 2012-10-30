package com.psher.core;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;

public class Utils
{
	private static final String	TAG	= Utils.class.getName();

	/**
	 * 检查SD卡是否可用
	 * 
	 * @return
	 */
	public static boolean isSDCardAvailable()
	{
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED))
		{
			return true;
		}
		return false;
	}

	public static long currentTimeMillis()
	{
		return System.currentTimeMillis();
	}

	public static Integer getAPILevel()
	{
		return android.os.Build.VERSION.SDK_INT;
	}

	public static byte[] getSignature(Context c)
	{
		PackageManager pm = c.getPackageManager();
		PackageInfo pi;

		ByteBuffer bb = ByteBuffer.allocate(4);
		bb.putInt(0xbadface);
		try
		{
			pi = pm.getPackageInfo(c.getPackageName(), PackageManager.GET_SIGNATURES);
			Signature[] ss = pi.signatures;
			int len = 0;
			for (Signature s : ss)
			{
				byte[] bf = s.toByteArray();
				len += bf.length;
			}
			bb = ByteBuffer.allocate(len);
			for (Signature s: ss)
			{
				bb.put(s.toByteArray());
			}
		}
		catch (Throwable e)
		{
		}
		
		return bb.array();
	}
	
	public static String getPackageVersion(Context c)
	{
		String strVersion = "";

		PackageManager pm = c.getPackageManager();
		PackageInfo pi;

		try
		{
			pi = pm.getPackageInfo(c.getPackageName(), 0);
			strVersion = pi.versionName;
		}
		catch (Throwable e)
		{
			strVersion = ""; // failed, ignored
		}
		return strVersion;
	}

	public static String map2String(HashMap<String, String> message)
	{
		String r = "";
		for (String key : message.keySet())
		{
			r += "'" + key + "'";
			r += ":";
			r += "'" + message.get(key) + "'; ";
		}
		return r;
	}

	public static String safeString(String s)
	{
		if (null == s) return "";
		return s;
	}
	
	public static String boolean2str(boolean b)
	{
		return b ? "true" : "false";
	}

	public static String readStringFromStream(InputStream is) throws Exception
	{
		String recv = "";
		int available = is.available(), len = 0, total = 0;
		if (available > 0)
		{
			byte[] buf = new byte[available];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (available > 0 && total < available)
			{
				len = is.read(buf);
				total += len;
				baos.write(buf, 0, len);
			}
			recv = new String(baos.toByteArray(), "UTF-8");
		}
		return recv;
	}

	public static String getIpAddress()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress())
					{
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		}
		catch (SocketException e)
		{
			Logger.e(TAG, e);
		}
		catch (Throwable e)
		{
			Logger.e(TAG, e);
		}

		return "";
	}

	// first
	// android.os.Build.static vars

	// second
	// add <uses-permission android:name="android.permission.READ_PHONE_STATE"/>
	// first
	public static TelephonyManager getTelephonyManager(Context c)
	{
		TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
		return tm;
	}

	public static boolean isApplicationHasInstalled(Context c, String app)
	{
		PackageManager pm = c.getPackageManager();
		List<PackageInfo> pal = pm.getInstalledPackages(0);
		for (int i = 0, size = pal.size(); i < size; i++)
		{
			PackageInfo p = pal.get(i);
			ApplicationInfo ai = p.applicationInfo;
			String appname = ai.loadLabel(pm).toString();
			// String packagename = p.packageName;
			if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) > 0)
			{
			}
			else
			{
			}
			if (appname.equals(app)) return true;
		}
		return false;
	}

	public static byte[] hashBytes(byte[] buf)
	{
		MessageDigest messageDigest = null;
		byte[] byteArray = new byte[0];
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
			messageDigest.reset();
			messageDigest.update(buf);
			byteArray = messageDigest.digest();
		}
		catch (NoSuchAlgorithmException e)
		{
			Logger.e(TAG, e);
			System.out.println("NoSuchAlgorithmException caught!");
			//System.exit(-1);
		}
		catch (Throwable e)
		{
			Logger.e(TAG, e);
		}
		return byteArray;
	}
	
	public static int hashBytes2Integer(byte[] byteArray)
	{
		int ret = 0x900d;
	
		try
		{
			int obytes = 4;
			byte[] output = new byte[obytes];
	
			int i;
			for (i = 0; i < output.length; i++)
			{
				output[i] = 0;
			}
			for (i = 0; i < byteArray.length; i++)
			{
				output[i % obytes] ^= byteArray[i];
			}
	
			for (i = 0; i < output.length; i++)
			{
				ret <<= 8;
				ret += output[i];
			}
			if ((ret & 0x80000000) != 0) ret ^= 0x8aaaaaaa;
		}
		catch (Throwable e)
		{
			Logger.e(TAG, e);
		}
	
		return ret;
	}
	
	public static int hashString2Integer(String str)
	{
		int ret = 0x900dface;
		
		try
		{
			byte[] byteArray;
			byteArray = hashBytes(str.getBytes("UTF-8"));
			ret = hashBytes2Integer(byteArray);
		}
		catch (Exception e)
		{
			Logger.e(TAG, e);
		}
		return ret;
	}

	// 高效的字符替换程序
	public static String replace(String strSource, String strFrom, String strTo)
	{
		if (strSource == null)
		{
			return null;
		}
		int i = 0;
		if ((i = strSource.indexOf(strFrom, i)) >= 0)
		{
			char[] cSrc = strSource.toCharArray();
			char[] cTo = strTo.toCharArray();
			int len = strFrom.length();
			StringBuffer buf = new StringBuffer(cSrc.length);
			buf.append(cSrc, 0, i).append(cTo);
			i += len;
			int j = i;
			while ((i = strSource.indexOf(strFrom, i)) > 0)
			{
				buf.append(cSrc, j, i - j).append(cTo);
				i += len;
				j = i;
			}
			buf.append(cSrc, j, cSrc.length - j);
			return buf.toString();
		}
		return strSource;
	}

	// 自动创建快捷方式
	public static void createShortcut(Context context, String shortcutname, int icon, Class<?> target)
	{
		// <uses-permission
		// android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
		Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
		intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(context, icon));
		intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, shortcutname);
		intent.putExtra("duplicate", false);
		Intent sIntent = new Intent(Intent.ACTION_MAIN);
		sIntent.addCategory(Intent.CATEGORY_LAUNCHER);// 加入action,和category之后，程序卸载的时候才会主动将该快捷方式也卸载
		sIntent.setClass(context, target);
		intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, sIntent);
		context.sendBroadcast(intent);
	}

	// 判断快捷方式是否存在
	public static boolean hasShortCut(Context context, String shortcutname)
	{
		// <uses-permission
		// android:name="com.android.launcher.permission.READ_SETTINGS"/>
		// <uses-permission
		// android:name="com.android.launcher.permission.INSTALL_SHORTCUT"/>
		String url = "";
		System.out.println(getSystemVersion());
		if (getSystemVersion() < 8)
		{
			url = "content://com.android.launcher.settings/favorites?notify=true";
		}
		else
		{
			url = "content://com.android.launcher2.settings/favorites?notify=true";
		}
		ContentResolver resolver = context.getContentResolver();
		Cursor cursor = resolver.query(Uri.parse(url), null, "title= ?", new String[] { shortcutname }, null);

		if (cursor != null && cursor.moveToFirst())
		{
			cursor.close();
			return true;
		}

		return false;
	}

	private static int getSystemVersion()
	{
		return android.os.Build.VERSION.SDK_INT;
	}

	public static void sleep(long ms)
	{
		try
		{
			Thread.sleep(ms);
		}
		catch (InterruptedException e)
		{
			Logger.e(TAG, e);
		}
		catch (Throwable e)
		{
			Logger.e(TAG, e);
		}

	}

	public static void run(String app, String appparams)
	{
		String[] params = appparams.split(" ");
		String[] progArray = new String[params.length + 1];

		int nIndex = 0;
		progArray[nIndex++] = app;
		for (String p : params)
		{
			progArray[nIndex++] = p;
		}
		try
		{
			// sh = Runtime.getRuntime().exec(progArray);
			Runtime.getRuntime().exec(progArray);
		}
		catch (IOException e)
		{
			Logger.e(TAG, e);
		}
		catch (Throwable e)
		{
			Logger.e(TAG, e);
		}
	}

	public static void killUnrelatedActivityProcesses(Context c)
	{
		if (getAPILevel() >= 8)
		{
			_killUnrelatedActivityProcesses(c);
		}
	}
	
	@TargetApi(8)
	private static void _killUnrelatedActivityProcesses(Context c)
	{
		ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);

		String[] friends = { "com.crazyamber.", "launcher" };
		List<RunningAppProcessInfo> apps = am.getRunningAppProcesses();
		for (RunningAppProcessInfo pi : apps)
		{
			for (String pname : pi.pkgList)
			{
				if (null != pname && pname.length() > 0)
				{
					boolean bFriend = false;
					for (String friend : friends)
					{
						if (pname.contains(friend))
						{
							bFriend = true;
							break;
						}
					}
					if (bFriend)
					{
						continue;
					}
					try
					{
						if (getSystemVersion() >= 8)
						{
							am.killBackgroundProcesses(pname);
						}
					}
					catch (Exception e)
					{
						// TODO Auto-generated catch block
					}
				}
			}
		}
	}

	public static boolean fileExists(String filename)
	{
		File file = new File(filename);
		return file.exists();
	}

	/*
	protected static interface _Function
	{
		void process(final Looper looper, final HandlerThread ht, Message msg);
	};

	// not working now
	// _temp_ui_thread will running forever
	protected static void _runInThread(final Context c, final int what, final Object obj, final _Function funcPrepare)
	{
		new HandlerThread("_temp_ui_thread")
		{
			@Override
			protected void onLooperPrepared()
			{
				new Handler(getLooper())
				{
					@Override
					public void handleMessage(Message msg)
					{
						funcPrepare.process(getLooper(), (HandlerThread) getLooper().getThread(), msg);
					}
				}.obtainMessage(what, obj).sendToTarget();
			}
		}.start();
	}

	public static void toastInThread(final Context c, final String message)
	{
		final int DISPLAY_UI_TOAST = 0x70a37;
		_runInThread(c, DISPLAY_UI_TOAST, message, new _Function()
		{
			@Override
			public void process(final Looper looper, final HandlerThread ht, Message msg)
			{
				if (DISPLAY_UI_TOAST == msg.what)
				{
					Toast t = Toast.makeText(c, (String) msg.obj, Toast.LENGTH_LONG);
					t.show();
					final Timer timer = new Timer();
					timer.schedule(new TimerTask()
					{
						@Override
						public void run()
						{
							ht.quit();
							timer.cancel();
						}
					}, 5000);
				}
			}
		});
	}
	*/
	
	public static int parseInt(String str)
	{
		int ret = 0;
		try
		{
			ret = Integer.valueOf(str);
		}
		catch (Exception e)
		{
			
		}
		return ret;
	}

	public static long parseLong(String str)
	{
		long ret = 0;
		try
		{
			ret = Long.valueOf(str);
		}
		catch (Exception e)
		{
			
		}
		return ret;
	}

	public static byte[] intToBytes(int n)
	{
		byte[] bytes = ByteBuffer.allocate(Integer.SIZE / 8).putInt(n).array();
		return bytes;
	}
	
	public static String bytesToHex(byte[] data)
	{
		if (data == null)
		{
			return null;
		}
		
		int len = data.length;
		String str = "";
		for (int i = 0; i < len; i++) 
		{
			String hs = java.lang.Integer.toHexString(data[i] & 0xFF);
			if ((data[i] & 0xFF) < 16)
				str = str + "0" + hs;
			else
				str = str + hs;
		}
		return str;
	}

	public static byte[] hexToBytes(String str)
	{
		if (str == null)
		{
			return null;
		}
		else if (str.length() < 2)
		{
			return null;
		}
		else
		{
			int i;
			int len = str.length() / 2;
			byte[] buffer = new byte[len];
			for (i = 0; i < len; i++)
			{
				buffer[i] = (byte) Integer.parseInt(
						str.substring(i * 2, i * 2 + 2), 16);
			}
			return buffer;
		}
	}
	
	public static void msgBox(Context c, String title, String message)
	{
		Dialog dialog = new AlertDialog.Builder(c).setTitle(title)
				.setMessage(message)
				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface dialog, int whichButton)
					{

					}
				}).create();

		dialog.show();
	}
	
	public static int clamp(int v, int min, int max)
	{
		if (v < min) v = min;
		else if (v > max) v = max;
		return v;
	}
	public static int RandIn(int min, int max)
	{
		Random r = new Random();
		int n = r.nextInt();
		if (n < 0) n = -n;
		n = n % (max - min) + min;
		return n;
	}
}
