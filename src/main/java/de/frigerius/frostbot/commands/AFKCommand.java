package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.BotSettings;
import main.java.de.frigerius.frostbot.ClientController;

public class AFKCommand extends BaseCommand {
	ClientController _clientController;

	public AFKCommand(int cmdPwr) {
		super("afk", cmdPwr);
		_clientController = _bot.getClientController();
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		if (args.length == 1) {
			try {
				int time = Integer.parseInt(args[0]);
				if (time <= 15) {
					if (c.getChannelId() != BotSettings.afkChannelID) {
						AddAfk(c);
						_bot.TS3API.moveClient(c.getId(), BotSettings.afkChannelID);
					}
					return CommandResult.NoErrors;
				}
			} catch (NumberFormatException ex) {
				return CommandResult.ArgumentError;
			}
		}
		if (c.getChannelId() != BotSettings.afkChannelIDLong) {
			AddAfk(c);
			_bot.TS3API.moveClient(c.getId(), BotSettings.afkChannelIDLong);
		}
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client c, int cmdPwr) {
		return true;
	}

	private void AddAfk(Client c) {
		_clientController.addAfk(c);
	}

	@Override
	public String getArguments() {
		return "[(Dauer)]";
	}

	@Override
	public String getDescription() {
		return "Zieht dich in den AFK-Channel.";
	}

	@Override
	protected String getDetails() {
		return "Durch Angabe einer Dauer, wirst du in den passenden Channel einsortiert.";
	}

}
