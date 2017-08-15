package de.frigerius.frostbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class ClientController
{
	private final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);
	private FrostBot _bot;
	private final ConcurrentHashMap<Integer, MyClient> _supporter = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, MyClient> _needsSupport = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, MyClient> _afkCmdMap = new ConcurrentHashMap<>();
	private MyClient _activeSupporter = null;
	private ReentrantLock _activeSupLock = new ReentrantLock();

	public ClientController()
	{
		_bot = FrostBot.getInstance();
	}

	public void clientJoined(ClientJoinEvent clientEvent)
	{
		if (checkVerify(clientEvent))
		{
			// Logger.info(String.format("Welcome message will be send to %s : %s.", e.getClientId(), e.getClientNickname()));
			_bot.TS3API.sendPrivateMessage(clientEvent.getClientId(), makeVerifyMessage()).onFailure(result -> {
				LOGGER.error(String.format("Failed to send message to %s.", clientEvent.getClientNickname()));
			});
		} else
		{
			List<String> msgs = new LinkedList<String>();
			if (!MyClient.isInServerGroup(clientEvent.getClientServerGroups(), BotSettings.ignoreMeGroup))
			{
				String msg = _bot.getNews().getMsg();
				msgs.add(String.format("Willkommen auf dem TeamSpeak-Server von %s. Schreibe !help, wenn du eine Übersicht über deine Befehle erhalten möchtest.%s",
						BotSettings.serverName, msg.equals("") ? "" : "\n" + msg));
			}

			if (MyClient.isInServerGroup(MyClient.makeStringToServerGroups(clientEvent.getClientServerGroups()), BotSettings.supporterGroups))
			{
				addSupporter(new MyClient(clientEvent));
			}

			int eventCount = WvWEvents.GetCurrentEventCount();
			if (eventCount > 1)
			{
				msgs.add(String.format("Aktuell finden " + ColoredText.green("%d") + " Events statt. Schreibe " + ColoredText.green("!listevents") + " für mehr Details.",
						eventCount));
			} else if (eventCount == 1)
			{
				msgs.add(String.format("Aktuell findet " + ColoredText.green("ein") + " Event statt. Schreibe " + ColoredText.green("!listevents") + " für mehr Details.",
						eventCount));
			}

			if (MyClient.HasGreaterOrEqualCmdPower(MyClient.makeStringToServerGroups(clientEvent.getClientServerGroups()), 45))
			{
				int count = checkTickets(clientEvent);
				if (count > 0)
					msgs.add(String.format("Anzahl offener Tickets: [color=green]%s[/color]", count));
			}
			_bot.sendBulkMessages(clientEvent.getClientId(), null, msgs);
		}
	}

	private int checkTickets(ClientJoinEvent clientEvent)
	{
		try (Connection con = FrostBot.getSQLConnection())
		{
			String sql = "SELECT Count(*) FROM Tickets WHERE State = 'Open' OR (State = 'InProgress' AND SupporterUID = ?)";
			try (PreparedStatement stmt = con.prepareStatement(sql))
			{
				stmt.setString(1, clientEvent.getUniqueClientIdentifier());
				ResultSet result = stmt.executeQuery();
				if (result.next())
				{
					return result.getInt(1);
				}
			}
		} catch (SQLException e)
		{
			LOGGER.error("Error requesting open Tickets", e);
		}
		return 0;
	}

	public void clientLeft(ClientLeaveEvent e)
	{
		int id = e.getClientId();
		_needsSupport.remove(id);
		AfkMover afkMover = _bot.getAfkMover();
		if (afkMover != null)
			afkMover.removeClient(id);
		_supporter.remove(id);
		_afkCmdMap.remove(id);
		_activeSupLock.lock();
		try
		{
			if (_activeSupporter.getId() == id)
				_activeSupporter = null;
		} finally
		{
			_activeSupLock.unlock();
		}
	}

	public static boolean checkVerify(ClientJoinEvent clientEvent)
	{
		return checkVerify(MyClient.makeStringToServerGroups(clientEvent.getClientServerGroups()));
	}

	private static boolean checkVerify(int[] groups)
	{
		return MyClient.isInServerGroup(groups, BotSettings.guestGroup) || MyClient.isInServerGroup(groups, BotSettings.removeGroupIdOnVerify);
	}

	private String makeVerifyMessage()
	{
		return String.format("Willkommen auf dem TeamSpeak-Server von %s.\n"
				+ "Für den Erhalt einer Servergruppe hast du zwei Möglichkeiten, entweder lässt du dich von einem Verifizierer verifizieren, "
				+ "oder du nutzt die automatische Verifizierung. Für mehr Infos, tippe bitte den Befehl \"!help verify\" ein.", BotSettings.serverName);
	}

	public void refreshUsers(List<Client> clients)
	{
		_supporter.clear();
		LinkedList<MyClient> stillAFK = new LinkedList<>();
		LinkedList<MyClient> needsHelp = new LinkedList<>();
		AfkMover afkMover = _bot.getAfkMover();
		for (Client client : clients)
		{
			String UID = client.getUniqueIdentifier();
			if (!UID.equals(BotSettings.myUID))
			{
				int clientId = client.getId();

				if (afkMover != null)
				{
					MyClient c = afkMover.getClient(clientId);
					if (c != null)
					{
						if (c.getUID().equals(UID))
							stillAFK.add(c);
					}
				}
				if (_needsSupport.containsKey(client.getId()))
				{
					MyClient c = _needsSupport.get(clientId);
					if (c.getUID().equals(UID))
						needsHelp.add(c);
				}
				if (BotSettings.supporterGroups.size() > 0)
				{
					if (MyClient.isInServerGroup(client.getServerGroups(), BotSettings.supporterGroups))
					{
						MyClient toAdd = new MyClient(client);
						_supporter.put(toAdd.getId(), toAdd);
					}
				}
			}
		}
		afkMover.refreshAfks(stillAFK);
		_needsSupport.clear();
		for (MyClient client : needsHelp)
		{
			_needsSupport.put(client.getId(), client);
		}
	}

	public void addSupporter(MyClient c)
	{
		_supporter.put(c.getId(), c);
		// Logger.info(c.getName() + " was added to List of supporters.");
		int helpSize = _needsSupport.size();
		if (helpSize > 0)
		{
			String msg = "";
			if (helpSize == 1)
			{
				msg = String.format("%s benötigt deine Hilfe.", ColoredText.green(_needsSupport.values().iterator().next().getName()));
			} else
			{
				msg = String.format("Es benötigen %s Personen deine Hilfe.", ColoredText.green("" + _needsSupport.size()));
			}
			_bot.TS3API.sendPrivateMessage(c.getId(), String.format("Hallo %s. %s Bitte antworte mir mit !helpverify, wenn du gerade Zeit hast.", c.getName(), msg));
		}
	}

	public void removeSupporter(int id)
	{
		_supporter.remove(id);
	}

	public void addAfk(MyClient client)
	{
		_afkCmdMap.put(client.getId(), client);
	}

	public void addAfk(Client client)
	{
		if (_afkCmdMap.containsKey(client.getId()))
		{
			_afkCmdMap.get(client.getId()).setLastChannelId(client.getChannelId());
		} else
		{
			_afkCmdMap.put(client.getId(), new MyClient(client));
		}
	}

	public MyClient removeAfk(int id)
	{
		return _afkCmdMap.remove(id);
	}

	public boolean isSupportNeeded()
	{
		return _needsSupport.size() > 0;
	}

	public Collection<MyClient> getHelpRequests()
	{
		return _needsSupport.values();
	}

	public void clearHelpRequests()
	{
		_needsSupport.clear();
	}

	public Collection<MyClient> getSupporter()
	{
		return _supporter.values();
	}

	public MyClient findHelpRequester(int id)
	{
		return _needsSupport.get(id);
	}

	public void removeHelpRequester(int id)
	{
		_needsSupport.remove(id);
	}

	public void addHelpRequester(MyClient client)
	{
		_needsSupport.put(client.getId(), client);
	}

	public MyClient getActiveSupporter()
	{
		_activeSupLock.lock();
		try
		{
			return _activeSupporter;
		} finally
		{
			_activeSupLock.unlock();
		}
	}

	public void setActiveSupporter(MyClient client)
	{
		_activeSupLock.lock();
		try
		{
			_activeSupporter = client;
		} finally
		{
			_activeSupLock.unlock();
		}
	}

	public MyClient getSupporter(int id)
	{
		return _supporter.get(id);
	}

}
