package main.java.de.frigerius.frostbot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.exceptions.ChannelAlreadyHasBotException;

public class ChannelBotCommander {

	private HashMap<String, ChannelBot> _bots = new HashMap<>();
	private HashSet<Integer> _currentBotChannels = new HashSet<>();
	private ReentrantLock _botListLock = new ReentrantLock();

	ChannelBotCommander() {
	}

	public ChannelBot createChannelBot(Client owner, String name, int channelId) throws ChannelAlreadyHasBotException {
		ChannelBot childBot = null;
		_botListLock.lock();
		try {
			if (_bots.containsKey(owner.getUniqueIdentifier())) {
				childBot = _bots.get(owner.getUniqueIdentifier());
			} else {
				if (_currentBotChannels.contains(channelId)) {
					throw new ChannelAlreadyHasBotException();
				}
				childBot = new ChannelBot(owner, name, channelId);
				_currentBotChannels.add(channelId);
				_bots.put(owner.getUniqueIdentifier(), childBot);
			}
		} finally {
			_botListLock.unlock();
		}
		return childBot;
	}

	public void killBot(Client owner) {
		_botListLock.lock();
		try {
			ChannelBot bot = _bots.get(owner.getUniqueIdentifier());
			if (bot != null) {
				bot.exit();
			}
		} finally {
			_botListLock.unlock();
		}
	}

	public void removeBot(ChannelBot bot) {
		_botListLock.lock();
		try {
			_bots.remove(bot.getOwner().getUID());
			_currentBotChannels.remove(bot.getChannelId());
		} finally {
			_botListLock.unlock();
		}
	}

	public void closeAll() {
		for (ChannelBot bot : _bots.values()) {
			bot.exit();
		}
	}
}
