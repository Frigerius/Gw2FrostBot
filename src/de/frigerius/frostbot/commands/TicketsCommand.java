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

public class TicketsCommand extends BaseCommand
{
	private final Logger LOGGER = LoggerFactory.getLogger(TicketsCommand.class);

	public TicketsCommand(int cmdPwr)
	{
		super("tickets", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			String sql = "";
			String filter[] = new String[args.length];
			if (args.length == 1)
			{
				for (int i = 0; i < filter.length; i++)
				{
					switch (args[i])
					{
					case "-o":
						filter[i] = "Open";
						break;
					case "-c":
						filter[i] = "Closed";
						break;
					case "-p":
						filter[i] = "InProgress";
						break;
					case "-r":
						filter[i] = "Rejected";
						break;
					}
				}

				sql = "SELECT TicketID, State, Message, rUsers.UserName, sUsers.UserName "
						+ "FROM Tickets LEFT JOIN Users rUsers ON Tickets.RequestorUID = rUsers.UserUID LEFT JOIN Users sUsers ON Tickets.SupporterUID = sUsers.UserUID "
						+ "WHERE State = ?";
				for (int i = 1; i < filter.length; i++)
				{
					sql += " OR State = ?";
				}
			} else
				sql = "SELECT TicketID, State, Message, rUsers.UserName, sUsers.UserName FROM Tickets LEFT JOIN Users rUsers ON Tickets.RequestorUID = rUsers.UserUID LEFT JOIN Users sUsers ON Tickets.SupporterUID = sUsers.UserUID";

			try (PreparedStatement sel = con.prepareStatement(sql))
			{
				if (filter.length > 0)
				{
					for (int i = 0; i < filter.length; i++)
					{
						sel.setString(i + 1, filter[i]);
					}
				}
				ResultSet set = sel.executeQuery();
				List<String> tickets = new ArrayList<>();
				while (set.next())
				{
					String requestor = set.getString("rUsers.UserName");
					String supporter = set.getString("sUsers.UserName");
					supporter = supporter != null ? String.format(" %s |", supporter) : "";
					String message = set.getString("Message");
					String state = set.getString("State");
					String id = set.getString("TicketID");
					String msg = String.format("ID: %s | %s | %s |%s \"%s\"", id, state, requestor, supporter, message);
					if (msg.length() < 1000)
						tickets.add(msg);
					else
					{
						tickets.add(String.format("ID: %s | %s | %s |%s", id, state, requestor, supporter));
						tickets.add(String.format("\"%s\"", set.getString("Message")));
					}
				}
				if (tickets.size() > 0)
					_bot.sendBulkMessages(client, "Tickets:", tickets);
				else
					_bot.TS3API.sendPrivateMessage(client.getId(), "Es konnten keine Tickets gefunden werden.");
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
		return "[(Filter1)] [(Filter2)] ...";
	}

	@Override
	public String getDescription()
	{
		return "Listed alle Tickets auf. Filteroptionen möglich (s. Details)";
	}

	@Override
	protected String getDetails()
	{
		return "-o = Open\n-c = Closed\n-p = InProgress\n-r = Rejected";
	}

}
