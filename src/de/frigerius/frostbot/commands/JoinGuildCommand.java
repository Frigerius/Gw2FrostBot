package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.AutomatedVerification;
import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.MyClient;

public class JoinGuildCommand extends RequestingBaseGuildCommand
{
	public JoinGuildCommand(int cmdPwr)
	{
		super("joinguild", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length != 2)
			return CommandResult.ArgumentError;
		try
		{
			if (AddRequester(client.getUniqueIdentifier()))
			{
				int gId = Integer.parseInt(args[0]);
				String apiKey = args[1];
				if (!AutomatedVerification.isValidAPIKey(apiKey))
					return CommandResult.ArgumentError;
				if (!_guildManager.joinGuild(apiKey, client, gId))
				{
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Bitte �berpr�fe deine Eingabe. Du scheinst nicht der ausgew�hlten Gilde anzugeh�ren."));
				}
			}
		} catch (NumberFormatException ex)
		{
			return CommandResult.ArgumentError;
		} finally
		{
			RemoveRequester(client.getUniqueIdentifier());
		}

		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client client)
	{
		return !MyClient.HasCmdPower(client.getServerGroups(), 11) && super.hasClientRights(client);
	}

	@Override
	public String getArguments()
	{
		return "[Gilden ID] [API-Key]";
	}

	@Override
	public String getDescription()
	{
		return "Tritt der Gilde mit der ID bei.";
	}

	@Override
	protected String getDetails()
	{
		return "Finde mit !listguilds die ID deiner Gilde heraus. Anschlie�end kannst du ihr wie im folgenden Beispiel beitreten:\n"
				+ ColoredText.green("Beispiel: !joinguild 42 XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");
	}

}
