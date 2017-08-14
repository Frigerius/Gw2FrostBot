package de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.FrostBot;
import de.frigerius.frostbot.UserDatabase;

public class CloseTicketCommand extends BaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(CloseTicketCommand.class);
	private ReentrantLock _lock = new ReentrantLock();

	public CloseTicketCommand(int cmdPwr)
	{
		super("closeticket", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length < 1 && args.length > 2)
			return CommandResult.ArgumentError;
		_lock.lock();
		try
		{
			int ticketID = Integer.parseInt(args[0]);
			try (Connection con = FrostBot.getSQLConnection())
			{
				if (!UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname()))
					return CommandResult.Error;
				String sql = "UPDATE Tickets SET State = ?, LastEdit = ?, SupporterUID = ?, Comment = ? WHERE TicketID = ? AND (State = ? OR State = ?) AND (SupporterUID = NULL OR SupporterUID = ?)";
				try (PreparedStatement updt = con.prepareStatement(sql))
				{
					updt.setString(1, "Closed");
					Date date = new Date();
					Timestamp stamp = new Timestamp(date.getTime());
					updt.setTimestamp(2, stamp);
					updt.setString(3, client.getUniqueIdentifier());
					updt.setString(4, args.length == 2 ? args[1] : "");
					updt.setInt(5, ticketID);
					updt.setString(6, "Open");
					updt.setString(7, "InProgress");
					updt.setString(8, client.getUniqueIdentifier());
					if (updt.executeUpdate() == 0)
					{
						_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Leider konnte das Ticket nicht geschlossen werden. Bist du sicher, dass es dir gehört?"));
						return CommandResult.NoErrors;
					}
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Du hast das Ticket erfolgreich geschlossen."));
					return CommandResult.NoErrors;

				}
			} catch (SQLException ex)
			{
				LOGGER.error("Error in claiming Ticket.", ex);
				return CommandResult.Error;
			}
		} catch (NumberFormatException ex)
		{
			return CommandResult.ArgumentError;
		} finally
		{
			_lock.unlock();
		}
	}

	@Override
	public String getArguments()
	{
		return "[TicketID] [(Kommentar)]";
	}

	@Override
	public String getDescription()
	{
		return "Schließe das Ticket mit der angegebenen ID. Füge optional einen Kommentar hinzu.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
