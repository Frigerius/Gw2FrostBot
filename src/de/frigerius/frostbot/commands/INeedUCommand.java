package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.BotSettings;
import de.frigerius.frostbot.MyClient;

public class INeedUCommand extends BaseCommand
{

	public INeedUCommand(int cmdPwr)
	{
		super("ineedu", 0);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		if (_bot.isUserRank(BotSettings.ignoreMeGroup))
			_bot.TS3API.removeClientFromServerGroup(BotSettings.ignoreMeGroup, c.getDatabaseId());
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client c)
	{
		return super.hasClientRights(c) && MyClient.isInServerGroup(c.getServerGroups(), BotSettings.ignoreMeGroup);
	}

	@Override
	public String getArguments()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Der Bot wird dich wieder anschreiben, wenn du den Server betrittst.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
