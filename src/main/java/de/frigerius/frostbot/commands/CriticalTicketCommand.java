package main.java.de.frigerius.frostbot.commands;

import java.util.concurrent.locks.ReentrantLock;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public abstract class CriticalTicketCommand extends BaseCommand
{
	private static ReentrantLock _lock = new ReentrantLock();

	public CriticalTicketCommand(String command, int cmdPwr)
	{
		super(command, cmdPwr);
	}

	@Override
	protected final CommandResult handleIntern(Client client, String[] args)
	{
		_lock.lock();
		try
		{
			return sHandleIntern(client, args);
		} finally
		{
			_lock.unlock();
		}
	}

	protected abstract CommandResult sHandleIntern(Client client, String[] args);

}
