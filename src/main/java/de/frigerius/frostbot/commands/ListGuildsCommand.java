package main.java.de.frigerius.frostbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class ListGuildsCommand extends BaseGuildCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(ListGuildsCommand.class);

	public ListGuildsCommand(int cmdPwr) {
		super("listguilds", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		if (client != null)
			_bot.sendBulkMessages(client, "Es stehen folgende Gilden zur Verfügung:", _guildManager.getGuilds());
		else {
			for (String s : _guildManager.getGuilds())
				LOGGER.info(s);
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Listet alle Gilden auf, für die es eine ServerGruppe gibt.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
