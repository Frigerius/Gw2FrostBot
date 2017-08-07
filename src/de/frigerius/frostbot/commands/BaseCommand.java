package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.FrostBot;
import de.frigerius.frostbot.MyClient;

/**
 * Basic class for commands.
 * 
 * @author Vinzenz
 */
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

	/**
	 * Command handle return values.
	 * 
	 * @author Vinzenz
	 * 
	 */
	public enum CommandResult
	{
		/**
		 * Command handled without any errors.
		 */
		NoErrors,
		/**
		 * An Error occurred while handling.
		 */
		Error,
		/**
		 * User is not having the permission to use the command.
		 */
		InvalidPermissions,
		/**
		 * Wrong argument count/format/...
		 */
		ArgumentError
	}

	/**
	 * Implement your command behavior.
	 * 
	 * @param client
	 *            Requestor
	 * @param args
	 *            Command arguments, maybe empty
	 * @return CommandResult
	 */
	protected abstract CommandResult handleIntern(Client client, String[] args);

	/**
	 * @return Command
	 */
	public final String getCommand()
	{
		return _command;
	}

	/**
	 * @param client
	 *            Requestor
	 * @param args
	 *            Command arguments, maybe empty
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

	/**
	 * Checks if the user can use this command.
	 * 
	 * @param client
	 *            Requestor
	 * @return true if User can use this command, false else.
	 */
	public boolean hasClientRights(Client client, int cmdPwr)
	{
		if (cmdPwr < 0)
			return client == null || MyClient.HasGreaterOrEqualCmdPower(client.getServerGroups(), _cmdPwr);
		else
			return client == null || cmdPwr >= _cmdPwr;
	}

	public boolean hasClientRights(Client client)
	{
		return hasClientRights(client, -1);
	}

	/**
	 * @return Full command with arguments.
	 */
	public String getFullCommand()
	{
		String com = "!" + _command;
		if (getArguments() == "")
		{
			return com;
		}
		return String.format("%1$s %2$s", com, getArguments());
	}

	/**
	 * @return commands arguments.
	 */
	public abstract String getArguments();

	/**
	 * @return commands description.
	 */
	public abstract String getDescription();

	/**
	 * @return full formated command with description etc.
	 */
	public final String getDetailedDescription()
	{
		return String.format("!%s:\n%s\n%s\n%s", _command, getDescription(), ColoredText.green(getFullCommand()), getDetails());
	}

	/**
	 * @return Detailed description.
	 */
	protected abstract String getDetails();

	/**
	 * @return needed power to use this command.
	 */
	public final int getCmdPwr()
	{
		return _cmdPwr;
	}
}
