package main.java.de.frigerius.frostbot.commands;

import java.util.HashMap;
import java.util.Map;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.AfkMover;
import main.java.de.frigerius.frostbot.BotSettings;

public class CreateChannelCommand extends BaseCommand {
	private AfkMover _afkMover;

	public CreateChannelCommand(int cmdPwr) {
		super("createchannel", cmdPwr);
		_afkMover = _bot.getAfkMover();
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		if (args.length != 1)
			return CommandResult.ArgumentError;
		if (BotSettings.eventParentChannelId != -1) {
			if (args[0].length() > 40) {
				_bot.TS3API.sendPrivateMessage(c.getId(), "Bitte wähle einen kürzeren Namen, es dürfen maximal 40 Zeichen verwendet werden.");
				return CommandResult.NoErrors;
			}
			Map<ChannelProperty, String> properties = new HashMap<ChannelProperty, String>();
			properties.put(ChannelProperty.CHANNEL_FLAG_PERMANENT, "1");
			_bot.TS3API.createChannel(args[0], properties).onSuccess(id -> {
				_bot.TS3API.getChannels().onSuccess(result -> {
					Channel subChannel = null;
					for (Channel channel : result) {
						if (channel.getParentChannelId() == BotSettings.eventParentChannelId) {
							subChannel = channel;
						}
					}
					if (_afkMover != null) {
						_afkMover.addSpecateChannel(id);
					}
					if (subChannel != null) {
						_bot.TS3API.moveChannel(id, BotSettings.eventParentChannelId, subChannel.getId());
					} else {
						_bot.TS3API.moveChannel(id, BotSettings.eventParentChannelId);
					}
					if (c != null)
						_bot.TS3API.sendPrivateMessage(c.getId(), "Ich habe den Channel für dich erstellt.");
				});
			});
		}
		return CommandResult.NoErrors;

	}

	@Override
	public String getArguments() {
		return "[Channel name]";
	}

	@Override
	public String getDescription() {
		return "Erstellt einen Channel als letztes Kind vom EventChannel und stellt dessen Rechte ein.";
	}

	@Override
	protected String getDetails() {
		return "Der Name darf 40 Zeichen nicht überschreiten.\nBeispiel1: !createchannel MeinEventChannel\nBeispiel2: !createchannel \"MeinEventChannel mit mehr Wörtern\"";
	}

}
