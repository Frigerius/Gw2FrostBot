package main.java.de.frigerius.frostbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClient;

public class ListAllDataBaseIDCommand extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(ListAllDataBaseIDCommand.class);

	public ListAllDataBaseIDCommand(int cmdPwr) {
		super("listdbids", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		_bot.TS3API.getDatabaseClients().onSuccess(result -> {
			for (DatabaseClient dbClient : result) {
				LOGGER.info(dbClient.getNickname() + " UID: " + dbClient.getUniqueIdentifier() + " DID: " + dbClient.getDatabaseId());
			}
		});
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
