package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.FrostBot;
import de.frigerius.frostbot.MyClient;

public abstract class BaseCommand
{
	private final String _command;
	private final int _cmdPwr;
	protected FrostBot _bot;

	protected BaseCommand(String command, int cmdPwr)
	{
		_command = command;
		_cmdPwr = cmdPwr;
		_bot = FrostBot.getInstance();
	}

	public enum CommandResult
	{
		NoErrors, Error, InvalidPermissions, ArgumentError
	}

	protected abstract CommandResult handleIntern(Client client, String[] args);

	public String getCommand()
	{
		return _command;
	}

	/**
	 * @param client
	 *            Client
	 * @param args
	 *            arguments
	 * @return 0 - no errors<br/>
	 *         1 - error<br/>
	 *         2 - invalid permission<br/>
	 *         3 - wrong format
	 */
	public final CommandResult handle(Client client, String[] args)
	{
		if (hasClientRights(client))
		{
			return handleIntern(client, args);
		}
		return CommandResult.InvalidPermissions;
	}

	public boolean hasClientRights(Client client)
	{
		return client == null || MyClient.GetCmdPower(client.getServerGroups()) >= _cmdPwr;
	}

	public String getFullCommand()
	{
		String com = "!" + _command;
		if (getFormatExtension() == "")
		{
			return com;
		}
		return String.format("%1$s %2$s", com, getFormatExtension());
	}

	public abstract String getFormatExtension();

	public abstract String getDescription();

	public String getDetailedDescription()
	{
		return String.format("!%s:\n%s\n%s\n%s", _command, getDescription(), ColoredText.green(getFullCommand()), getDetails());
	}

	protected abstract String getDetails();

	public int getCmdPwr()
	{
		return _cmdPwr;
	}
}
