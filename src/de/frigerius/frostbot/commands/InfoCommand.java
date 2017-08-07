package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class InfoCommand extends BaseCommand
{
	private final String _infoText;
	private final String _description;

	public InfoCommand(String command, int cmdPwr, String infoText, String description)
	{
		super(command, cmdPwr);
		_infoText = infoText;
		_description = description;
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		_bot.TS3API.sendPrivateMessage(c.getId(), _infoText);
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client c, int cmdPwr)
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
		return _description;
	}

	@Override
	protected String getDetails()
	{
		// TODO Auto-generated method stub
		return "";
	}

}
