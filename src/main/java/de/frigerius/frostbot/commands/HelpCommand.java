package main.java.de.frigerius.frostbot.commands;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.MyClient;

public class HelpCommand extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(HelpCommand.class);
	Commands _commands;

	public HelpCommand(int cmdPwr, Commands commands) {
		super("help", cmdPwr);
		_commands = commands;
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		if (c != null) {
			if (args.length == 0) {
				makeMessageStuff(c);
				return CommandResult.NoErrors;
			} else if (args.length == 1) {
				String cmd = args[0].startsWith("!") ? args[0] : "!" + args[0];
				_bot.TS3API.sendPrivateMessage(c.getId(), _commands.getDetailedDescription(c, cmd));
				return CommandResult.NoErrors;
			}
		} else {
			if (args.length == 0) {
				listConsoleCommands();
				return CommandResult.NoErrors;
			} else if (args.length == 1) {
				BaseCommand cmd = _commands.consoleCommands.get(args[0]);
				if (cmd == null)
					return CommandResult.ArgumentError;
				else
					LOGGER.info(cmd.getDetailedDescription());
				return CommandResult.NoErrors;
			}
		}

		return CommandResult.ArgumentError;
	}

	private void makeMessageStuff(Client c) {
		List<String> helpMessages = new LinkedList<String>();
		for (String sCmd : _commands.sortedCommands) {
			BaseCommand cmd = _commands.commands.get(sCmd);
			if (cmd != null && cmd.hasClientRights(c, MyClient.GetCmdPower(c.getServerGroups()))) {
				helpMessages.add(String.format("%1$s - %2$s", ColoredText.green(cmd.getFullCommand()), cmd.getDescription()));
			}
		}
		_bot.sendBulkMessages(c, "Mögliche Befehle sind:", helpMessages);
	}

	private void listConsoleCommands() {
		for (String sCmd : _commands.sortedConsoleCommands) {
			BaseCommand cmd = _commands.consoleCommands.get(sCmd);
			if (cmd != null) {
				LOGGER.info(String.format("%1$s - %2$s", cmd.getFullCommand(), cmd.getDescription()));
			}
		}
	}

	@Override
	public boolean hasClientRights(Client c) {
		return true;
	}

	@Override
	public String getArguments() {
		return "[(Befehlsname)]";
	}

	@Override
	public String getDescription() {
		return "Erhalte genauere Informationen zu einem Befehl.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
