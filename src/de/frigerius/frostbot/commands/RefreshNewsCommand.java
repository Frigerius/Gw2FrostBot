package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class RefreshNewsCommand extends BaseCommand
{

	public RefreshNewsCommand(int cmdPwr)
	{
		super("refmsg", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		_bot.getNews().refresh();
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
		return "";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
