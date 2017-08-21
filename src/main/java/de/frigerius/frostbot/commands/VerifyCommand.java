package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.BotSettings;
import main.java.de.frigerius.frostbot.ClientController;
import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.MyClient;
import main.java.de.frigerius.frostbot.UserDatabase;
import main.java.de.frigerius.frostbot.MyClient.RequestionState;
import main.java.de.frigerius.frostbot.commands.Verifier.VerificationResult;
import me.xhsun.guildwars2wrapper.GuildWars2;

public class VerifyCommand extends RequestingBaseCommand
{

	private final Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);
	private ClientController _clientController;
	private Map<Integer, String> _worlds;
	private GuildWars2 gw2api;

	public VerifyCommand(int cmdPwr)
	{
		super("verify", cmdPwr);
		_clientController = _bot.getClientController();
		init();
		gw2api = GuildWars2.getInstance();
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		String uid = client.getUniqueIdentifier();
		if (AddRequester(uid))
		{
			try
			{
				boolean isVerified = isClientVerified(client);
				boolean isForumRequest = args.length == 2 && args[1].length() > 0;
				if (isVerified && !isForumRequest)
				{
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Du bist bereits verifiziert."));
					return CommandResult.NoErrors;
				}
				boolean runningRequest = false;
				if (!isVerified)
				{
					MyClient myClient = _clientController.findHelpRequester(client.getId());
					runningRequest = myClient != null;
					if (runningRequest)
					{
						if (myClient.getRequestionState() == RequestionState.ManualVerificationRequested && args.length == 0)
						{
							LOGGER.warn(String.format("%s: Anfrage läuft bereits.", client.getNickname()));
							_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red(
									"Du hast bereits eine Anfrage eingereicht, bitte gedulde dich, bis dir jemand zur Hilfe kommt, oder nutze die automatische Verifizierung (!help verify)."));
							return CommandResult.NoErrors;
						}
						if (myClient.getRequestionState() == RequestionState.AutomatedVerificationFailed && args.length == 1)
						{
							LOGGER.warn(String.format("%s: Anfrage läuft bereits. (Automated-Failed)", client.getNickname()));
							_bot.TS3API.sendPrivateMessage(client.getId(),
									ColoredText.red("Du hast bereits eine Anfrage eingereicht, bitte gedulde dich, bis dir jemand zur Hilfe kommt."));
							return CommandResult.NoErrors;
						}
						if (args.length == 1)
						{
							_clientController.removeHelpRequester(client.getId());
							runningRequest = false;
						}
					}
				}
				if (args.length == 0)
				{
					handleDirectCall(client);
				} else if (args.length == 1 || args.length == 2)
				{
					_bot.TS3API.sendPrivateMessage(client.getId(), "Ich habe deinen Key erhalten. Bitte habe einen Moment Geduld.");
					Verifier verifier = new Verifier(gw2api, _worlds, client, args[0], isForumRequest ? args[1] : null);
					try (Connection con = FrostBot.getSQLConnection())
					{
						UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname());
						if (!isVerified && !runningRequest)
						{
							if (!handleVerify(verifier.verifyTS(con), client))
								return CommandResult.NoErrors;
						}
						if (isForumRequest)
						{
							handleForumVerify(verifier.verifyForum(con), client, con, args[1]);
						}
					}
				} else
				{
					LOGGER.info(String.format("Argument Fehler, ungülige Anzahl von Argumenten %s", args.length));
					return CommandResult.ArgumentError;
				}

			} catch (Exception e)
			{
				LOGGER.error("Error in Verify", e);
			} finally
			{
				RemoveRequester(uid);
			}
		} else
		{
			LOGGER.info(String.format("%s Anfrage ist in Bearbeitung.", client.getNickname()));
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Deine Anfrage wird bereits verarbeitet, bitte habe etwas Geduld."));
		}
		return CommandResult.NoErrors;
	}

	private boolean handleVerify(VerificationResult result, Client client)
	{
		if (result != VerificationResult.Success)
		{
			String msg = "";
			if (result == VerificationResult.InvalidAPIKey)
			{
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Dein API-Key ist ungültig, bitte überprüfe deine Eingabe."));
				return false;
			}
			if (result == VerificationResult.Failure || result == VerificationResult.APIError)
			{
				LOGGER.warn(String.format("%s: Automatische Verifizierung fehlgeschlagen.", client.getNickname()));
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText
						.red("Leider ist bei der automatisierten Verifizierung ein Fehler aufgetreten, ich werde die zuständige Instanz über dein Anliegen informieren."));
				msg = ColoredText.green(client.getNickname() + " benötigt Hilfe bei der Verifizierung.%s");
			}
			if (result == VerificationResult.TooManyVerifications)
			{
				LOGGER.warn(String.format("%s: Zu häufge Verifizierung.", client.getNickname()));
				_bot.TS3API.sendPrivateMessage(client.getId(),
						ColoredText.red("Du hast dich zu oft mit dem selben Account angemeldet. Ich werde nun ein Ticket für dich erstellen."));
				msg = ColoredText.green(client.getNickname() + " hat sich zu oft mit dem selben Account angemeldet und benötigt jetzt deine Hilfe.%s");
			}
			if (result == VerificationResult.ConnectionError)
			{
				LOGGER.warn(String.format("%s: Server nicht erreichbar.", client.getNickname()));
				_bot.TS3API.sendPrivateMessage(client.getId(),
						ColoredText.red("Der API-Server konnte nicht erreicht werden, bitte versuche es später erneut, oder reiche mit \"!verify\" eine Anfrage ein."));
				return false;
			}
			sendMsgToSupporter(client, msg, RequestionState.AutomatedVerificationFailed);
		}
		return true;

	}

	private void handleForumVerify(VerificationResult result, Client client, Connection con, String fUsername)
	{
		if (result == VerificationResult.Success)
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), "Es wurde für dich ein Ticket für die Verifizierung im Forum erstellt.");
		} else
		{
			if (result == VerificationResult.InvalidAPIKey)
			{
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Dein API-Key ist ungültig, bitte überprüfe deine Eingabe."));
			} else if (result == VerificationResult.Failure || result == VerificationResult.APIError)
			{
				LOGGER.warn(String.format("%s: Automatische Verifizierung fehlgeschlagen. (Forum)", client.getNickname()));
				_bot.TS3API.sendPrivateMessage(client.getId(),
						ColoredText.red("Leider ist bei deiner Anfrage ein Fehler aufgetreten. Ich werde für dein Anliegen ein Ticket erstellen."));
				String sql = "INSERT INTO Tickets (RequestorUID, State, Message, LastEdit) VALUES (?, ?, ?, ?)";
				try (PreparedStatement insrt = con.prepareStatement(sql))
				{
					insrt.setString(1, client.getUniqueIdentifier());
					insrt.setString(2, "Open");
					insrt.setString(3, String.format("Problem bei automatischer Server-Bestätigung. Anfrage wurde von %s für den Nutzernamen %s eingereicht.", client.getNickname(),
							fUsername));
					Date date = new Date();
					Timestamp stamp = new Timestamp(date.getTime());
					insrt.setTimestamp(4, stamp);
					insrt.executeUpdate();

				} catch (SQLException e)
				{
					LOGGER.error("Error adding Ticket", e);
					_bot.TS3API.sendPrivateMessage(client.getId(),
							ColoredText.red("Leider ist ein Fehler bei der generierung deines Tickets aufgetreten. Bitte wende dich persönlich an die zuständige Instanz."));
				}
			} else if (result == VerificationResult.ForumVerificationInProgress)
			{
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Für den Account wurde bereits eine Anfrage auf Verifizierung im Forum eingereicht."));

			} else if (result == VerificationResult.ConnectionError)
			{
				LOGGER.warn(String.format("%s: Server nicht erreichbar. (Forum)", client.getNickname()));
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Der API-Server konnte nicht erreicht werden, bitte versuche es später erneut."));

			}
		}
	}

	private void handleDirectCall(Client client)
	{
		String msg = ColoredText.green(client.getNickname() + " möchte verifiziert werden.%s");
		sendMsgToSupporter(client, msg, RequestionState.ManualVerificationRequested);
	}

	private void sendMsgToSupporter(Client client, String msg, RequestionState state)
	{
		MyClient sup = _clientController.getActiveSupporter();
		if (sup == null)
		{
			Collection<MyClient> supporter = _clientController.getSupporter();
			for (MyClient c : supporter)
			{
				_bot.TS3API.sendPrivateMessage(c.getId(), String.format(msg, " Antworte mir mit !helpverify, wenn du helfen möchtest."));
				_bot.TS3API.pokeClient(c.getId(), "Jemand benötigt Hilfe bei der Verifizierung.");
			}
			if (supporter.size() == 0)
			{
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Momentan steht kein Verifizierer zur Verfügung. Ich melde mich bei dir, sobald sich dies ändert."));
			} else
			{
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Ich habe deine Anfrage eingereicht."));
			}
			_clientController.addHelpRequester(new MyClient(client, state));
		} else
		{
			try
			{
				_bot.TS3API.sendPrivateMessage(sup.getId(), String.format(msg, ""));
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green(String.format("%s wird sich nun um dein Anliegen kümmern.", sup.getName())));
				_bot.TS3API.moveClient(client.getId(), BotSettings.supporterChannelID);
			} catch (Exception ex)
			{
				LOGGER.error("Error in moving client.", ex);
			}
		}
	}

	private boolean isClientVerified(Client client)
	{
		return isClientVerified(client.getServerGroups());
	}

	private boolean isClientVerified(int[] groups)
	{
		return MyClient.isInServerGroup(groups, BotSettings.server_groupMap.values());
	}

	private void init()
	{
		_worlds = new HashMap<>();
		_worlds.put(1001, "Anvil Rock");
		_worlds.put(1002, "Borlis Pass");
		_worlds.put(1003, "Yak's Bend");
		_worlds.put(1004, "Henge of Denravi");
		_worlds.put(1005, "Maguuma");
		_worlds.put(1006, "Sorrow's Furnace");
		_worlds.put(1007, "Gate of Madness");
		_worlds.put(1008, "Jade Quarry");
		_worlds.put(1009, "Fort Aspenwood");
		_worlds.put(1010, "Ehmry Bay");
		_worlds.put(1011, "Stormbluff Isle");
		_worlds.put(1012, "Darkhaven");
		_worlds.put(1013, "Sanctum of Rall");
		_worlds.put(1014, "Crystal Desert");
		_worlds.put(1015, "Isle of Janthir");
		_worlds.put(1016, "Sea of Sorrows");
		_worlds.put(1017, "Tarnished Coast");
		_worlds.put(1018, "Northern Shiverpeaks");
		_worlds.put(1019, "Blackgate");
		_worlds.put(1020, "Ferguson's Crossing");
		_worlds.put(1021, "Dragonbrand");
		_worlds.put(1022, "Kaineng");
		_worlds.put(1023, "Devona's Rest");
		_worlds.put(1024, "Eredon Terrace");
		_worlds.put(2001, "Fissure of Woe");
		_worlds.put(2002, "Desolation");
		_worlds.put(2003, "Gandara");
		_worlds.put(2004, "Blacktide");
		_worlds.put(2005, "Ring of Fire");
		_worlds.put(2006, "Underworld");
		_worlds.put(2007, "Far Shiverpeaks");
		_worlds.put(2008, "Whiteside Ridge");
		_worlds.put(2009, "Ruins of Surmia");
		_worlds.put(2010, "Seafarer's Rest");
		_worlds.put(2011, "Vabbi");
		_worlds.put(2012, "Piken Square");
		_worlds.put(2013, "Aurora Glade");
		_worlds.put(2014, "Gunnar's Hold");
		_worlds.put(2101, "Jade Sea [FR]");
		_worlds.put(2102, "Fort Ranik [FR]");
		_worlds.put(2103, "Augury Rock [FR]");
		_worlds.put(2104, "Vizunah Square [FR]");
		_worlds.put(2105, "Arborstone [FR]");
		_worlds.put(2201, "Kodash [DE]");
		_worlds.put(2202, "Riverside [DE]");
		_worlds.put(2203, "Elona Reach [DE]");
		_worlds.put(2204, "Abaddon's Mouth [DE]");
		_worlds.put(2205, "Drakkar Lake [DE]");
		_worlds.put(2206, "Miller's Sound [DE]");
		_worlds.put(2207, "Dzagonur [DE]");
		_worlds.put(2301, "Baruch Bay [SP]");
	}

	@Override
	public boolean hasClientRights(Client c, int cmdPwr)
	{
		return true;
	}

	@Override
	public String getArguments()
	{
		return "[([url=https://wiki.guildwars2.com/wiki/API:API_key]API-KEY[/url])] [([url=https://abaddons-maul.de/]Abaddon-Forums-Nutzernamen[/url])]";
	}

	@Override
	public String getDescription()
	{
		return "Hilft bei der Verifizierung auf dem TS. \"!help verify\" für mehr Details.";
	}

	@Override
	protected String getDetails()
	{
		return "Für Anfrage einer Verifizierung durch einen Verifizierer, musst du lediglich \"!verify\" eingeben.\n"
				+ "Für die automatische Verifizierung, benötigst du deinen [url=https://wiki.guildwars2.com/wiki/API:API_key]API-KEY[/url]. Diesen kannst du über [url]https://account.arena.net/applications[/url] erstellen.\n"
				+ "Anschließend kannst du den Key als Argument für den Befehl übergeben:\n"
				+ ColoredText.green("Beispiel: !verify XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX") + "\n"
				+ "Durch Übergabe deines [url=https://abaddons-maul.de/]Forums-Nutzernamen[/url], erstellst du eine Anfrage, auch dort verifiziert zu werden:\n"
				+ ColoredText.green("Beispiel: !verify XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX \"Mein Nutzername\"");
	}

}
