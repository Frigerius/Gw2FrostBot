package main.java.de.frigerius.frostbot.commands;

import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.WvWEvents;

public class ListEventsCommand extends BaseCommand {

	public ListEventsCommand(int cmdPwr) {
		super("listevents", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		List<String> events = WvWEvents.GetCurrentEvents();
		if (events != null && events.size() > 0) {
			_bot.sendBulkMessages(c, ColoredText.green("Es finden folgende Events statt:"), events);
		} else {
			_bot.TS3API.sendPrivateMessage(c.getId(), "Leider finden momentan keine Events statt.");
		}
		return null;
	}

	@Override
	public boolean hasClientRights(Client c, int cmdPwr) {
		return true;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Listet alle aktuell stattfindenden Events auf.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
