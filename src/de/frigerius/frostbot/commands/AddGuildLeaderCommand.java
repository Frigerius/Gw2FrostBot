package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.MyClient;

public class AddGuildLeaderCommand extends RequestingBaseGuildCommand
{

	public AddGuildLeaderCommand(int cmdPwr)
	{
		super("addleader", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length != 1)
			return CommandResult.ArgumentError;
		if (AddRequester(client.getUniqueIdentifier()))
		{
			try
			{
				String guildId = _guildManager.getGuildId(client);
				if (guildId.equals(""))
				{
					return CommandResult.ArgumentError;
				} else
				{
					if (_guildManager.setGuildLeader(guildId, Integer.parseInt(args[0]), true))
						_bot.TS3API.sendPrivateMessage(client.getId(), "Leader wurde hinzugefügt.");
					else
						_bot.TS3API.sendPrivateMessage(client.getId(), "Leader konnte nicht hinzugefügt werden.");
				}
			} catch (NumberFormatException e)
			{
				return CommandResult.ArgumentError;
			} finally
			{
				RemoveRequester(client.getUniqueIdentifier());
			}
		}
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client client)
	{
		return MyClient.HasCmdPower(client.getServerGroups(), getCmdPwr());
	}

	@Override
	public String getArguments()
	{
		return "[Mitglied ID]";
	}

	@Override
	public String getDescription()
	{
		return "Ernennt das Mitglied zum Gildenleiter. " + ColoredText.red("(Ohne Diplomat bringt ihm dieses nichts!)");
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
