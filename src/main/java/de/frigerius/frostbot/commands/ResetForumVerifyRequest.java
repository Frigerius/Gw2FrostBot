package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;

public class ResetForumVerifyRequest extends CriticalTicketCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(ResetForumVerifyRequest.class);

	public ResetForumVerifyRequest(int cmdPwr)
	{
		super("resetreq", cmdPwr);
	}

	@Override
	protected CommandResult sHandleIntern(Client client, String[] args)
	{
		if (args.length != 2)
			return CommandResult.ArgumentError;
		try
		{
			int ticketID = Integer.parseInt(args[0]);
			String comment = args[1];
			try (Connection con = FrostBot.getSQLConnection())
			{
				if (cancelRequest(con, ticketID))
				{
					String sql = "UPDATE Tickets SET State = 'Rejected', SupporterUID = ?, Comment = ? WHERE TicketID = ? AND (State = 'Open' OR SupporterUID = ?)";
					try (PreparedStatement stmt = con.prepareStatement(sql))
					{
						stmt.setString(1, client.getUniqueIdentifier());
						stmt.setString(2, comment);
						stmt.setInt(3, ticketID);
						stmt.setString(4, client.getUniqueIdentifier());
						if (stmt.executeUpdate() == 1)
						{
							_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Das Ticket wurde erfolgreich angepasst."));
						} else
						{
							_bot.TS3API.sendPrivateMessage(client.getId(),
									ColoredText.red("Fehler beim Versuch das Ticket zu bearbeiten! Stellen sie sicher, dass ihre Eingabe korrekt ist."));
						}
					}
				} else
				{
					_bot.TS3API.sendPrivateMessage(client.getId(),
							ColoredText.red("Fehler! Fehler! Fehler! Hilfe! Feuer! Fehler! Argh! Bist du sicher, dass deine Eingabe korrekt ist?"));
				}
			} catch (SQLException e)
			{
				LOGGER.error("SQL Error", e);
				return CommandResult.Error;
			}
		} catch (NumberFormatException ex)
		{
			return CommandResult.ArgumentError;
		}
		return CommandResult.NoErrors;
	}

	private boolean cancelRequest(Connection con, int ticketID)
	{
		String sql = "SELECT RequestorUID FROM Tickets WHERE TicketID = ?";
		String userUID = "";
		try (PreparedStatement stmt = con.prepareStatement(sql))
		{
			stmt.setInt(1, ticketID);
			ResultSet result = stmt.executeQuery();
			if (result.next())
			{
				userUID = result.getString(1);
			} else
			{
				return false;
			}
		} catch (SQLException e1)
		{
			LOGGER.error("Error requesting UserUID from Ticket", e1);
			return false;
		}

		sql = "UPDATE Verifications SET ForumUserName = NULL, LastEdit = ? WHERE FirstUserUID = ? OR SecondUserUID = ?";
		try (PreparedStatement stmt = con.prepareStatement(sql))
		{
			Date date = new Date();
			Timestamp stamp = new Timestamp(date.getTime());
			stmt.setTimestamp(1, stamp);
			stmt.setString(2, userUID);
			stmt.setString(3, userUID);
			if (stmt.executeUpdate() == 1)
				return true;

		} catch (SQLException e)
		{
			LOGGER.error("Error rejecting Request", e);
		}
		return false;
	}

	@Override
	public String getArguments()
	{
		return "[Ticket-ID] [Kommentar]";
	}

	@Override
	public String getDescription()
	{
		return "Setzt die Verifizierungs-Anfrage f�r das Forum des Tickets zur�ck.";
	}

	@Override
	protected String getDetails()
	{
		return "Beispiel: !resetreq 42 \"Ein Kommentar\"";
	}

}
