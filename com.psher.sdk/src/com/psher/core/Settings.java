package com.psher.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings
{
	private static SimpleCipher _c = new SimpleCipher(
			Utils.hexToBytes("37ca9a53c87eaa66f8fe88444f02a876"), 
			Utils.hexToBytes("a3bb37149550b256009d23fc34b85836"));

	public static void set(Context c, String key, String val)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		sp.edit().putString(key, _c.encryptString(val)).commit();
	}

	public static String get(Context c, String key)
	{
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
		String val = _c.decryptString(sp.getString(key, ""));
		return val;
	}
	
	public static void setInt(Context c, String key, int val)
	{
		String v = Integer.toString(val);
		set(c, key, v);
	}
	
	public static int getInt(Context c, String key)
	{
		String r = get(c, key);
		return Utils.parseInt(r);
	}
}
