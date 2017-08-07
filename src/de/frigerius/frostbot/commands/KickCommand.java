package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class KickCommand extends BaseCommand
{

	public KickCommand(int cmdPwr)
	{
		super("kick", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		_bot.TS3API.kickClientFromServer("Was du nicht willst, was man dir tut...", c.getId());
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client c,int cmdPwr)
	{
		return true;
	}

	@Override
	public String getArguments()
	{
		return "[Name]";
	}

	@Override
	public String getDescription()
	{
		return "Willst du das wirklich tun...?";
	}

	@Override
	protected String getDetails()
	{
		return "Bist du dir auch ganz sicher?";
	}

}
