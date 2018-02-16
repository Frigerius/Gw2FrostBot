package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class GetAfkRule extends BaseCommand {

	public GetAfkRule(int cmdPwr) {
		super("getafkrule", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		int rule = _bot.getAfkMover().GetChannelAfkRule(client.getChannelId());
		_bot.TS3API.sendPrivateMessage(client.getId(), String.format("Dein aktueller Channel benutzt folgende AFK-Regel: %s", _bot.getAfkMover().AfkRuleToString(rule)));
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Zeigt die aktuell verwendete AFK-Regel des Channels an.";
	}

	@Override
	protected String getDetails() {
		return "Nicht jeder Channel wird vom Bot auf AFKler geprüft.";
	}

}
