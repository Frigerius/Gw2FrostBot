package de.frigerius.frostbot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.BotSettings;
import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.FrostBot;

public class Commands
{
	public final Map<String, BaseCommand> commands = new HashMap<>();
	private final Map<String, BaseCommand> consoleCommands = new HashMap<>();
	public final List<String> sortedCommands = new ArrayList<>();
	private final Pattern _pattern = Pattern.compile("(^(?<command>!([a-z]*(?= |$)))|((?<=[ ])((\"(?<param>(((?!\").)*))\")|(?<param2>(((?!\"|[ ]).)*)))(?=[ |\"]|$)))");
	private final Logger LOGGER = LoggerFactory.getLogger(Commands.class);
	private FrostBot _bot;

	public Commands()
	{
		_bot = FrostBot.getInstance();
	}

	public void init()
	{
		LOGGER.info("Initializing Commands...");
		createDefaultCommands();
		LOGGER.info("Commands Initialized.");
	}

	private void registerCommand(BaseCommand command)
	{
		commands.put("!" + command.getCommand().toLowerCase(), command);
		sortedCommands.add("!" + command.getCommand().toLowerCase());
	}

	private void registerConsoleCommand(BaseCommand command)
	{
		consoleCommands.put(command.getCommand().toLowerCase(), command);
	}

	private void createDefaultCommands()
	{
		registerCommand(new HelpCommand(0, this));
		registerCommand(new InfoCommand("homepage", 0, "[url]" + BotSettings.homepage + "[/url]", "Gibt den Link zur Hompage an."));
		// Verifizierung
		registerCommand(new VerifyCommand(0));
		registerCommand(new ShowVerifier(20));
		registerCommand(new HelpVerify(20));
		registerCommand(new PauseVerifyCommand(20));
		registerCommand(new ResumeVerifyCommand(20));
		// Events
		registerCommand(new ListEventsCommand(0));
		registerCommand(new JoinEventCommand(10));
		registerCommand(new AddEventCommand(15));
		registerCommand(new DeleteEventCommand(15));
		registerCommand(new SetChannelRecording(15));
		// Channel
		registerCommand(new CreateChannelCommand(15));
		registerCommand(new SetupChannelCommand(15));
		// Guild Commands
		registerCommand(new ListGuildsCommand(10));
		registerCommand(new JoinGuildCommand(10));
		registerCommand(new LeaveGuildCommand(11));
		registerCommand(new ListMembersCommand(1));
		registerCommand(new AddMemberCommand(1));
		registerCommand(new AddGuildLeaderCommand(1));
		registerCommand(new KickFromGuildCommand(1));
		registerCommand(new AddGuildCommand(55));

		// AFK
		registerCommand(new AFKCommand(0));
		registerCommand(new BackCommand(0));
		// Mute Bot
		registerCommand(new ShutUpCommand(10));
		registerCommand(new INeedUCommand(10));

		// Console Commands
		registerConsoleCommand(new CreateChannelCommand(0));
		registerConsoleCommand(new StopCommand(0));
		registerConsoleCommand(new ChannelInfoCommand(0));
		registerConsoleCommand(new ServerGroupInfoCommand(0));
		registerConsoleCommand(new ClientInfoCommand(0));
		registerConsoleCommand(new ListAllDataBaseIDCommand(0));
		registerConsoleCommand(new RefreshNewsCommand(0));
	}

	public void sendCommandDescriptionsToClient(Client c)
	{
		for (String com : sortedCommands)
		{
			BaseCommand command = commands.get(com);
			if (command != null && command.hasClientRights(c))
			{
				_bot.TS3API.sendPrivateMessage(c.getId(), String.format("%1$s - %2$s", ColoredText.green(command.getFullCommand()), command.getDescription()));
			}
		}
	}

	public String getDetailedDescription(Client c, String cmd)
	{
		BaseCommand command = commands.get(cmd);
		if (command == null)
			return ColoredText.red("Befehl konnte nicht gefunden werden.");
		if (command.hasClientRights(c))
			return command.getDetailedDescription();
		else
			return ColoredText.red("Leider fehlen dir die nötigen Rechte für diese Information");
	}

	public void handleClientCommand(String cmd, Client c)
	{
		if (c == null)
			return;
		Matcher m = _pattern.matcher(cmd);
		m.find();
		String command = m.group("command");
		if (command == null)
		{
			LOGGER.warn(String.format("%s: %s (No Command Found!)", cmd, c.getNickname()));
			_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Ungültiger Befehl."));
			return;
		}
		String[] args = new String[0];
		List<String> largs = new ArrayList<String>();
		StringBuilder sb = new StringBuilder();
		while (m.find())
		{
			if (m.group("param") != null)
			{
				sb.append(" \"").append(m.group("param")).append("\"");
				largs.add(m.group("param"));
			} else
			{
				sb.append(" \"").append(m.group("param2")).append("\"");
				largs.add(m.group("param2"));
			}
		}
		args = largs.toArray(new String[largs.size()]);
		command = command.toLowerCase();

		LOGGER.info(String.format("%s: %s%s", c.getNickname(), command, sb.toString()));

		BaseCommand baseCommand = null;
		if (commands.containsKey(command))
		{
			baseCommand = commands.get(command);
		}

		if (baseCommand != null)
		{
			BaseCommand.CommandResult result = baseCommand.handle(c, args);
			if (result == BaseCommand.CommandResult.Error)
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Beim ausführen des Befehls ist ein Fehler aufgetreten"));
			if (result == BaseCommand.CommandResult.InvalidPermissions)
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Leider besitzt du nicht die Berechtigung, um diesen Befehl nutzen zu können."));
			if (result == BaseCommand.CommandResult.ArgumentError)
				_bot.TS3API.sendPrivateMessage(c.getId(),
						ColoredText.red("Dein Befehl hat eine ungültige Signatur. Korrekt wäre: ") + ColoredText.green(baseCommand.getFullCommand()));
		} else
		{
			LOGGER.info(String.format("\"Unbekannter Befehl\" will be send to %s.", c.getNickname()));
			_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Unbekannter Befehl."));
		}
	}

	public void handleConsoleCommand(String cmd)
	{
		String command = cmd;
		String[] args = new String[0];
		if (cmd.contains(" "))
		{
			String[] parts = cmd.split(" ");
			command = parts[0];
			args = new String[parts.length - 1];
			System.arraycopy(parts, 1, args, 0, parts.length - 1);

		}
		LOGGER.info("Command: " + cmd);
		command = command.toLowerCase();
		if (consoleCommands.containsKey(command))
		{
			LOGGER.info(consoleCommands.get(command).handle(null, args).toString());
		}
	}
}
