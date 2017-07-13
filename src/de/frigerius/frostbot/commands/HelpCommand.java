package de.frigerius.frostbot.commands;

import java.util.LinkedList;
import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;

public class HelpCommand extends BaseCommand
{
	Commands _commands;

	public HelpCommand(int cmdPwr, Commands commands)
	{
		super("help", cmdPwr);
		_commands = commands;
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		if (args.length == 0)
		{
			makeMessageStuff(c);
			return CommandResult.NoErrors;
		} else if (args.length == 1)
		{
			String cmd = args[0].startsWith("!") ? args[0] : "!" + args[0];
			_bot.TS3API.sendPrivateMessage(c.getId(), _commands.getDetailedDescription(c, cmd));
			return CommandResult.NoErrors;
		}
		return CommandResult.ArgumentError;
	}

	private void makeMessageStuff(Client c)
	{
		List<String> helpMessages = new LinkedList<String>();
		for (String sCmd : _commands.sortedCommands)
		{
			BaseCommand cmd = _commands.commands.get(sCmd);
			if (cmd != null && cmd.hasClientRights(c))
			{
				helpMessages.add(String.format("%1$s - %2$s", ColoredText.green(cmd.getFullCommand()), cmd.getDescription()));
			}
		}
		_bot.sendBulkMessages(c, "Mögliche Befehle sind:", helpMessages);
	}

	@Override
	public boolean hasClientRights(Client c)
	{
		return true;
	}

	@Override
	public String getFormatExtension()
	{
		return "[(Befehlsname)]";
	}

	@Override
	public String getDescription()
	{
		return "Erhalte genauere Informationen zu einem Befehl.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
