package main.java.de.frigerius.frostbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserDatabase
{
	private static final Logger LOGGER = LoggerFactory.getLogger(UserDatabase.class);

	public static void AddAccountID(String accountId, String nickname, String uid, String server)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			con.setAutoCommit(false);
			insert(con, accountId, nickname, uid, server);
			con.commit();
			con.setAutoCommit(true);

		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
		}
	}

	private static void insert(Connection con, String accountId, String nickname, String uid, String server)
	{
		if (!AddUser(con, uid, nickname))
			return;
		String insertSQL = "INSERT INTO Verifications (AccountID, NumOfRegs, FirstUserUID, Server, LastEdit) VALUES (?, ?, ?, ?, ?) "
				+ "ON DUPLICATE KEY UPDATE NumOfRegs = ?, SecondUserUID = ?, Server = ?, LastEdit = ?";
		try (PreparedStatement insrt = con.prepareStatement(insertSQL))
		{
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
		} catch (SQLException e)
		{
			LOGGER.error("Error on selecting usages.");
		}
	}

	public static boolean AddUser(Connection con, String userUID, String userName)
	{
		try (PreparedStatement insrt = con.prepareStatement("INSERT INTO Users(UserUID, UserName) VALUES (?,?) ON DUPLICATE KEY UPDATE UserName = ?"))
		{
			insrt.setString(1, userUID);
			insrt.setString(2, userName);
			insrt.setString(3, userName);
			insrt.executeUpdate();
		} catch (SQLException e)
		{
			LOGGER.error("Error on selecting usages.");
			return false;
		}
		return true;
	}

	private static int select(Connection con, String id)
	{
		try (PreparedStatement select = con.prepareStatement("SELECT NumOfRegs FROM Verifications WHERE AccountId = ?"))
		{
			select.setString(1, id);
			ResultSet result = select.executeQuery();
			if (result.next())
			{
				return result.getInt(1);
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on selecting usages.");
		}
		return 0;
	}

	public static int getAccountUsages(Connection connection, String id)
	{
		return select(connection, id);
	}
}
