package de.frigerius.frostbot.commands;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

public abstract class RequestingBaseCommand extends BaseCommand
{

	private HashSet<String> _clientUIDs = new HashSet<>();
	private ReentrantLock _lock = new ReentrantLock();

	public RequestingBaseCommand(String command, int cmdPwr)
	{
		super(command, cmdPwr);
	}

	protected boolean AddRequestor(String uid)
	{
		_lock.lock();
		try
		{
			if (_clientUIDs.contains(uid))
			{
				return false;
			} else
			{
				_clientUIDs.add(uid);
				return true;
			}
		} finally
		{
			_lock.unlock();
		}
	}

	protected void RemoveRequestor(String uid)
	{
		_lock.lock();
		try
		{
			_clientUIDs.remove(uid);
		} finally
		{
			_lock.unlock();
		}
	}
}
