package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.UserDatabase;

public class CloseTicketCommand extends CriticalTicketCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(CloseTicketCommand.class);

	public CloseTicketCommand(int cmdPwr) {
		super("closeticket", cmdPwr);
	}

	@Override
	protected CommandResult sHandleIntern(Client client, String[] args) {
		if (args.length < 1 || args.length > 2)
			return CommandResult.ArgumentError;
		try {
			int ticketID = Integer.parseInt(args[0]);
			try (Connection con = FrostBot.getSQLConnection()) {
				if (!UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname()))
					return CommandResult.Error;
				String sql = "UPDATE Tickets SET State = 'Closed', LastEdit = ?, SupporterUID = ?, Comment = ? WHERE TicketID = ? AND (State = 'Open' OR (State = 'InProgress' AND SupporterUID = ?))";
				try (PreparedStatement updt = con.prepareStatement(sql)) {
					Date date = new Date();
					Timestamp stamp = new Timestamp(date.getTime());
					updt.setTimestamp(1, stamp);
					updt.setString(2, client.getUniqueIdentifier());
					updt.setString(3, args.length == 2 ? args[1] : "");
					updt.setInt(4, ticketID);
					updt.setString(5, client.getUniqueIdentifier());
					if (updt.executeUpdate() == 0) {
						_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Leider konnte das Ticket nicht geschlossen werden. Bist du sicher, dass es dir gehört?"));
						return CommandResult.NoErrors;
					}
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Du hast das Ticket erfolgreich geschlossen."));
					return CommandResult.NoErrors;

				}
			} catch (SQLException ex) {
				LOGGER.error("Error in claiming Ticket.", ex);
				return CommandResult.Error;
			}
		} catch (NumberFormatException ex) {
			return CommandResult.ArgumentError;
		}
	}

	@Override
	public String getArguments() {
		return "[TicketID] [(Kommentar)]";
	}

	@Override
	public String getDescription() {
		return "Schließe das Ticket mit der angegebenen ID. Füge optional einen Kommentar hinzu.";
	}

	@Override
	protected String getDetails() {
		return ColoredText.green("Beispiel: !closeticket 42 \"Ein Kommentar\"");
	}

}
