package main.java.de.frigerius.frostbot.commands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;

public class SetAfkRule extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(SetAfkRule.class);

	public SetAfkRule(int cmdPwr) {
		super("setafkrule", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		if (!_bot.getChannelController().isEventChannel(client.getChannelId())) {
			LOGGER.info(String.format("%s was not in an event channel.", client.getNickname()));
			_bot.TS3API.sendPrivateMessage(client.getId(), "Du befindest dich nicht in einem Event-Channel und kannst diesen Befehl daher nicht verwenden.");
			return CommandResult.NoErrors;
		}
		try {
			if (args.length > 1)
				return CommandResult.ArgumentError;
			int rule = -1;
			if (args.length == 1) {
				int tmp = Integer.parseInt(args[0]);
				if (tmp >= 0 && tmp < 4)
					rule = tmp;
			}
			_bot.getAfkMover().SetChannelAfkRule(client.getChannelId(), rule);
			_bot.TS3API.sendPrivateMessage(client.getId(), String.format("Dein aktueller Channel benutzt jetzt folgende AFK-Regel: %s", _bot.getAfkMover().AfkRuleToString(rule)));
		} catch (NumberFormatException e) {
			return CommandResult.ArgumentError;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "[(Regel)]";
	}

	@Override
	public String getDescription() {
		return "Setzt die AFK-Regel für den aktuellen Event-Channel.";
	}

	@Override
	protected String getDetails() {
		// 0 = In, 1 = In&Out, 2 = Out, 3 = JustTime, default=In | Out
		return "Ein Client wird als AFK angesehen, wenn er für eine gewisse Zeit keine Aktion im TS ausgeführt hat. Zusätzlich können folgende Regeln gesetzt werden:"
				+ "\n0 - Kein/Muted Mikro" + "\n1 - Kein/Muted Ausgabe" + "\n2 - Kein/Muted Mikro " + ColoredText.red("UND") + " Ausgabe" + "\n3 - Kein/Muted Mikro "
				+ ColoredText.red("Oder") + " Ausgabe" + "\nKeine/Ungültige Angabe - Keine extra Regel";
	}

}
