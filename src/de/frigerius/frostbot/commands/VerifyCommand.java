package de.frigerius.frostbot.commands;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.AutomatedVerification;
import de.frigerius.frostbot.BotSettings;
import de.frigerius.frostbot.ClientController;
import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.MyClient;
import de.frigerius.frostbot.MyClient.RequestionState;

public class VerifyCommand extends RequestingBaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(VerifyCommand.class);
	private ClientController _clientController;
	private AutomatedVerification _verifier;

	public VerifyCommand(int cmdPwr)
	{
		super("verify", cmdPwr);
		_clientController = _bot.getClientController();
		_verifier = _bot.getVerifier();
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		String uid = client.getUniqueIdentifier();
		if (AddRequester(uid))
		{
			try
			{
				if (AutomatedVerification.isClientVerified(client))
				{
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Du bist bereits verifiziert."));
				} else
				{
					MyClient myClient = _clientController.findHelpRequester(client.getId());
					if (myClient != null)
					{
						if (myClient.getRequestionState() == RequestionState.ManualVerificationRequested && args.length != 1)
						{
							_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red(
									"Du hast bereits eine Anfrage eingereicht, bitte gedulde dich, bis dir jemand zur Hilfe kommt, oder nutze die automatische Verifizierung (!help verify)."));
							return CommandResult.NoErrors;

						} else if (myClient.getRequestionState() == RequestionState.AutomatedVerificationFailed)
						{
							_bot.TS3API.sendPrivateMessage(client.getId(),
									ColoredText.red("Du hast bereits eine Anfrage eingereicht, bitte gedulde dich, bis dir jemand zur Hilfe kommt."));
							return CommandResult.NoErrors;
						}
						_clientController.removeHelpRequester(client.getId());
					}
					if (args.length == 0)
					{
						handleDirectCall(client);
					} else if (args.length == 1)
					{
						handleVerify(client, args[0]);
					} else
					{
						LOGGER.info(String.format("Argument Fehler, ungülige Anzahl von Argumenten %s", args.length));
						return CommandResult.ArgumentError;
					}

				}
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

	private void handleVerify(Client client, String apikey)
	{
		if (!AutomatedVerification.isValidAPIKey(apikey))
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Dein API-Key ist ungültig, bitte überprüfe deine Eingabe."));
			return;
		}
		int returnCode = _verifier.verify(client, apikey);
		if (returnCode > 0)
		{
			String msg = "";
			if (returnCode == 1)
			{
				_bot.TS3API.sendPrivateMessage(client.getId(),
						ColoredText.red("Leider ist bei der automatisierten Verifizierung ein Fehler aufgetreten, ich werde nun ein Ticket für dich erstellen."));
				msg = ColoredText.green(client.getNickname() + " benötigt Hilfe bei der Verifizierung.%s");
			}
			if (returnCode == 2)
			{
				_bot.TS3API.sendPrivateMessage(client.getId(),
						ColoredText.red("Du hast dich zu oft mit dem selben Account angemeldet. Ich werde nun ein Ticket für dich erstellen."));
				msg = ColoredText.green(client.getNickname() + " hat sich zu oft mit dem selben Account angemeldet und benötigt jetzt deine Hilfe.%s");
			}
			sendMsgToSupporter(client, msg, RequestionState.AutomatedVerificationFailed);
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

	@Override
	public boolean hasClientRights(Client c)
	{
		return MyClient.isInServerGroup(c.getServerGroups(), BotSettings.guestGroup) || MyClient.isInServerGroup(c.getServerGroups(), BotSettings.removeGroupIdOnVerify);
	}

	@Override
	public String getArguments()
	{
		return "[(API-Key)]";
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
				+ ColoredText.red("Der automatische Prozess kann einige Sekunden in Anspruch nehmen!");
	}

}
