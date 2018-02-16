package main.java.de.frigerius.frostbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroupClient;

public class MaintenanceBot {
	private final Logger LOGGER = LoggerFactory.getLogger(MaintenanceBot.class);
	private MyConnection _connection;
	private FrostBot _bot;

	public MaintenanceBot(String[] args) {
		LOGGER.info("Starte Wartung...");
		BotSettings.nickName = "MaintenanceBot";
		_connection = new MyConnection(() -> {
		});

		try (Connection con = FrostBot.getSQLConnection()) {
			con.setAutoCommit(false);
			for (String s : args) {

				if (s.equals("fillSg")) {
					FillServerGroupTable();
				}
				if (s.equalsIgnoreCase("setupdb")) {
					SetupDBS(con);
				}
				if (s.equals("resetverifications")) {
					RemoveVerifications(con);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
	}

	private void FillServerGroupTable() {
		_connection.init();
		_bot.TS3API.getServerGroups().onSuccess(result -> {
			PreparedStatement insrt = null;
			try (Connection con = FrostBot.getSQLConnection()) {
				con.setAutoCommit(false);
				insrt = con.prepareStatement("insert into ServerGroups (ID, Name, IsUserRank, CmdPower) values (?, ?, ?, ?)");
				for (ServerGroup sg : result) {
					insrt.setInt(1, sg.getId());
					insrt.setString(2, sg.getName());
					insrt.setBoolean(3, false);
					insrt.setInt(4, 0);
					insrt.addBatch();
				}
				insrt.executeBatch();
				con.commit();

			} catch (SQLException e) {
				LOGGER.error("Couldn't connect to Databse.", e);
			} finally {
				if (insrt != null)
					try {
						insrt.close();
					} catch (SQLException e) {
						e.printStackTrace();
					}
				_bot.QUERY.exit();
			}
		});
	}

	private void SetupDBS(Connection con) {
		try (Statement stmt = con.createStatement()) {
			String createUsers = "CREATE TABLE Users(UserUID VARCHAR(40) NOT NULL PRIMARY KEY, UserName VARCHAR(255) NOT NULL)";
			String createServerGroups = "CREATE TABLE ServerGroups(ID INT(11) NOT NULL PRIMARY KEY, Name VARCHAR(255), IsUserRank BOOLEAN, CmdPower INT(11))";
			String createGuilds = "CREATE TABLE Guilds(GuildID VARCHAR(40) NOT NULL PRIMARY KEY, SGID INT(11), GuildName VARCHAR(40), FOREIGN KEY (SGID) REFERENCES ServerGroups(ID))";
			String createGuildmembers = "CREATE TABLE GuildMembers(MemberID INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, UserUID VARCHAR(40), GuildID VARCHAR(40), IsLeader BOOLEAN, FOREIGN KEY (UserUID) REFERENCES Users(UserUID), FOREIGN KEY (GuildID) REFERENCES Guilds(GuildID))";
			String createEvents = "CREATE TABLE Events(EventID INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, EventName VARCHAR(255), StartTime DATETIME, EndTime DATETIME, ChannelID INT(11), UserUID VARCHAR(40), IsCanceled BOOLEAN, FOREIGN KEY (UserUID) REFERENCES Users(UserUID))";
			String createVerifications = "CREATE TABLE Verifications(AccountID VARCHAR(36) NOT NULL PRIMARY KEY, NumOfRegs INT(11), FirstUserUID VARCHAR(40), SecondUserUID VARCHAR(40), Server VARCHAR(20), LastEdit DATETIME, ForumUserName VARCHAR(50), FOREIGN KEY (FirstUserUID) REFERENCES Users(UserUID), FOREIGN KEY (SecondUserUID) REFERENCES Users(UserUID))";
			String createTickets = "CREATE TABLE Tickets(TicketID INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, RequestorUID VARCHAR(40) NOT NULL, State VARCHAR(15), SupporterUID VARCHAR(40), LastEdit DATETIME, Message VARCHAR(1024), Comment VARCHAR(1024), FOREIGN KEY (RequestorUID) REFERENCES Users(UserUID), FOREIGN KEY (SupporterUID) REFERENCES Users(UserUID))";
			String recChannel = "CREATE TABLE RecChannel(ChannelID INT(11) NOT NULL PRIMARY KEY, UserUID VARCHAR(40) NOT NULL, ChannelState VARCHAR(40) NOT NULL, FOREIGN KEY (UserUID) REFERENCES Users(UserUID))";
			stmt.addBatch(createUsers);
			stmt.addBatch(createServerGroups);
			stmt.addBatch(createGuilds);
			stmt.addBatch(createGuildmembers);
			stmt.addBatch(createEvents);
			stmt.addBatch(createVerifications);
			stmt.addBatch(createTickets);
			stmt.addBatch(recChannel);
			stmt.executeBatch();
			con.commit();
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
	}

	public void RemoveVerifications(Connection con) {
		_connection.init();
		_bot.refreshRankPermissionMaps();
		_bot.TS3API.sendServerMessage("[INFO] Es werden nun die verifizierten Benutzer zurückgesetzt. Während dessen steht euch der Bot nicht zur Verfügung.");
		LOGGER.info("Removing Verifications from Database...");
		String sqlDel = "DELETE FROM Verifications WHERE ForumUserName Is NULL";
		String sqlClear = "UPDATE Verifications SET NumOfRegs = NULL, FirstUserUID = NULL, SecondUserUID = NULL";
		try (Statement stmt = con.createStatement()) {
			int removed = stmt.executeUpdate(sqlDel);
			int updated = stmt.executeUpdate(sqlClear);
			con.commit();
			LOGGER.info(String.format("Removed %d Updated %d", removed, updated));
		} catch (SQLException e) {
			LOGGER.error("Wasn't able to reset Verifications.", e);
		}
		List<Integer> ids = new LinkedList<Integer>(BotSettings.server_groupMap.values());
		ids.add(BotSettings.removeGroupIdOnVerify);
		LOGGER.info("Removing clients from ServerGroups...");
		for (int id : ids) {
			if (_bot.isUserRank(id)) {
				int sgId = id;
				_bot.TS3API.getServerGroupClients(sgId).onSuccess(clients -> {
					LOGGER.info(String.format("Removing clients from ServerGroup %d StartCount %d", sgId, clients.size()));
					for (ServerGroupClient client : clients) {
						_bot.TS3API.removeClientFromServerGroup(sgId, client.getClientDatabaseId()).onFailure(b -> {
							LOGGER.info(String.format("Failed removing Client %s from ServerGroup %d", client.getNickname(), sgId));
						});
					}
				}).onFailure(b -> {
					LOGGER.error(String.format("Failed getting clients from ServerGroup %d", sgId));
				});
			}
		}
		try {
			_bot.TS3API.sendServerMessage(
					"[INFO] Die verifizierten Benutzer wurden erfolgreich zurückgesetzt.\nBitte wende dich mit \"!help verify\" an den FrostBot, um zu erfahren, wie du dich wieder verifizieren kannst.\n").await();
			_bot.QUERY.exit();
		} catch (InterruptedException e) {
			LOGGER.error("WhoAmI Filed", e);
		}

	}

}
