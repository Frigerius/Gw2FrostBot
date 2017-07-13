package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ClientController;
import de.frigerius.frostbot.MyClient;

public class ResumeVerifyCommand extends BaseCommand
{

	ClientController _clientController;

	public ResumeVerifyCommand(int cmdPwr)
	{
		super("resumeverify", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		_clientController.addSupporter(new MyClient(c));
		_bot.TS3API.sendPrivateMessage(c.getId(), "Ich habe dich wieder in die Liste der aktiven Verifizierer eingetragen.");
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
		return "Fügt dich wieder in die Liste der aktiven Verifizierer ein.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
