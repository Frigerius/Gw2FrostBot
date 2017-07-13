package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.MyClient;

public class LeaveGuildCommand extends RequestingBaseGuildCommand
{

	public LeaveGuildCommand(int cmdPwr)
	{
		super("leaveguild", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (AddRequestor(client.getUniqueIdentifier()))
		{
			try
			{
				_guildManager.LeaveGuild(client);
			} finally
			{
				RemoveRequestor(client.getUniqueIdentifier());
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
	public String getFormatExtension()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Du verlässt die ServerGruppe deiner gilde.";
	}

	@Override
	protected String getDetails()
	{
		return ColoredText.green(String.format("Beispiel: !%s 42", getCommand()));
	}

}
