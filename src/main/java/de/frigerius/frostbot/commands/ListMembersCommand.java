package main.java.de.frigerius.frostbot.commands;

import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.MyClient;

public class ListMembersCommand extends BaseGuildCommand
{

	public ListMembersCommand(int cmdPwr)
	{
		super("listmember", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		String id = _guildManager.getGuildId(client);
		if (id.equals(""))
		{
			return CommandResult.InvalidPermissions;
		}
		List<String> members = _guildManager.getMember(id);
		if (members.isEmpty())
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), "In deiner Gilde sind keine Mitglieder, glückwunsch, das dürfte garnicht passieren");
		} else
		{
			_bot.sendBulkMessages(client, "Liste der Mitglieder:", members);
		}
		return CommandResult.NoErrors;

	}

	@Override
	public boolean hasClientRights(Client client,int cmdPwr)
	{
		return MyClient.HasCmdPower(client.getServerGroups(), getCmdPwr());
	}

	@Override
	public String getArguments()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Listet alle Mitglieder deiner Gilde mit ihrer ID auf.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
