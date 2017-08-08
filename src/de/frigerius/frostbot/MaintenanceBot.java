package de.frigerius.frostbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

public class MaintenanceBot
{
	private final Logger LOGGER = LoggerFactory.getLogger(MaintenanceBot.class);
	private MyConnection _connection;
	private FrostBot _bot;

	public MaintenanceBot(String[] args)
	{
		LOGGER.info("Starte Wartung...");
		BotSettings.nickName = "MaintenanceBot";
		_connection = new MyConnection(() -> {
		});
		try (Connection con = FrostBot.getSQLConnection())
		{
			con.setAutoCommit(false);
			for (String s : args)
			{

				if (s.equals("fillSg"))
				{
					FillServerGroupTable();
				}
				if (s.equalsIgnoreCase("setupdb"))
				{
					SetupDBS(con);
				}
				if (s.equals("transfer"))
				{
					CopyVerifications(con);
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
		}
	}

	private void FillServerGroupTable()
	{
		_connection.init();
		_bot.TS3API.getServerGroups().onSuccess(result -> {
			PreparedStatement insrt = null;
			try (Connection con = FrostBot.getSQLConnection())
			{
				con.setAutoCommit(false);
				insrt = con.prepareStatement("insert into ServerGroups (ID, Name, IsUserRank, CmdPower) values (?, ?, ?, ?)");
				for (ServerGroup sg : result)
				{
					insrt.setInt(1, sg.getId());
					insrt.setString(2, sg.getName());
					insrt.setBoolean(3, false);
					insrt.setInt(4, 0);
					insrt.addBatch();
				}
				insrt.executeBatch();
				con.commit();

			} catch (SQLException e)
			{
				LOGGER.error("Couldn't connect to Databse.", e);
			} finally
			{
				if (insrt != null)
					try
					{
						insrt.close();
					} catch (SQLException e)
					{
						e.printStackTrace();
					}
				_bot.QUERY.exit();
			}
		});
	}

	private void SetupDBS(Connection con)
	{
		try (Statement stmt = con.createStatement())
		{
			String createUsers = "CREATE TABLE Users(UserUID VARCHAR(40) NOT NULL PRIMARY KEY, UserName VARCHAR(255) NOT NULL, IgnoreMe BOOLEAN Default FALSE)";
			String createServerGroups = "CREATE TABLE ServerGroups(ID INT(11) NOT NULL PRIMARY KEY, Name VARCHAR(255), IsUserRank BOOLEAN, CmdPower INT(11))";
			String createGuilds = "CREATE TABLE Guilds(GuildID VARCHAR(40) NOT NULL PRIMARY KEY, SGID INT(11), GuildName VARCHAR(40), FOREIGN KEY (SGID) REFERENCES ServerGroups(ID))";
			String createGuildmembers = "CREATE TABLE GuildMembers(MemberID INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, UserUID VARCHAR(40), GuildID VARCHAR(40), IsLeader BOOLEAN, FOREIGN KEY (UserUID) REFERENCES Users(UserUID), FOREIGN KEY (GuildID) REFERENCES Guilds(GuildID))";
			String createEvents = "CREATE TABLE Events(EventID INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, EventName VARCHAR(255), StartTime DATETIME, EndTime DATETIME, ChannelID INT(11), UserUID VARCHAR(40), IsCanceled BOOLEAN, FOREIGN KEY (UserUID) REFERENCES Users(UserUID))";
			String createVerifications = "CREATE TABLE Verifications(AccountID VARCHAR(36) NOT NULL PRIMARY KEY, NumOfRegs INT(11), FirstUserUID VARCHAR(40), SecondUserUID VARCHAR(40), Server VARCHAR(20), LastEdit DATETIME, ForumUserName VARCHAR(50), FOREIGN KEY (FirstUserUID) REFERENCES Users(UserUID), FOREIGN KEY (SecondUserUID) REFERENCES Users(UserUID))";
			String createTickets = "CREATE TABLE Tickets(TicketID INT(11) NOT NULL PRIMARY KEY AUTO_INCREMENT, RequestorUID VARCHAR(40) NOT NULL, State VARCHAR(15), SupporterUID VARCHAR(40), LastEdit DATETIME, Message VARCHAR(1024), Comment VARCHAR(1024), FOREIGN KEY (RequestorUID) REFERENCES Users(UserUID), FOREIGN KEY (SupporterUID) REFERENCES Users(UserUID))";
			stmt.addBatch(createUsers);
			stmt.addBatch(createServerGroups);
			stmt.addBatch(createGuilds);
			stmt.addBatch(createGuildmembers);
			stmt.addBatch(createEvents);
			stmt.addBatch(createVerifications);
			stmt.addBatch(createTickets);
			stmt.executeBatch();
			con.commit();
		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
		}
	}

	private void CopyVerifications(Connection con)
	{
		try (Statement stmt = con.createStatement())
		{
			ResultSet result = stmt.executeQuery("SELECT * FROM old_Verifications");
			try (PreparedStatement insrt = con.prepareStatement("INSERT INTO Verifications (AccountID, NumOfRegs, FirstUserUID, Server, LastEdit) Values (?,?,?,?,?)"))
			{
				try (PreparedStatement insrtUser = con.prepareStatement("INSERT INTO Users(UserUID, UserName) VALUES (?,?) ON DUPLICATE KEY UPDATE UserName = ?"))
				{
					while (result.next())
					{
						insrtUser.setString(1, result.getString("uid"));
						insrtUser.setString(2, result.getString("nickname"));
						insrtUser.setString(3, result.getString("nickname"));
						insrt.setString(1, result.getString("accountid"));
						insrt.setInt(2, result.getInt("numoregs"));
						insrt.setString(3, result.getString("uid"));
						insrt.setString(4, result.getString("server"));
						insrt.setTimestamp(5, result.getTimestamp("lastedit"));
						insrtUser.addBatch();
						insrt.addBatch();
					}
					insrtUser.executeBatch();
					insrt.executeBatch();
					con.commit();
				}
			}

		} catch (SQLException e)
		{
			LOGGER.error("Database error", e);
		}
	}

}
