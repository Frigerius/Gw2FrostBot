package main.java.de.frigerius.frostbot;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerGroup;

import main.java.de.frigerius.frostbot.commands.Commands;

public class FrostBot
{
	private final Logger LOGGER = LoggerFactory.getLogger(FrostBot.class);
	private static FrostBot _instance;

	public TS3Query QUERY = null;
	public TS3ApiAsync TS3API = null;
	public int QueryID;

	private MyConnection _connection;
	private Tasks _tasks;
	private Commands _commands;
	private ClientController _clientController;
	private Events _events;
	private Console _console;
	private AfkMover _afkMover;
	private GuildManager _guildManager;
	private ChannelController _channelController;

	private News _news;
	private final HashSet<Integer> _userRanks = new HashSet<>();
	private final HashMap<Integer, Integer> _rankPermissionMap = new HashMap<>();
	private final ReentrantLock _permissionLock = new ReentrantLock();

	private boolean _configurated = false;

	private FrostBot()
	{
		_instance = this;
		_tasks = new Tasks();
		_commands = new Commands();
		_clientController = new ClientController();
		_events = new Events();
		_connection = new MyConnection(() -> onConnected());
		_console = new Console();
		_guildManager = new GuildManager();
		_channelController = new ChannelController();
		_news = new News("news.txt");
	}

	public static FrostBot getInstance()
	{
		return _instance == null ? new FrostBot() : _instance;
	}

	public Tasks getTasks()
	{
		return _tasks;
	}

	public Commands getCommands()
	{
		return _commands;
	}

	public ClientController getClientController()
	{
		return _clientController;
	}

	public Events getEvents()
	{
		return _events;
	}

	public AfkMover getAfkMover()
	{
		return _afkMover;
	}

	public GuildManager getGuildManager()
	{
		return _guildManager;
	}

	public News getNews()
	{
		return _news;
	}

	public ChannelController getChannelController()
	{
		return _channelController;
	}

	// Methos

	public boolean readConfig()
	{
		LOGGER.info("Starting to read config-File...");
		File config = new File("config.ini");
		if (!BotSettings.read(config))
			return false;
		LOGGER.info("Config-File loaded.");
		_configurated = true;
		return true;
	}

	public boolean start()
	{
		try
		{
			LOGGER.info("Starting Bot...");
			if (!_configurated)
				return false;
			_guildManager.refresh();
			_commands.init();
			if (BotSettings.isAFKMoverEnabled)
			{
				LOGGER.info("Adding AfkMover to Tasks...");
				_afkMover = new AfkMover();
				_tasks.AddTask(_afkMover);
			}
			_connection.init();
			TS3API.getChannels().onSuccess(channels -> {
				if (BotSettings.isAFKMoverEnabled)
				{
					_afkMover.refreshAFKChannelList(channels);
				}
				_channelController.refreshChannelList(channels);
			});
			Runtime.getRuntime().addShutdownHook(new Thread()
			{
				@Override
				public void run()
				{
					_instance.stop();
				}
			});
			_console.runReadThread();
			LOGGER.info("FrostBot started.");
		} catch (Exception e)
		{
			LOGGER.error("Error on startup.", e);
			return false;
		}
		return true;
	}

	public void stop()
	{
		LOGGER.info("FrostBot is shutting down!");
		try
		{
			for (Client c : TS3API.getClients().get())
			{
				if (BotSettings.notifyUserIDs.contains(c.getUniqueIdentifier()))
				{
					TS3API.sendPrivateMessage(c.getId(), "FrostBot is shutting down!");
				}
			}
		} catch (InterruptedException e)
		{
			LOGGER.info("Could not send shutdown message!");
		}
		_tasks.stopAll();
		QUERY.exit();
	}

	private void onConnected()
	{
		_events.init();
		TS3API.getClients().onSuccess(result -> {
			for (Client c : result)
			{
				if (BotSettings.notifyUserIDs.contains(c.getUniqueIdentifier()))
				{
					TS3API.sendPrivateMessage(c.getId(), "FrostBot connected!");
				}
			}
			refreshRankPermissionMaps();
			LOGGER.info("Starting User refresh...");
			_clientController.refreshUsers(result);
			LOGGER.info("Starting Main Loop...");
			FrostBot.getInstance().getTasks().startMainLoop();
			LOGGER.info("Events are being registered...");
			TS3API.registerAllEvents();
		});
	}

	public static Connection getSQLConnection() throws SQLException
	{
		return DriverManager.getConnection(BotSettings.sqlUrl, BotSettings.sqlUsername, BotSettings.sqlPassword);
	}

	public void sendBulkMessages(Client c, String initmsg, List<String> msgs)
	{
		sendBulkMessages(c.getId(), initmsg, msgs);
	}

	public void sendBulkMessages(int id, String initmsg, List<String> msgs)
	{
		List<String> tmp = new LinkedList<String>();
		int msgSize = 0;
		if (initmsg != null)
		{
			tmp.add(initmsg);
			msgSize = tmp.get(0).length();
		}
		List<String> toSend = new LinkedList<String>();
		for (String msg : msgs)
		{
			if (msgSize + msg.length() + (tmp.size() * 2) < 1024)
			{
				msgSize += msg.length();
			} else
			{
				toSend.add(String.join("\n", tmp));
				tmp.clear();
				tmp.add("");
				msgSize = msg.length();
			}
			tmp.add(msg);
		}
		toSend.add(String.join("\n", tmp));
		for (String msg : toSend)
		{
			if (msg.length() > 0)
				TS3API.sendPrivateMessage(id, msg);
		}
	}

	public void refreshRankPermissionMaps()
	{
		_permissionLock.lock();
		try (Connection con = FrostBot.getSQLConnection())
		{
			try (Statement stmt = con.createStatement())
			{
				ResultSet result = stmt.executeQuery("SELECT * FROM ServerGroups");
				_rankPermissionMap.clear();
				_userRanks.clear();
				while (result.next())
				{
					int id = result.getInt("ID");
					int cmdPower = result.getInt("CmdPower");
					boolean isUserRank = result.getBoolean("IsUserRank");
					_rankPermissionMap.put(id, cmdPower);
					if (isUserRank)
						_userRanks.add(id);
				}
			}
		} catch (SQLException ex)
		{
			LOGGER.error("SQL Error", ex);
		} finally
		{
			_permissionLock.unlock();
		}
	}

	public boolean isUserRank(int rank)
	{
		_permissionLock.lock();
		try
		{
			return _userRanks.contains(rank);
		} finally
		{
			_permissionLock.unlock();
		}
	}

	public int getCmdPower(int rank)
	{
		_permissionLock.lock();
		try
		{
			if (_rankPermissionMap.containsKey(rank))
			{
				return _rankPermissionMap.get(rank);
			}
			return 0;
		} finally
		{
			_permissionLock.unlock();
		}
	}

	private void addRank(int id, boolean isUserRank, int cmdPower)
	{
		_permissionLock.lock();
		try
		{
			_rankPermissionMap.put(id, cmdPower);
			if (isUserRank)
				_userRanks.add(id);
		} finally
		{
			_permissionLock.unlock();
		}
	}

	public void removeRank(int id)
	{
		_permissionLock.lock();
		try
		{
			_rankPermissionMap.remove(id);
			_userRanks.remove(id);
		} finally
		{
			_permissionLock.unlock();
		}
	}

	public boolean addServerGroup(int id, boolean isUserRank)
	{
		return addServerGroup(id, isUserRank, 0);
	}

	public boolean addServerGroup(int id, boolean isUserRank, int cmdPower)
	{
		try
		{
			List<ServerGroup> groups = TS3API.getServerGroups().get();
			for (ServerGroup grp : groups)
			{
				if (grp.getId() == id)
				{
					try (Connection con = getSQLConnection())
					{
						String insertSQL = "INSERT INTO ServerGroups (ID, Name, IsUserRank, CmdPower) VALUES (?, ?, ?, ?) "
								+ "ON DUPLICATE KEY UPDATE Name = ?, IsUserRank = ?, CmdPower = ?";
						try (PreparedStatement stmt = con.prepareStatement(insertSQL))
						{
							stmt.setInt(1, id);
							stmt.setString(2, grp.getName());
							stmt.setBoolean(3, isUserRank);
							stmt.setInt(4, cmdPower);
							stmt.setString(5, grp.getName());
							stmt.setBoolean(6, isUserRank);
							stmt.setInt(7, cmdPower);
							stmt.executeUpdate();
							addRank(id, isUserRank, cmdPower);
						}
					} catch (SQLException ex)
					{
						LOGGER.error("SQL Error", ex);
					}
				}
			}
			return true;
		} catch (InterruptedException e)
		{
			LOGGER.error("InterruptException on add ServerGroups");
			return false;
		}
	}

	public boolean isValidAPIKey(String key)
	{
		int[] lengths = { 8, 4, 4, 4, 20, 4, 4, 4, 12 };
		String[] split = key.split("-");
		if (lengths.length != split.length)
			return false;
		for (int i = 0; i < lengths.length; i++)
		{
			if (split[i].length() != lengths[i])
				return false;
		}
		return true;
	}
}
