package main.java.de.frigerius.frostbot.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.BotSettings;
import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.MyClient;
import main.java.de.frigerius.frostbot.UserDatabase;
import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.ErrorCode;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;

public class Verifier {
	public enum VerificationResult {
		Success, TooManyVerifications, ConnectionError, APIError, Failure, ForumVerificationInProgress, InvalidAPIKey
	}

	private final Logger LOGGER = LoggerFactory.getLogger(Verifier.class);
	private GuildWars2 _gw2api;
	private FrostBot _bot;
	private String _apiKey;
	private String _fUserName;
	private Account _acc;
	private boolean _isVerified = false;
	private Map<Integer, String> _worlds;
	private Client _client;
	private String _worldName;

	public Verifier(GuildWars2 gw2api, Map<Integer, String> worlds, Client client, String apikey, String fUserName) {
		_bot = FrostBot.getInstance();
		_gw2api = gw2api;
		_worlds = worlds;
		_apiKey = apikey;
		_fUserName = fUserName;
		_isVerified = MyClient.isInServerGroup(client.getServerGroups(), BotSettings.server_groupMap.values());
		_client = client;
	}

	private VerificationResult requestAPI() {
		if (_acc == null) {
			try {
				if (!_bot.isValidAPIKey(_apiKey)) {
					LOGGER.warn("API-Key ist ungültig.");
					return VerificationResult.InvalidAPIKey;
				}
				LOGGER.info(String.format("Api wird angefragt für %s", _client.getNickname()));
				_acc = _gw2api.getSynchronous().getAccountInfo(_apiKey);
				LOGGER.info(String.format("Anfrage der API abgeschlossen für %s", _client.getNickname()));
				_worldName = _worlds.get(_acc.getWorldId());

			} catch (GuildWars2Exception ex) {
				if (ex.getErrorCode() == ErrorCode.Server) {
					return VerificationResult.ConnectionError;
				}
				if (ex.getErrorCode() == ErrorCode.Network) {
					return VerificationResult.ConnectionError;
				}
				if (ex.getErrorCode() == ErrorCode.Other && ex.getMessage() == "Endpoint not available") {
					return VerificationResult.ConnectionError;
				} else {

					LOGGER.error("Error in AutomatedVerification", ex);
					return VerificationResult.APIError;
				}
			}
		}
		return VerificationResult.Success;
	}

	public boolean isVerified() {
		return _isVerified;
	}

	public VerificationResult verifyForum(Connection con) throws SQLException {
		VerificationResult requestResult = requestAPI();
		if (requestResult == VerificationResult.Success) {
			if (!isForumVerificationRequested(con))
				return CreateForumVerificationRequest(con);
			else
				return VerificationResult.ForumVerificationInProgress;
		} else {
			return requestResult;
		}
	}

	public boolean isForumVerificationRequested(Connection con) throws SQLException {
		String sql = "SELECT ForumUserName FROM Verifications WHERE AccountID = ?";
		try (PreparedStatement stmt = con.prepareStatement(sql)) {
			stmt.setString(1, _acc.getId());
			ResultSet result = stmt.executeQuery();
			if (result.next()) {
				String name = result.getString("ForumUserName");
				if (name != null && !name.equals("")) {
					return true;
				}
			}
		}
		return false;
	}

	public VerificationResult CreateForumVerificationRequest(Connection con) {
		VerificationResult result = requestAPI();
		if (result == VerificationResult.Success) {
			if (insertForumUserName(con, _acc.getId(), _worldName, _fUserName))
				return addTicket(con, _client, _worldName, _fUserName) ? VerificationResult.Success : VerificationResult.Failure;
		} else
			return result;
		return VerificationResult.Failure;
	}

	private boolean insertForumUserName(Connection con, String accountId, String server, String forumUserName) {
		String insertSQL = "INSERT INTO Verifications (AccountID, Server, LastEdit, ForumUserName) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE Server = ?, LastEdit = ?, ForumUserName = ?";
		try (PreparedStatement insrt = con.prepareStatement(insertSQL)) {
			insrt.setString(1, accountId);
			insrt.setString(2, server);
			Date date = new Date();
			Timestamp stamp = new Timestamp(date.getTime());
			insrt.setTimestamp(3, stamp);
			insrt.setString(4, forumUserName);
			// Update
			insrt.setString(5, server);
			insrt.setTimestamp(6, stamp);
			insrt.setString(7, forumUserName);
			insrt.executeUpdate();
			return true;
		} catch (SQLException e) {
			LOGGER.error("Error on selecting usages.");
		}
		return false;
	}

	private boolean addTicket(Connection con, Client client, String worldName, String forumUserName) {
		String sql = "INSERT INTO Tickets (RequestorUID, State, Message, LastEdit) VALUES (?, ?, ?, ?)";
		try (PreparedStatement insrt = con.prepareStatement(sql)) {
			insrt.setString(1, client.getUniqueIdentifier());
			insrt.setString(2, "Open");
			insrt.setString(3, String.format("Forums-Verifizierung: Nutzername: %s | Server: %s", forumUserName, worldName));
			Date date = new Date();
			Timestamp stamp = new Timestamp(date.getTime());
			insrt.setTimestamp(4, stamp);
			insrt.executeUpdate();
			return true;
		} catch (SQLException e) {
			LOGGER.error("Error on adding Ticket");
		}
		return false;
	}

	public VerificationResult verifyTS(Connection con) {
		try {
			VerificationResult requestResult = requestAPI();
			if (requestResult == VerificationResult.Success) {
				String accUID = _acc.getId();
				int accUses = UserDatabase.getAccountUsages(con, accUID);
				// Check if request is valid
				if (accUses < 2) {
					if (BotSettings.server_groupMap.containsKey(_worldName)) {
						if (verifyInTs(_client, _worldName) == VerificationResult.Failure)
							return VerificationResult.Failure;
						insertUser(con, accUID, _client.getNickname(), _client.getUniqueIdentifier(), _worldName);
						return VerificationResult.Success;
					}

				} else {
					LOGGER.info(String.format("%s hat sich zu oft registriert.", _client.getNickname()));
					return VerificationResult.TooManyVerifications;
				}
			} else
				return requestResult;
		} catch (Exception e) {
			LOGGER.error("AutomatedVerification failed", e);
		}
		return VerificationResult.Failure;
	}

	private VerificationResult verifyInTs(Client client, String worldName) throws InterruptedException {
		int groupId = BotSettings.server_groupMap.get(worldName);
		if (_bot.isUserRank(groupId)) {
			LOGGER.info(String.format("Adding %s to servergroup %s...", client.getNickname(), worldName));
			if (BotSettings.removeGroupIdOnVerify != -1 && MyClient.isInServerGroup(client.getServerGroups(), BotSettings.removeGroupIdOnVerify)) {
				_bot.TS3API.removeClientFromServerGroup(BotSettings.removeGroupIdOnVerify, client.getDatabaseId());
			}
			boolean result = _bot.TS3API.addClientToServerGroup(groupId, client.getDatabaseId()).get();
			if (result) {
				LOGGER.info(String.format("%s was added to servergroup %s.", client.getNickname(), worldName));
				return VerificationResult.Success;
			}
		}
		return VerificationResult.Failure;
	}

	private void insertUser(Connection con, String accountId, String nickname, String uid, String server) {
		String insertSQL = "INSERT INTO Verifications (AccountID, NumOfRegs, FirstUserUID, Server, LastEdit) VALUES (?, ?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE NumOfRegs = ?, SecondUserUID = ?, Server = ?, LastEdit = ?";
		try (PreparedStatement insrt = con.prepareStatement(insertSQL)) {
			insrt.setString(1, accountId);
			insrt.setInt(2, 1);
			insrt.setString(3, uid);
			insrt.setString(4, server);
			Date date = new Date();
			Timestamp stamp = new Timestamp(date.getTime());
			insrt.setTimestamp(5, stamp);
			insrt.setInt(6, 2);
			insrt.setString(7, uid);
			insrt.setString(8, server);
			insrt.setTimestamp(9, stamp);
			insrt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error on selecting usages.");
		}
	}
}
