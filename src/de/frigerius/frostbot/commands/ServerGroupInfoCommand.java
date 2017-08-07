package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ServerInfo;

public class ServerGroupInfoCommand extends BaseCommand
{

	public ServerGroupInfoCommand(int cmdPwr)
	{
		super("getsginfo", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		ServerInfo.getServerGroups();
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
