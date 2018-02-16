package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.UserDatabase;

public class BlockRecordChannel extends BaseCommand {
	private final Logger LOGGER = LoggerFactory.getLogger(BlockRecordChannel.class);

	public BlockRecordChannel(int cmdPwr) {
		super("blockrec", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		try (Connection con = FrostBot.getSQLConnection()) {
			UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname());
			String sql = "INSERT INTO RecChannel (ChannelID, UserUID, ChannelState) VALUES (?,?,'BlockRec')";
			try (PreparedStatement stmt = con.prepareStatement(sql)) {
				stmt.setInt(1, client.getChannelId());
				stmt.setString(2, client.getUniqueIdentifier());
				int result = stmt.executeUpdate();
				if (result == 1)
					_bot.TS3API.sendPrivateMessage(client.getId(), "Der Channel ist nun für !setrec gesperrt. Diese Sperre lässt sich mit !remblock wieder aufheben.");
			}

		} catch (MySQLIntegrityConstraintViolationException e) {
			_bot.TS3API.sendPrivateMessage(client.getId(), "Der Channel ist als Record-Channel eingetragen. Bitte benutze !remrec bevor du diesen Channel sperrst.");
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
		return "Verhindert das setzen des Recording-Symbols für diesen Channel mittels !setrec.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
