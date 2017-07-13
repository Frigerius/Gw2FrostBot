package de.frigerius.frostbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

public class ServerInfo
{
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerInfo.class);

	public static void getChannelIds()
	{
		FrostBot.getInstance().TS3API.getChannels().onSuccess(channel -> {
			for (Channel ch : channel)
			{
				LOGGER.info(ch.getName() + " : " + ch.getId());
			}
		});

	}

	public static void getServerGroups()
	{
		FrostBot.getInstance().TS3API.getServerGroups().onSuccess(sg -> {
			for (ServerGroup s : sg)
			{
				LOGGER.info(s.getName() + ": " + s.getId());
			}
		});

	}

}
