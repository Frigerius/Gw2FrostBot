package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import main.java.de.frigerius.frostbot.BotSettings;
import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.UserDatabase;

public class SetChannelRecording extends BaseCommand {

	private final Logger LOGGER = LoggerFactory.getLogger(SetChannelRecording.class);

	public SetChannelRecording(int cmdPwr) {
		super("setrec", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		ChannelInfo channel;
		try {
			channel = _bot.TS3API.getChannelInfo(client.getChannelId()).get();
			LOGGER.info(String.format("%s: %s ID: %s", client.getNickname(), getCommand(), channel.getId()));
		} catch (InterruptedException e1) {
			return CommandResult.Error;
		}
		if (channel.getIconId() == 0) {
			try (Connection con = FrostBot.getSQLConnection()) {
				UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname());
				String sql = "INSERT INTO RecChannel (ChannelID, UserUID, ChannelState) VALUES (?,?,'Recording')";
				try (PreparedStatement stmt = con.prepareStatement(sql)) {
					stmt.setInt(1, client.getChannelId());
					stmt.setString(2, client.getUniqueIdentifier());
					int result = stmt.executeUpdate();
					if (result == 1) {
						Map<ChannelProperty, String> properties = new HashMap<ChannelProperty, String>();
						properties.put(ChannelProperty.CHANNEL_ICON_ID, Long.toString(BotSettings.recordIconId));
						_bot.TS3API.editChannel(client.getChannelId(), properties);
					}
				}

			} catch (MySQLIntegrityConstraintViolationException e) {
				LOGGER.error("SQLIntegrityError", e);
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Dieser Channel kann von dir nicht modifiziert werden."));
			} catch (SQLException e) {
				LOGGER.error("SQL Error", e);
			}

		} else {
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Du kannst Channelsymbole nicht überschreiben."));
		}

		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Stellt den aktuellen Channel als Aufnahme-Channel ein.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
