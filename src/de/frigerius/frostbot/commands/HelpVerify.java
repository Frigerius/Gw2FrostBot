package de.frigerius.frostbot.commands;

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

	public HelpVerify(int cmdPwr)
	{
		super("helpverify", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (_clientController.isSupportNeeded())
		{
			if (client.getChannelId() != BotSettings.supporterChannelID)
			{
				try
				{
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Ich werde dich nun in den Support-Channel ziehen."));
					_bot.TS3API.moveClient(client.getId(), BotSettings.supporterChannelID);
				} catch (Exception ex)
				{
					LOGGER.error("Error in moving Supporter", ex);
				}
			}

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
			_bot.TS3API.sendPrivateMessage(client.getId(), "Momentan muss niemand verifiziert werden, ich wünsche dir weiterhin einen schönen Tag :)");
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getFormatExtension()
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
		return "";
	}

}
