package de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.FrostBot;

public class TicketCommand extends BaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(TicketCommand.class);

	public TicketCommand(int cmdPwr)
	{
		super("ticket", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		if (args.length != 1)
			return CommandResult.ArgumentError;
		int ticketID = 0;
		try
		{
			ticketID = Integer.parseInt(args[0]);
		} catch (NumberFormatException e)
		{
			return CommandResult.ArgumentError;
		}
		try (Connection con = FrostBot.getSQLConnection())
		{
			String sql = "SELECT TicketID, State, Message, rUsers.UserName, sUsers.UserName FROM Tickets LEFT JOIN Users rUsers ON Tickets.RequestorUID = rUsers.UserUID LEFT JOIN Users sUsers ON Tickets.SupporterUID = sUsers.UserUID WHERE TicketID = ?";

			try (PreparedStatement sel = con.prepareStatement(sql))
			{
				sel.setInt(1, ticketID);
				ResultSet set = sel.executeQuery();
				if (set.next())
				{

					String requestor = set.getString("rUsers.UserName");
					String supporter = set.getString("sUsers.UserName");
					supporter = supporter != null ? String.format(" %s |", supporter) : "";
					String message = set.getString("Message");
					String state = set.getString("State");
					String id = set.getString("TicketID");
					String msg = String.format("ID: %s | %s | %s |%s \"%s\"", id, state, requestor, supporter, message);
					if (msg.length() < 1000)
						_bot.TS3API.sendPrivateMessage(client.getId(), msg);
					else
					{
						_bot.TS3API.sendPrivateMessage(client.getId(), String.format("ID: %s | %s | %s |%s", id, state, requestor, supporter));
						_bot.TS3API.sendPrivateMessage(client.getId(), String.format("\"%s\"", set.getString("Message")));
					}

				} else
					_bot.TS3API.sendPrivateMessage(client.getId(), "Das gesuchte Ticket existiert nicht.");
			}
		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[ID]";
	}

	@Override
	public String getDescription()
	{
		return "Zeigt das Ticket mit der angegebenen ID an.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
