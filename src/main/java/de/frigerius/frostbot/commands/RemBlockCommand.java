package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.UserDatabase;

public class RemBlockCommand extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(RemBlockCommand.class);

	public RemBlockCommand(int cmdPwr) {
		super("remblock", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		try (Connection con = FrostBot.getSQLConnection()) {
			UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname());
			String sql = "DELETE FROM RecChannel WHERE ChannelID = ? AND ChannelState = 'BlockRec'";
			try (PreparedStatement stmt = con.prepareStatement(sql)) {
				stmt.setInt(1, client.getChannelId());
				int result = stmt.executeUpdate();
				if (result == 1) {
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Die Sperre wurde aufgehoben."));
				} else {
					_bot.TS3API.sendPrivateMessage(client.getId(), "Für deinen Channel ist keine Sperre vorhanden.");
				}
			}

		} catch (SQLException e) {
			LOGGER.error("SQL Error", e);
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
		return "Entfernt den Record-Block vom aktuellen channel.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
