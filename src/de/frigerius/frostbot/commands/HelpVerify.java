package de.frigerius.frostbot.commands;

import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.BotSettings;
import de.frigerius.frostbot.ClientController;
import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.MyClient;

public class HelpVerify extends BaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(HelpVerify.class);
	private ClientController _clientController;
	private ReentrantLock _lock = new ReentrantLock();

	public HelpVerify(int cmdPwr)
	{
		super("helpverify", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		_lock.lock();
		try
		{
			if (_clientController.isSupportNeeded())
			{
				MyClient sup = _clientController.getSupporter(client.getId());
				if (sup == null)
				{
					sup = new MyClient(client);
					_clientController.addSupporter(sup);
				}
				_clientController.setActiveSupporter(sup);
				if (client.getChannelId() != BotSettings.supporterChannelID)
				{
					try
					{
						_bot.TS3API.moveClient(client.getId(), BotSettings.supporterChannelID);
						// _bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Ich werde dich nun in den Support-Channel ziehen."));
					} catch (Exception ex)
					{
						LOGGER.error("Error in moving Supporter", ex);
						_clientController.setActiveSupporter(null);
						return CommandResult.Error;
					}
				}
				// notify and move requester.
				for (MyClient c : _clientController.getHelpRequests())
				{
					try
					{
						_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.green("Ein Verifizierer steht nun zu deiner Verfügung, ich bringe dich nun in den Support-Channel."));
						_bot.TS3API.moveClient(c.getId(), BotSettings.supporterChannelID);
					} catch (Exception ex)
					{
						LOGGER.error("Error in moving \"Needs Help\"", ex);
					}
				}
				_clientController.clearHelpRequests();
			} else
			{
				MyClient sup = _clientController.getActiveSupporter();
				if (sup == null)
				{
					LOGGER.info("No Verifier needed.");
					_bot.TS3API.sendPrivateMessage(client.getId(), "Momentan muss niemand verifiziert werden, ich wünsche dir weiterhin einen schönen Tag :)");

				} else
				{
					LOGGER.info(String.format("%s is active supporter.", sup.getName()));
					_bot.TS3API.sendPrivateMessage(client.getId(), String.format("%s bearbeitet derzeitige Anfragen.", sup.getName()));
				}
			}
		} finally
		{
			_lock.unlock();
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Akzeptiert alle bestehenden Verifizierungs-Anfragen und bewegt den Verifizierer in den Verifizierungs-Channel.";
	}

	@Override
	protected String getDetails()
	{
		return "Es wird zusätzlich eine Session gestartet, in welcher dich jede weitere Anfrage erreicht, bis du den Verifizierungs-Channel verlässt.";
	}

}
