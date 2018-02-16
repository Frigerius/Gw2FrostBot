package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.FrostBot;

public class ListOpenTicketsCommand extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(ListOpenTicketsCommand.class);

	public ListOpenTicketsCommand(int cmdPwr) {
		super("otickets", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		try (Connection con = FrostBot.getSQLConnection()) {
			String sql = "SELECT TicketID, State, Message FROM Tickets WHERE State = 'Open' OR (State = 'InProgress' AND SupporterUID = ?)";

			try (PreparedStatement sel = con.prepareStatement(sql)) {
				sel.setString(1, client.getUniqueIdentifier());
				ResultSet set = sel.executeQuery();
				List<String> tickets = new ArrayList<>();
				while (set.next()) {
					String id = set.getString("TicketID");
					String state = set.getString("State");
					String message = set.getString("Message");
					String msg = String.format("ID: %s | %s | \"%s\"", id, state, message);
					if (msg.length() < 1000)
						tickets.add(msg);
					else {
						tickets.add(String.format("ID: %s | %s", id, state));
						tickets.add(String.format("\"%s\"", set.getString("Message")));
					}
				}
				if (tickets.size() > 0)
					_bot.sendBulkMessages(client, "Offene Tickets:", tickets);
				else
					_bot.TS3API.sendPrivateMessage(client.getId(), "Aktuell gibt es keine offenen Tickets.");
			}
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
			return CommandResult.Error;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Listed alle offenen Tickets auf.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
