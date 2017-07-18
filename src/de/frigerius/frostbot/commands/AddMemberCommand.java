package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;

public class AddMemberCommand extends BaseGuildCommand
{

	public AddMemberCommand(int cmdPwr)
	{
		this("addmember", cmdPwr);
	}

	protected AddMemberCommand(String cmd, int cmdPwr)
	{
		super(cmd, cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length >= 1)
		{
			boolean isLeader = false;
			if (args.length == 2)
			{
				try
				{
					isLeader = Integer.parseInt(args[1]) == 1;
				} catch (NumberFormatException e)
				{
					return CommandResult.ArgumentError;
				}
			}
			String guildId = _guildManager.getGuildId(client);
			if (guildId.equals(""))
			{
				return CommandResult.InvalidPermissions;
			} else
			{
				_bot.TS3API.sendPrivateMessage(client.getId(), addMember(guildId, client, args[0], isLeader));
				return CommandResult.NoErrors;
			}
		} else
		{
			return CommandResult.ArgumentError;
		}
	}

	protected String addMember(String guildId, Client client, String name, boolean isLeader)
	{
		try
		{
			Client other = _bot.TS3API.getClientByNameExact(name, false).get();
			if (other != null)
			{
				String guildName = _guildManager.addMember(client, other, guildId, isLeader);
				if (!guildName.equals(""))
				{
					_bot.TS3API.sendPrivateMessage(other.getId(), String.format("Du wurdest von %s der Gilde %s zugefügt.", client.getNickname(), guildName));
					return String.format("%s wurde deiner Gilde hinzugefügt.", other.getNickname());
				} else
				{
					return String.format("%s scheint schon in einer Gilde zu sein.", other.getNickname());
				}
			} else
			{
				return String.format(ColoredText.red("%s konnte nicht gefunden werden."), name);
			}

		} catch (InterruptedException e)
		{
			return String.format(ColoredText.red("%s - Zeitüberschreitung bei Anfrage."), name);
		}
	}

	@Override
	public String getArguments()
	{
		return "[TS-User-Name] [(Ist Gildenleiter)]";
	}

	@Override
	public String getDescription()
	{
		return "Fügt den Nutzer mit dem spezifischen Namen deiner Gilde hinzu.";
	}

	@Override
	protected String getDetails()
	{
		return "Wird zusätzlich eine 1 übergeben, wird der Member zusätzlich als Gildenleiter eingetragen.\n" + "Beispiel1: !addmember \"Ich bin ein TS Nutzer\"\n"
				+ "Beispiel2(Member ist Gildenleiter) !addmember \"Nutzername ist Gildenleiter\" 1";
	}

}
