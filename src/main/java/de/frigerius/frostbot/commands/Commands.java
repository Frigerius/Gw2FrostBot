package main.java.de.frigerius.frostbot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.BotSettings;
import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.commands.BaseCommand.CommandResult;

public class Commands
{
	public final Map<String, BaseCommand> commands = new HashMap<>();
	public final Map<String, BaseCommand> consoleCommands = new HashMap<>();
	public final List<String> sortedCommands = new ArrayList<>();
	public final List<String> sortedConsoleCommands = new ArrayList<>();
	private final Pattern _pattern = Pattern.compile("(^(?<command>!([a-z]*(?= |$)))|((?<=[ ])((\"(?<param>(((?!\").)*))\")|(?<param2>(((?!\"|[ ]).)*)))(?=[ |\"]|$)))");
	private final Pattern _consolePattern = Pattern.compile("(^(?<command>([a-z]*(?= |$)))|((?<=[ ])((\"(?<param>(((?!\").)*))\")|(?<param2>(((?!\"|[ ]).)*)))(?=[ |\"]|$)))");
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
		sortedConsoleCommands.add(command.getCommand().toLowerCase());
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
		registerCommand(new AddMembersCommand(1));
		registerCommand(new AddGuildLeaderCommand(1));
		registerCommand(new KickFromGuildCommand(1));
		registerCommand(new AddGuildCommand(55));
		// Ticket Commands
		registerCommand(new ListOpenTicketsCommand(50));
		registerCommand(new TicketsCommand(50));
		registerCommand(new TicketCommand(50));
		registerCommand(new ClaimTicketCommand(50));
		registerCommand(new CloseTicketCommand(50));
		registerCommand(new SetCommentCommand(50));
		registerCommand(new ResetForumVerifyRequest(50));

		// AFK
		registerCommand(new AFKCommand(0));
		registerCommand(new BackCommand(0));
		// Mute Bot
		registerCommand(new ShutUpCommand(10));
		registerCommand(new INeedUCommand(10));

		// Console Commands
		registerConsoleCommand(new HelpCommand(0, this));
		registerConsoleCommand(new CreateChannelCommand(0));
		registerConsoleCommand(new StopCommand(0));
		registerConsoleCommand(new ChannelInfoCommand(0));
		registerConsoleCommand(new ServerGroupInfoCommand(0));
		registerConsoleCommand(new ClientInfoCommand(0));
		registerConsoleCommand(new ListAllDataBaseIDCommand(0));
		registerConsoleCommand(new RefreshNewsCommand(0));
		registerConsoleCommand(new ListGuildsCommand(0));
		registerConsoleCommand(new DeleteGuildCommand(0));
	}

	public String getDetailedDescription(Client c, String cmd)
	{
		BaseCommand command = commands.get(cmd);
		if (command == null)
			return ColoredText.red("Befehl konnte nicht gefunden werden.");
		if (command.hasClientRights(c))
			return command.getDetailedDescription();
		else
			return ColoredText.red("Leider fehlen dir die nötigen Rechte für diese Information.");
	}

	public void handleClientCommand(String cmd, Client c)
	{
		if (c == null)
			return;
		Matcher m = _pattern.matcher(cmd);
		String command = extractCommand(m);
		if (command == null)
		{
			LOGGER.warn(String.format("%s: %s (No Command Found!)", c.getNickname(), cmd));
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
			if (result == CommandResult.Error)
			{
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Beim ausführen des Befehls ist ein Fehler aufgetreten"));
			}
			if (result == CommandResult.InvalidPermissions)
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Leider besitzt du nicht die Berechtigung, um diesen Befehl nutzen zu können."));
			if (result == CommandResult.ArgumentError)
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Dein Befehl hat eine ungültige Signatur.\n") + ColoredText.green(baseCommand.getDetailedDescription()));
			LOGGER.info(String.format("%s: %s%s (%s)", c.getNickname(), command, sb.toString(), result));
		} else
		{
			LOGGER.info(String.format("%s: %s%s (%s)", c.getNickname(), command, sb.toString(), "Unknown Command"));
			_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Unbekannter Befehl."));
		}
	}

	private String extractCommand(Matcher m)
	{
		if (m.find())
			return m.group("command");
		else
			return null;
	}

	public void handleConsoleCommand(String cmd)
	{
		Matcher m = _consolePattern.matcher(cmd);
		String command = extractCommand(m);
		if (command == null)
		{
			return;
		}
		String[] args = new String[0];
		List<String> largs = new ArrayList<String>();
		while (m.find())
		{
			if (m.group("param") != null)
			{
				largs.add(m.group("param"));
			} else
			{
				largs.add(m.group("param2"));
			}
		}
		args = largs.toArray(new String[largs.size()]);
		command = command.toLowerCase();
		if (consoleCommands.containsKey(command))
		{
			LOGGER.info(consoleCommands.get(command).handle(null, args).toString());
		}
	}
}
