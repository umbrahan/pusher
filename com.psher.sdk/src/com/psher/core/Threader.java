package com.psher.core;

public class Threader extends Thread
{
	private boolean _bRunning = false;
	private boolean _bOnce = false;
	
	private Runnable _r;
	protected static Object	lock = new Object();

	public void run()
	{
		while (_bRunning)
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
			if (_bRunning)
			{
				_r.run();
				if (_bOnce)
				{
					_bRunning = false;
				}
			}
		}
	}
	
	public Threader(Runnable r, boolean bOnce)
	{
		_r = r;
		_bRunning = true;
		_bOnce = bOnce;
	}
	
	public void Start()
	{
		this.start();
	}
	
	public void Touch()
	{
		synchronized (lock)
		{
			lock.notify();
		}
	}
	
	public void Stop()
	{
		_bRunning = false;
		synchronized (lock)
		{
			lock.notifyAll();
		}
	}
}
