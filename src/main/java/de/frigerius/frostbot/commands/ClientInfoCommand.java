package main.java.de.frigerius.frostbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class ClientInfoCommand extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(ClientInfoCommand.class);

	public ClientInfoCommand(int cmdPwr) {
		super("getclinfo", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client cl, String[] args) {
		_bot.TS3API.getClients().onSuccess(result -> {
			for (Client c : result) {
				LOGGER.info(c.getNickname() + " " + c.getId() + " " + c.isServerQueryClient());
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
		// TODO Auto-generated method stub
		return "";
	}

}
