package de.frigerius.frostbot;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MainLoop implements Runnable
{
	private final List<ClientService> clientServices = new LinkedList<>();
	private final Logger LOGGER = LoggerFactory.getLogger(MainLoop.class);
	private FrostBot _bot = FrostBot.getInstance();

	
	
	@Override
	public void run()
	{
		_bot.TS3API.getClients().onSuccess(result -> {
			for (ClientService service : clientServices)
			{
				try
				{
					service.handle(result);
				} catch (Exception e)
				{
					LOGGER.error("A service caused an exception", e);
				}
			}
		});

	}

	public void AddTask(ClientService service)
	{
		clientServices.add(service);
	}

	public void RemoveTask(ClientService service)
	{
		clientServices.remove(service);
	}

}