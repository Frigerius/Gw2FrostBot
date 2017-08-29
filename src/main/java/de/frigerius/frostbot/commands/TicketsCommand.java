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
			String filter[] = null;
			if (args.length == 1)
			{
				filter = new String[args[0].length()];
				char arg[] = args[0].toCharArray();
				for (int i = 0; i < filter.length; i++)
				{
					switch (arg[i])
					{
					case 'o':
						filter[i] = "Open";
						break;
					case 'c':
						filter[i] = "Closed";
						break;
					case 'p':
						filter[i] = "InProgress";
						break;
					case 'r':
						filter[i] = "Rejected";
						break;
					default:
						return CommandResult.ArgumentError;
					}
				}

				sql = "SELECT TicketID, State, Message FROM Tickets WHERE State = ?";
				for (int i = 1; i < filter.length; i++)
				{
					sql += " OR State = ?";
				}
			} else
				sql = "SELECT TicketID, State, Message FROM Tickets";

			try (PreparedStatement sel = con.prepareStatement(sql))
			{
				if (filter != null && filter.length > 0)
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
					String id = set.getString("TicketID");
					String state = set.getString("State");
					String message = set.getString("Message");
					String msg = String.format("ID: %s | %s | \"%s\"", id, state, message);
					if (msg.length() < 1000)
						tickets.add(msg);
					else
					{
						tickets.add(String.format("ID: %s | %s", id, state));
						tickets.add(String.format("\"%s\"", set.getString("Message")));
					}
				}
				if (tickets.size() > 0)
				{
					if (client != null)
						_bot.sendBulkMessages(client, "Tickets:", tickets);
					else
						LOGGER.info(String.format("Tickets:\n%s", String.join("\n", tickets)));

				} else
				{
					if (client != null)
						_bot.TS3API.sendPrivateMessage(client.getId(), "Es konnten keine Tickets gefunden werden.");
					else
						LOGGER.info("Es konnten keine Tickets gefunden werden.");

				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
			return CommandResult.Error;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[(Filter)]";
	}

	@Override
	public String getDescription()
	{
		return "Listed alle Tickets auf. Filteroptionen möglich (s. Details)";
	}

	@Override
	protected String getDetails()
	{
		return "o = Open\nc = Closed\np = InProgress\nr = Rejected\nBeispiel: !tickets op\n!tickets opcr ist äquivalent zu !tickets";
	}

}
