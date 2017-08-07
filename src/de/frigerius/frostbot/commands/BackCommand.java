package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ClientController;
import de.frigerius.frostbot.MyClient;

public class BackCommand extends BaseCommand
{
	ClientController _clientController;

	public BackCommand(int cmdPwr)
	{
		super("back", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		MyClient client = _clientController.removeAfk(c.getId());
		if (client != null)
		{
			_bot.TS3API.moveClient(c.getId(), client.getLastChannelId());
		} else
		{
			_bot.TS3API.sendPrivateMessage(c.getId(), "Oh, etwas ist schief gelaufen, ich habe wohl vergessen, dass du AFK warst.");
		}
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
		return "Du bist nicht mehr AFK und wirst zurück in deinen ursprünglichen Channel gezogen. (nur nach !afk möglich)";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
