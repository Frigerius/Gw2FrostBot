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

import main.java.de.frigerius.frostbot.BotSettings;
import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.MyClient;
import main.java.de.frigerius.frostbot.UserDatabase;

public class RemoveRecCommand extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(RemoveRecCommand.class);

	public RemoveRecCommand(int cmdPwr) {
		super("remrec", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {

		ChannelInfo channel;
		try {
			channel = _bot.TS3API.getChannelInfo(client.getChannelId()).get();
		} catch (InterruptedException e) {
			return CommandResult.Error;
		}

		if (channel != null) {
			if (channel.getIconId() == (long) (int) BotSettings.recordIconId) {
				if (MyClient.HasGreaterOrEqualCmdPower(client.getServerGroups(), Commands.Sub1AdminLevel)) {
					return handleAdmin(client);
				} else {
					return handleUser(client);
				}
			} else {
				_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Dein aktueller Channel ist kein Aufnahme-Channel."));
			}
		} else
			return CommandResult.Error;
		return CommandResult.NoErrors;
	}

	private CommandResult handleAdmin(Client client) {
		try (Connection con = FrostBot.getSQLConnection()) {
			String sql = "DELETE FROM RecChannel WHERE ChannelID = ?";
			try (PreparedStatement stmt = con.prepareStatement(sql)) {
				stmt.setInt(1, client.getChannelId());
				int result = stmt.executeUpdate();
				if (result <= 1) {
					Map<ChannelProperty, String> properties = new HashMap<ChannelProperty, String>();
					properties.put(ChannelProperty.CHANNEL_ICON_ID, "0");
					_bot.TS3API.editChannel(client.getChannelId(), properties);
				}
			}

		} catch (SQLException e) {
			LOGGER.error("SQL Error", e);
			return CommandResult.Error;
		}
		return CommandResult.NoErrors;
	}

	private CommandResult handleUser(Client client) {
		try (Connection con = FrostBot.getSQLConnection()) {
			UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname());
			String sql = "DELETE FROM RecChannel WHERE UserUID = ? AND ChannelID = ?";
			try (PreparedStatement stmt = con.prepareStatement(sql)) {
				stmt.setString(1, client.getUniqueIdentifier());
				stmt.setInt(2, client.getChannelId());
				int result = stmt.executeUpdate();
				if (result <= 1) {
					Map<ChannelProperty, String> properties = new HashMap<ChannelProperty, String>();
					properties.put(ChannelProperty.CHANNEL_ICON_ID, "0");
					_bot.TS3API.editChannel(client.getChannelId(), properties);
				} else {
					_bot.TS3API.sendPrivateMessage(client.getId(), "Du hast nicht die Berechtigung das Symbol des Channels zu entfernen");
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
		return "Entfernt das Aufnahme Icon vom aktuellen Channel.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
