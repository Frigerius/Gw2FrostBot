package main.java.de.frigerius.frostbot;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class AfkMover extends ClientService
{
	private final Logger LOGGER = LoggerFactory.getLogger(AfkMover.class);
	private final ConcurrentHashMap<Integer, MyClient> _afkUsers;
	private final ReentrantLock _lock;
	private final HashSet<Integer> _spectateChannelForAfks = new HashSet<>();
	private FrostBot _bot;
	private ClientController _clientController;

	public AfkMover()
	{
		_bot = FrostBot.getInstance();
		_lock = new ReentrantLock();
		_afkUsers = new ConcurrentHashMap<>();
		_clientController = _bot.getClientController();
	}

	@Override
	void handle(List<Client> clientList)
	{
		for (Client c : clientList)
		{
			MyClient client = _afkUsers.get(c.getId());
			if (client != null)
			{
				handleNotAfk(client, c);
			} else
			{
				handleAfk(c);
			}
		}

	}

	protected void handleAfk(final Client c)
	{
		int channelId = c.getChannelId();
		if (isClientAFK(c))
		{
			_lock.lock();
			try
			{
				if (_spectateChannelForAfks.contains(channelId))
				{
					if (MyClient.isInServerGroup(c.getServerGroups(), BotSettings.afkIgnoreServerGroups))
					{
						return;
					}

					MyClient client = new MyClient(c);
					if (client.getLastChannelId() != BotSettings.afkChannelIDLong)
					{
						MyClient sup = _clientController.getActiveSupporter();
						if (sup != null && sup.getId() == client.getId())
						{
							_clientController.setActiveSupporter(null);
						}
						_afkUsers.put(client.getId(), client);
						_bot.TS3API.moveClient(client.getId(), BotSettings.afkChannelIDLong).onFailure(a -> _afkUsers.remove(c.getId()));
						_bot.TS3API.sendPrivateMessage(client.getId(), "Du wurdest in einen AFk-Channel gezogen.");
					}
					LOGGER.info(c.getNickname() + " is afk.");
				}
			} finally
			{
				_lock.unlock();
			}
		}
	}

	protected void handleNotAfk(MyClient client, Client c)
	{
		if (isClientAFK(c))
			return;
		if (client.getLastChannelId() != -1 && c.getChannelId() == BotSettings.afkChannelIDLong)
		{
			_bot.TS3API.moveClient(client.getId(), client.getLastChannelId());
			_bot.TS3API.sendPrivateMessage(client.getId(), "Du bist nun nicht mehr AFK und wurdest in deinen vorherigen Channel gezogen.");
			LOGGER.info(c.getNickname() + " is back again.");
			client.setLastChannelId(-1);
		}
	}

	private boolean isClientAFK(Client c)
	{
		boolean result = false;
		switch (BotSettings.afkRule)
		{
		case 0:
			result = (c.isInputMuted() || !c.isInputHardware());
			break;
		case 1:
			result = (c.isInputMuted() || !c.isInputHardware()) && (c.isOutputMuted() || !c.isOutputHardware());
			break;
		case 2:
			result = (c.isOutputMuted() || !c.isOutputHardware());
			break;
		default:
			result = (c.isInputMuted() || !c.isInputHardware()) || (c.isOutputMuted() || !c.isOutputHardware());
			break;
		}
		return result && c.getIdleTime() >= BotSettings.afkTime;
	}

	public void refreshAFKChannelList()
	{
		_bot.TS3API.getChannels().onSuccess(channels -> {
			refreshAFKChannelList(channels);
		});
	}

	public void refreshAFKChannelList(List<Channel> channels)
	{
		_lock.lock();
		_spectateChannelForAfks.clear();
		_spectateChannelForAfks.add(BotSettings.supporterChannelID);
		try
		{
			for (int i : BotSettings.afkSpectateChannelIDs)
			{
				_spectateChannelForAfks.add(i);
			}
			for (Channel channel : channels)
			{
				int PID = channel.getParentChannelId();
				if (_spectateChannelForAfks.contains(PID))
					_spectateChannelForAfks.add(channel.getId());
			}
		} finally
		{
			_lock.unlock();
		}
	}

	public void addSpectateChannel(int parentId, int channelId)
	{
		_lock.lock();
		try
		{
			if (_spectateChannelForAfks.contains(parentId))
			{
				_spectateChannelForAfks.add(channelId);
			}
		} finally
		{
			_lock.unlock();
		}
	}

	public void addSpecateChannel(int channelId)
	{
		_lock.lock();
		try
		{
			_spectateChannelForAfks.add(channelId);
		} finally
		{
			_lock.unlock();
		}
	}

	public void removeSpecateChannel(int channelid)
	{
		_lock.lock();
		try
		{
			_spectateChannelForAfks.remove(channelid);
		} finally
		{
			_lock.unlock();
		}
	}

	public void onChannelMoved(int parentId, int channelId)
	{
		_lock.lock();
		try
		{
			if (_spectateChannelForAfks.contains(parentId))
			{
				_spectateChannelForAfks.add(channelId);
			} else
			{
				_spectateChannelForAfks.remove(channelId);
			}
		} finally
		{
			_lock.unlock();
		}
	}

	public void removeClient(int id)
	{
		_afkUsers.remove(id);
	}

	public void refreshAfks(LinkedList<MyClient> stillAFK)
	{
		_afkUsers.clear();
		for (MyClient client : stillAFK)
		{
			_afkUsers.put(client.getId(), client);
		}
	}

	public MyClient getClient(int id)
	{
		return _afkUsers.get(id);
	}

	public boolean isClientAfk(int id)
	{
		return _afkUsers.contains(id);
	}
}
