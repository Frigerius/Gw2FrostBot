package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.AutomatedVerification;
import de.frigerius.frostbot.ColoredText;

public class AddGuildCommand extends BaseGuildCommand
{

	public AddGuildCommand(int cmdPwr)
	{
		super("addguild", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length != 2)
			return CommandResult.ArgumentError;
		String apiKey = args[0];
		if (!AutomatedVerification.isValidAPIKey(apiKey))
			return CommandResult.ArgumentError;
		String guildNameClean = args[1];

		if (_guildManager.addGuild(apiKey, guildNameClean))
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Die Gilde wurde erfolgreich hinzugefügt."));
		} else
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Die Gilde konnte nicht hinzugefügt werden, bitte überprüfe deine Eingabe."));
		}

		return CommandResult.NoErrors;
	}

	@Override
	public String getFormatExtension()
	{
		return "[API-Key] [GuildName]";
	}

	@Override
	public String getDescription()
	{
		return "Fügt eine Servergruppe für die Gilde GuildName hinzu.";
	}

	@Override
	protected String getDetails()
	{
		return "Beispiel: !addguild XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX \"Ein Gildenname\"\n"
				+ ColoredText.red("WICHTIG: Bitte den Gildennamen ohne Tag angeben!!");
	}

}
