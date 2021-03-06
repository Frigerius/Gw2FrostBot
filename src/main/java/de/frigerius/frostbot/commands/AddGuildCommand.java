package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;

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
		if (!_bot.isValidAPIKey(apiKey))
			return CommandResult.ArgumentError;
		String guildNameClean = args[1];

		if (_guildManager.addGuild(apiKey, guildNameClean))
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Die Gilde wurde erfolgreich hinzugef�gt."));
		} else
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Die Gilde konnte nicht hinzugef�gt werden, bitte �berpr�fe deine Eingabe."));
		}

		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[API-Key] [GuildName]";
	}

	@Override
	public String getDescription()
	{
		return "F�gt eine Servergruppe f�r die Gilde GuildName hinzu.";
	}

	@Override
	protected String getDetails()
	{
		return "Beispiel: !addguild XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX \"Ein Gildenname\"\n"
				+ ColoredText.red("WICHTIG: Bitte den Gildennamen ohne Tag angeben!!");
	}

}
