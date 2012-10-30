package com.psher.core;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SimpleCipher
{
	private IvParameterSpec ivspec;
	private SecretKeySpec keyspec;
	private Cipher cipher;

	public SimpleCipher(byte[] iv, byte[] sk)
	{
		ivspec = new IvParameterSpec(iv);
		keyspec = new SecretKeySpec(sk, "AES");

		try
		{
			cipher = Cipher.getInstance("AES/CBC/NoPadding");
		}
		catch (NoSuchAlgorithmException e)
		{
			Logger.e("", e);
		}
		catch (NoSuchPaddingException e)
		{
			e.printStackTrace();
			Logger.e("", e);
		}
	}

	public byte[] decrypt(byte[] inbuf)
	{
		byte[] decrypted = null;
		try
		{
			cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);
			decrypted = cipher.doFinal(inbuf);

			ByteBuffer buf = ByteBuffer.allocate(decrypted.length);
			buf.put(decrypted);
			buf.position(0);
			int len = buf.getInt();
			//int hi = len >> 16;
			len = len & 0xffff;
			int hc = buf.getInt();
			byte[] ret = new byte[len];
			buf.get(ret, 0, len);
			int rhc = Utils.hashBytes2Integer(ret);
			if (hc == rhc)
				return ret;
			return new byte[0];
		}
		catch (Exception e)
		{
			//Logger.e(e);
		}
		
		return null;
	}
	
	public byte[] encrypt(byte[] inbuf)
	{
		byte[] encrypted = null;
		try
		{
			int len = inbuf.length;
			int align = len + 8 + 15;
			align /= 16;
			align *= 16;
			
			ByteBuffer buf = ByteBuffer.allocate(align);
			Random ran =new Random(System.currentTimeMillis());
			int hi =( ran.nextInt() & 0x7fff) << 16;
			int plen = len | hi;
			buf.putInt(plen);
			int hc = Utils.hashBytes2Integer(inbuf);
			buf.putInt(hc);
			int i;
			for (i = 0; i < len; i++)
			{
				buf.put(inbuf[i]);
			}
			byte[] inb = buf.array();

			cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
			encrypted = cipher.doFinal(inb);
		}
		catch (Exception e)
		{
			//Logger.e(e);
		}
		
		return encrypted;
	}
	
	public String encryptString(String text)
	{
		if (null == text || text.length() <= 0)
			return "";
		
		byte[] r;
		try
		{
			r = encrypt(text.getBytes("UTF-8"));
			return Utils.bytesToHex(r);
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return "";
	}
	
	public String decryptString(String text)
	{
		if (null == text || text.length() <= 0)
			return "";
		
		byte[] i = Utils.hexToBytes(text);
		byte[] r = decrypt(i);
		String ret = "";
		try
		{
			ret = new String(r,"UTF-8");
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		return ret;
	}
}
