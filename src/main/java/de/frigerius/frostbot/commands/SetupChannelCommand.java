package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ChannelController;

public class SetupChannelCommand extends BaseCommand
{
	ChannelController _channelController;

	public SetupChannelCommand(int cmdPwr)
	{
		super("setupchannel", cmdPwr);
		_channelController = ChannelController.getInstance();
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		_channelController.setDefaultPermissions(client.getChannelId());
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
		return "Stellt den Channel für Events ein.";
	}

	@Override
	protected String getDetails()
	{
		return "Stellt die Rechte des Channels auf folgende Werte:\n" + "Betreten: 10\n" + "Abonnieren: 7\n" + "Beschreibung sehen: 10\n" + "Ändern: 0\n" + "Löschen: 60\n";
	}

}
