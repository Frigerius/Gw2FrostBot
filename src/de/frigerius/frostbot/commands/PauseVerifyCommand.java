package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ClientController;

public class PauseVerifyCommand extends BaseCommand
{
	ClientController _clientController;

	public PauseVerifyCommand(int cmdPwr)
	{
		super("pauseverify", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		_clientController.removeSupporter(c.getId());
		_bot.TS3API.sendPrivateMessage(c.getId(), "Hab eine entspannte Pause.");
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
		return "Entfernt dich von der Liste der verfügbaren Verifizierer, bis du dies aufhebst, oder dich neu verbindest.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
