package de.frigerius.frostbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.DatabaseClientInfo;

import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;
import me.xhsun.guildwars2wrapper.model.v2.guild.Guild;

public class GuildManager
{
	private ArrayList<MyGuild> _guilds;
	private ReentrantLock _lock = new ReentrantLock();
	private ArrayList<String> _printedGuilds;
	private GuildWars2 gw2api;
	private final Logger LOGGER = LoggerFactory.getLogger(GuildManager.class);
	FrostBot _bot;

	public GuildManager()
	{
		gw2api = GuildWars2.getInstance();
		_bot = FrostBot.getInstance();
	}

	public void refresh()
	{
		_lock.lock();
		try
		{
			_guilds = new ArrayList<>();
			loadGuilds();
			_printedGuilds = new ArrayList<>(_guilds.size());
			for (int i = 0; i < _guilds.size(); i++)
			{
				_printedGuilds.add(String.format("%s - %s", i, _guilds.get(i).getName()));
			}
		} finally
		{
			_lock.unlock();
		}
	}

	public void loadGuilds()
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			try (Statement select = con.createStatement())
			{
				ResultSet result = select.executeQuery("SELECT * FROM Guilds ORDER BY GuildName");
				while (result.next())
				{
					_guilds.add(new MyGuild(result.getString("GuildName"), result.getString("GuildID"), result.getInt("SGID")));
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Couldn't connect to Databse.", e);
		}
	}

	public ArrayList<String> getGuilds()
	{
		_lock.lock();
		try
		{
			return _printedGuilds;
		} finally
		{
			_lock.unlock();
		}
	}

	public String addMember(Client me, Client other, String gId, boolean isLeader)
	{
		for (MyGuild guild : _guilds)
		{
			if (guild.getID().equals(gId))
			{
				joinGuildIntern(other, guild, isLeader);
				return guild.getName();
			}
		}
		return "";
	}

	public boolean joinGuild(String apiKey, Client client, int guildId)
	{
		try
		{
			if (guildId >= _guilds.size() || guildId < 0)
				return false;
			Account acc = gw2api.getSynchronous().getAccountInfo(apiKey);
			MyGuild guild = _guilds.get(guildId);
			boolean canAccessGuilds = gw2api.getSynchronous().getAPIInfo(apiKey).getPermissions().contains("guilds");
			if (canAccessGuilds && acc.getGuildLeader().contains(guild.getID()))
			{
				return joinGuildIntern(client, guild, true);
			} else if (acc.getGuilds().contains(guild.getID()))
			{
				return joinGuildIntern(client, guild, false);
			}
		} catch (GuildWars2Exception e)
		{
			LOGGER.error("Error in requesting Account", e);
		}
		return false;
	}

	private boolean joinGuildIntern(Client client, MyGuild guild, boolean isLeader)
	{
		try
		{
			if (isMember(client))
				return false;
			if (_bot.TS3API.addClientToServerGroup(guild.getTsGroupId(), client.getDatabaseId()).get())
			{
				try (Connection con = FrostBot.getSQLConnection())
				{
					UserDatabase.AddUser(con, client.getUniqueIdentifier(), client.getNickname());
					String stmt = "INSERT INTO GuildMembers (UserUID, GuildID, IsLeader) VALUES (?,?,?)";
					try (PreparedStatement insrt = con.prepareStatement(stmt))
					{
						insrt.setString(1, client.getUniqueIdentifier());
						insrt.setString(2, guild.getID());
						insrt.setBoolean(3, isLeader);

						insrt.executeUpdate();
						return true;
					}
				} catch (SQLException e)
				{
					LOGGER.error("Error on adding GuildMember.");
				}
			}
		} catch (InterruptedException e)
		{
			return false;
		}
		return false;
	}

	public boolean setGuildLeader(String guildId, int memberId, boolean isLeader)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			String stmt = "UPDATE GuildMembers SET IsLeader = ? WHERE GuildID = ? AND MemberID = ?";
			try (PreparedStatement update = con.prepareStatement(stmt))
			{
				update.setBoolean(1, isLeader);
				update.setString(2, guildId);
				update.setInt(3, memberId);
				return update.executeUpdate() == 1;
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on adding GuildLeader.");
		}
		return false;
	}

	public boolean isMember(Client client)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			try (PreparedStatement select = con.prepareStatement("SELECT * FROM GuildMembers WHERE UserUID = ?"))
			{
				select.setString(1, client.getUniqueIdentifier());
				return select.executeQuery().next();
			}
		} catch (Exception e)
		{
			LOGGER.error("Error in isMember request.");
		}
		return false;
	}

	public String removeMember(int memberId, String guildID)
	{
		String userUID = null;
		try (Connection con = FrostBot.getSQLConnection())
		{
			String stmt = "SELECT UserUID FROM GuildMembers WHERE MemberID = ? AND GuildID = ?";
			try (PreparedStatement select = con.prepareStatement(stmt))
			{
				select.setInt(1, memberId);
				select.setString(2, guildID);
				ResultSet result = select.executeQuery();
				if (result.next())
				{
					userUID = result.getString("UserUID");
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on selecting GuildMember.");
		}
		if (userUID == null)
			return "";

		DatabaseClientInfo client;
		try
		{
			client = _bot.TS3API.getDatabaseClientByUId(userUID).get();
			if (client != null)
			{
				if (removeUserFromServerGroup(guildID, client.getDatabaseId()))
				{
					removeUserFromGuild(userUID);
					return client.getNickname();
				}
			}
		} catch (InterruptedException e)
		{
			LOGGER.error("Error on accessing Client Database", e);
		}
		return "";
	}

	public void LeaveGuild(Client client)
	{
		String gId = null;
		try (Connection con = FrostBot.getSQLConnection())
		{
			String stmt = "SELECT GuildID FROM GuildMembers WHERE UserUID = ?";
			try (PreparedStatement update = con.prepareStatement(stmt))
			{
				update.setString(1, client.getUniqueIdentifier());
				ResultSet result = update.executeQuery();
				if (result.next())
				{
					gId = result.getString("GuildID");
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on selecting GuildMember.");
		}
		if (gId == null)
			return;
		if (removeUserFromServerGroup(gId, client.getDatabaseId()))
			removeUserFromGuild(client.getUniqueIdentifier());
	}

	private void removeUserFromGuild(String userUID)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			String stmt = "DELETE FROM GuildMembers WHERE UserUID = ?";
			try (PreparedStatement update = con.prepareStatement(stmt))
			{
				update.setString(1, userUID);
				update.executeUpdate();
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on removing user from GuildMember.");
		}
	}

	private boolean removeUserFromServerGroup(String gId, int userDBID)
	{
		for (MyGuild guild : _guilds)
		{
			if (guild.getID().equals(gId))
			{
				try
				{
					return _bot.TS3API.removeClientFromServerGroup(guild.getTsGroupId(), userDBID).get();
				} catch (InterruptedException e)
				{
					LOGGER.error("Error in removing Client from server group", e);
					return false;
				}

			}
		}
		return false;
	}

	public boolean addGuild(String apiKey, String guildNameClean)
	{
		try
		{
			Account acc = gw2api.getSynchronous().getAccountInfo(apiKey);
			List<String> guilds = acc.getGuilds();
			for (String id : guilds)
			{
				Guild guild = gw2api.getSynchronous().getGeneralGuildInfo(id);
				if (guild.getName().equals(guildNameClean))
				{
					String name = String.format("%s [%s]", guild.getName(), guild.getTag());
					int sgId = _bot.TS3API.addServerGroup(guild.getTag()).get();
					if (_bot.addServerGroup(sgId, true, 11))
					{
						addGuildToDB(guild.getId(), sgId, name);
						_bot.TS3API.addServerGroupPermission(sgId, "i_group_needed_modify_power", 10, false, false).get();
						refresh();
						return true;
					}
				}
			}
		} catch (GuildWars2Exception | InterruptedException e)
		{
			LOGGER.error("Error in requesting Account", e);
		}
		return false;
	}

	private void addGuildToDB(String gId, int sgId, String name)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			try (PreparedStatement insrt = con.prepareStatement("INSERT INTO Guilds (GuildID, SGID, GuildName) VALUES (?,?,?)"))
			{
				insrt.setString(1, gId);
				insrt.setInt(2, sgId);
				insrt.setString(3, name);
				insrt.executeUpdate();
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on adding guild.");
		}
	}

	public String getGuildId(Client c)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			try (PreparedStatement insrt = con.prepareStatement("SELECT GuildID, IsLeader FROM GuildMembers WHERE UserUID = ?"))
			{
				insrt.setString(1, c.getUniqueIdentifier());
				ResultSet result = insrt.executeQuery();
				if (result.next())
				{
					if (result.getBoolean("IsLeader"))
					{
						return result.getString("GuildID");
					}
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on adding guild.");
		}
		return "";
	}

	public List<String> getMember(String guildID)
	{
		List<String> member = new LinkedList<String>();
		try (Connection con = FrostBot.getSQLConnection())
		{
			String stmt = "SELECT GuildMembers.MemberID, Users.UserName, GuildMembers.IsLeader From GuildMembers INNER JOIN Users ON GuildMembers.UserUID = Users.UserUID "
					+ "WHERE GuildMembers.GuildID = ?";
			try (PreparedStatement insrt = con.prepareStatement(stmt))
			{
				insrt.setString(1, guildID);
				ResultSet result = insrt.executeQuery();
				while (result.next())
				{
					member.add(String.format("%s - %s" + (result.getBoolean("IsLeader") ? " (Gildenleiter)" : ""), result.getInt("MemberID"), result.getString("UserName")));
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error on adding guild.");
		}
		return member;
	}

	public boolean deleteGuild(int id)
	{
		if (id >= _guilds.size() || id < 0)
			return false;
		String guildID = _guilds.get(id).getID();
		try (Connection con = FrostBot.getSQLConnection())
		{
			String stmt = "SELECT SGID From Guilds Where GuildID = ?";
			try (PreparedStatement select = con.prepareStatement(stmt))
			{
				select.setString(1, guildID);
				ResultSet result = select.executeQuery();
				if (result.next())
				{
					int sg = result.getInt("SGID");
					stmt = "DELETE FROM GuildMembers Where GuildID = ?";
					try (PreparedStatement delMembers = con.prepareStatement(stmt))
					{
						delMembers.setString(1, guildID);
						delMembers.executeUpdate();
					}
					stmt = "DELETE FROM Guilds Where GuildID = ?";
					try (PreparedStatement delGuild = con.prepareStatement(stmt))
					{
						delGuild.setString(1, guildID);
						delGuild.executeUpdate();
					}
					stmt = "Delete FROM ServerGroups WHERE ID = ?";
					try (PreparedStatement delSG = con.prepareStatement(stmt))
					{
						delSG.setInt(1, sg);
						delSG.executeUpdate();
					}
					_bot.removeRank(sg);
					refresh();
					return true;
				} else
					return false;
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error in deleting guild", e);
			return false;
		}
	}

}
