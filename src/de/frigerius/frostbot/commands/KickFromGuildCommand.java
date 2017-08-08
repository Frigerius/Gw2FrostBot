package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.MyClient;

public class KickFromGuildCommand extends BaseGuildCommand
{
	public KickFromGuildCommand(int cmdPwr)
	{
		super("kickmember", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length != 1)
			return CommandResult.ArgumentError;
		String id = _guildManager.getGuildId(client);
		if (id.equals(""))
		{
			return CommandResult.InvalidPermissions;
		}
		try
		{
			String result = _guildManager.removeMember(Integer.parseInt(args[0]), id);
			if (result != "")
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green(String.format("%s wurde aus deiner Gilde entfernt.", result)));
			else
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Fehler beim entfernen des Mitglieds."));
		} catch (NumberFormatException ex)
		{
			return CommandResult.ArgumentError;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client client, int cmdPwr)
	{
		return MyClient.HasCmdPower(client.getServerGroups(), getCmdPwr());
	}

	@Override
	public String getArguments()
	{
		return "[Mitglied-ID]";
	}

	@Override
	public String getDescription()
	{
		return "Entfernt das Mitglied mit der angegebenen ID aus der ServerGruppe der Gilde.";
	}

	@Override
	protected String getDetails()
	{
		return ColoredText.green(String.format("!%s 42", getCommand()));
	}

}
