package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class ExitChannelBotCommand extends BaseCommand {

	public ExitChannelBotCommand(int cmdPwr) {
		super("killmybot", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		_bot.getChannelBotCommander().killBot(client);
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Schaltet deinen Channel Bot aus.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
