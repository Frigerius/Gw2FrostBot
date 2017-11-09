package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class GetAfkRule extends BaseCommand
{

	public GetAfkRule(int cmdPwr)
	{
		super("getafkrule", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		int rule = _bot.getAfkMover().GetChannelAfkRule(client.getChannelId());
		String sRule = "";
		switch (rule)
		{
		case 0:
			sRule = "Kein/Muted Mikro";
			break;
		case 1:
			sRule = "Keine/Muted Ausgabe";
			break;
		case 2:
			sRule = "Kein/Muted Mikro und Ausgabe";
			break;
		case 3:
			sRule = "Kein/Muted Mikro oder Ausgabe";
			break;
		default:
			sRule = "Keine extra Regel";
			break;
		}
		_bot.TS3API.sendPrivateMessage(client.getId(), String.format("Dein aktueller Channel benutzt folgende AFK-Regel: %s", sRule));
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
		return "Zeigt die aktuell verwendete AFK-Regel des Channels an.";
	}

	@Override
	protected String getDetails()
	{
		return "Nicht jeder Channel wird vom Bot auf AFKler geprüft.";
	}

}
