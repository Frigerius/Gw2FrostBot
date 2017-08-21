package main.java.de.frigerius.frostbot.commands;

import main.java.de.frigerius.frostbot.GuildManager;

public abstract class RequestingBaseGuildCommand extends RequestingBaseCommand
{
	GuildManager _guildManager;

	public RequestingBaseGuildCommand(String command, int cmdPwr)
	{
		super(command, cmdPwr);
		_guildManager = _bot.getGuildManager();
	}

}
