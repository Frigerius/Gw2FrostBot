package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.WvWEvents;

public class JoinEventCommand extends BaseCommand {

	public JoinEventCommand(int cmdPwr) {
		super("joinevent", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		if (args.length != 1)
			return CommandResult.ArgumentError;
		int eventId;
		try {
			eventId = Integer.parseInt(args[0]);
		} catch (NumberFormatException ex) {
			return CommandResult.ArgumentError;
		}
		int channelId = WvWEvents.getChannelId(eventId);
		if (channelId > -1) {
			if (channelId == c.getChannelId()) {
				_bot.TS3API.sendPrivateMessage(c.getId(), "Du befindest dich bereits im Event-Channel.");
			} else {
				_bot.TS3API.moveClient(c.getId(), channelId);
			}
		} else {
			_bot.TS3API.sendPrivateMessage(c.getId(), "Das Event, das du suchst ist bereits vorbei oder existierte nie.");
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "[Event-ID]";
	}

	@Override
	public String getDescription() {
		return "Tritt dem Event mir der angegebenen [Event-ID] bei.";
	}

	@Override
	protected String getDetails() {
		return "Beispiel: !joinevent 1";
	}

}
