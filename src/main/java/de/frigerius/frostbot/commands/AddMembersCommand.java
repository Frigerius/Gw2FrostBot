package main.java.de.frigerius.frostbot.commands;

import java.util.ArrayList;
import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class AddMembersCommand extends AddMemberCommand
{

	public AddMembersCommand(int cmdPwr)
	{
		super("addmembers", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length > 0)
		{
			String guildId = _guildManager.getGuildId(client);
			if (guildId.equals(""))
			{
				return CommandResult.InvalidPermissions;
			} else
			{
				List<String> resultMsgs = new ArrayList<String>(args.length);
				for (String name : args)
				{
					resultMsgs.add(addMember(guildId, client, name, false));
				}
				_bot.sendBulkMessages(client, "", resultMsgs);
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
		return "[TS-User-Name] [(TS-User-Name)] [(TS-User-Name)]...";
	}

	@Override
	public String getDescription()
	{
		return "Fügt einen oder mehr Nutzer deiner Gilde hinzu.";
	}

	@Override
	public String getDetails()
	{
		return "Beispiel1: !addmembers \"Nutzer 1\" \"Nutzer 2\"\nBeispiel2: !addmembers \"Nutzer 1\" \"Nutzer 2\" \"Nutzer 3\"";
	}
}
