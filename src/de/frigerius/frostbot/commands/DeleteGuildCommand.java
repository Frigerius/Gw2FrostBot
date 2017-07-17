package de.frigerius.frostbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class DeleteGuildCommand extends BaseGuildCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(DeleteGuildCommand.class);

	public DeleteGuildCommand(int cmdPwr)
	{
		super("delguild", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length != 1)
			return CommandResult.ArgumentError;
		int id;
		try
		{
			id = Integer.parseInt(args[0]);
			if (_guildManager.deleteGuild(id))
			{
				// _bot.TS3API.sendPrivateMessage(client.getId(), "Gilde wurde gelöscht.");
				LOGGER.info("Gilde wurde gelöscht.");
			} else
				return CommandResult.Error;
		} catch (NumberFormatException e)
		{
			return CommandResult.ArgumentError;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[GuildID]";
	}

	@Override
	public String getDescription()
	{
		return "Löscht die Gilde mit der angegebenen ID.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
