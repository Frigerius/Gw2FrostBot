package de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.FrostBot;

public class ListOpenTicktetsCommand extends BaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(ListOpenTicktetsCommand.class);

	public ListOpenTicktetsCommand(int cmdPwr)
	{
		super("listopentickets", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			String sql = "SELECT * FROM Tickets WHERE State = ? OR (State = ? AND SupporterUID = ?)";
			try (PreparedStatement sel = con.prepareStatement(sql))
			{
				sel.setString(1, "Open");
				sel.setString(2, "InProgress");
				sel.setString(3, client.getUniqueIdentifier());
				ResultSet set = sel.executeQuery();
				List<String> tickets = new ArrayList<>();
				while (set.next())
				{
					tickets.add(String.format("ID: %s | %s | %s", set.getString("TicketID"), set.getString("State"), set.getString("Message")));
				}
				if (tickets.size() > 0)
					_bot.sendBulkMessages(client, "Offene Tickets:", tickets);
				else
					_bot.TS3API.sendPrivateMessage(client.getId(), "Aktuell gibt es keine offenen Tickets.");
			}
		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return null;
	}

	@Override
	public String getArguments()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Listed alle offenen Tickets auf.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
