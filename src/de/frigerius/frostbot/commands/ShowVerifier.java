package de.frigerius.frostbot.commands;

import java.util.ArrayList;
import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ClientController;
import de.frigerius.frostbot.MyClient;

public class ShowVerifier extends BaseCommand
{
	ClientController _clientController;

	public ShowVerifier(int cmdPwr)
	{
		super("listverifier", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		List<String> sups = new ArrayList<>();
		for (MyClient client : _clientController.getSupporter())
		{
			sups.add(client.getName());
		}
		_bot.TS3API.sendPrivateMessage(c.getId(), "Momentan befinden sich folgende Verifizierer auf dem Server:\n" + String.join(", ", sups));
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
		return "Listet alle aktuell verfügbaren Verifizierer auf.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
