package de.frigerius.frostbot.commands;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.Permission;

import de.frigerius.frostbot.ServerInfo;

public class ChannelInfoCommand extends BaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(ChannelInfoCommand.class);

	public ChannelInfoCommand(int cmdPwr)
	{
		super("getcinfo", cmdPwr);
	}

	private void consoleCommandGetChannelInfo(int id)
	{
		_bot.TS3API.getChannels().onSuccess(channels -> {
			for (Channel ch : channels)
			{
				if (ch.getId() == id)
				{
					LOGGER.info(ch.getName() + " : " + ch.getId());
					Map<String, String> map = ch.getMap();
					for (Entry<String, String> entry : map.entrySet())
					{
						LOGGER.info(entry.getKey() + " : " + entry.getValue());
					}
					_bot.TS3API.getChannelPermissions(id).onSuccess(result -> {
						for (Permission perm : result)
						{
							LOGGER.info(perm.getName() + " : " + perm.getValue());
						}
					});
					ch.getParentChannelId();
				}
			}
		});
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		if (args.length == 0)
			ServerInfo.getChannelIds();
		else
			consoleCommandGetChannelInfo(Integer.parseInt(args[0]));
		return CommandResult.NoErrors;
	}

	@Override
	public String getFormatExtension()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "";
	}

	@Override
	protected String getDetails()
	{
		// TODO Auto-generated method stub
		return "";
	}

}
