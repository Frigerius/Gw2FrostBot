package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class ListGuildsCommand extends BaseGuildCommand
{

	public ListGuildsCommand(int cmdPwr)
	{
		super("listguilds", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		_bot.sendBulkMessages(client, "Es stehen folgende Gilden zur Verfügung:", _guildManager.getGuilds());
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Listet alle Gilden auf, für die es eine ServerGruppe gibt.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
