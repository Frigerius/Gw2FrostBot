package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class AddMemberCommand extends BaseGuildCommand
{

	public AddMemberCommand(int cmdPwr)
	{
		super("addmember", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length == 1)
		{
			try
			{
				Client other = _bot.TS3API.getClientByNameExact(args[0], false).get();
				if (other != null)
				{
					String guildId = _guildManager.getGuildId(client);
					if (guildId.equals(""))
					{
						return CommandResult.InvalidPermissions;
					} else
					{
						String guildName = _guildManager.addMember(client, other, guildId);
						if (!guildName.equals(""))
						{
							_bot.TS3API.sendPrivateMessage(other.getId(), String.format("Du wurdest von %s der Gilde %s zugefügt.", client.getNickname(), guildName));
							_bot.TS3API.sendPrivateMessage(client.getId(), String.format("%s wurde deiner Gilde hinzugefügt.", other.getNickname()));
						} else
						{
							_bot.TS3API.sendPrivateMessage(client.getId(), String.format("%s scheint schon in einer Gilde zu sein.", other.getNickname()));
						}
						return CommandResult.NoErrors;
					}
				}
			} catch (InterruptedException e)
			{

				return CommandResult.Error;
			}
		} else
		{
			return CommandResult.ArgumentError;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[TS-User-Name]";
	}

	@Override
	public String getDescription()
	{
		return "Fügt den Nutzer mit dem spezifischen Namen deiner Gilde hinzu.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
