package de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.FrostBot;
import de.frigerius.frostbot.UserDatabase;

public class ClaimTicketCommand extends CriticalTicketCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(ClaimTicketCommand.class);

	public ClaimTicketCommand(int cmdPwr)
	{
		super("claimticket", cmdPwr);
	}

	@Override
	protected CommandResult sHandleIntern(Client client, String[] args)
	{
		if (args.length != 1)
			return CommandResult.ArgumentError;
		try
		{
			int ticketID = Integer.parseInt(args[0]);
			try (Connection con = FrostBot.getSQLConnection())
			{
				if (!UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname()))
					return CommandResult.Error;
				String sql = "UPDATE Tickets SET State = ?, LastEdit = ?, SupporterUID = ? WHERE TicketID = ? AND State = ?";
				try (PreparedStatement updt = con.prepareStatement(sql))
				{
					updt.setString(1, "InProgress");
					Date date = new Date();
					Timestamp stamp = new Timestamp(date.getTime());
					updt.setTimestamp(2, stamp);
					updt.setString(3, client.getUniqueIdentifier());
					updt.setInt(4, ticketID);
					updt.setString(5, "Open");
					if (updt.executeUpdate() != 1)
					{
						_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Das Ticket konnte leider nicht übernommen werden."));
						return CommandResult.NoErrors;
					}
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Du hast das Ticket erfolgreich übernommen."));
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
		}
	}

	@Override
	public String getArguments()
	{
		return "[Ticket-ID]";
	}

	@Override
	public String getDescription()
	{
		return "Übernimm die Verantwortung für das Ticket mit der angegebenen ID";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
