package main.java.de.frigerius.frostbot.commands;

import main.java.de.frigerius.frostbot.GuildManager;

public abstract class BaseGuildCommand extends BaseCommand {
	GuildManager _guildManager;

	public BaseGuildCommand(String command, int cmdPwr) {
		super(command, cmdPwr);
		_guildManager = _bot.getGuildManager();
	}

}
