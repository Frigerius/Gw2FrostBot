package main.java.de.frigerius.frostbot;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.wrapper.Channel;
import com.github.theholywaffle.teamspeak3.api.wrapper.ChannelInfo;

public class ChannelController
{
	private final Logger LOGGER = LoggerFactory.getLogger(FrostBot.class);
	private FrostBot _bot;
	private HashSet<Integer> _eventChannel;
	private ReentrantLock _lock = new ReentrantLock();

	public ChannelController()
	{
		_bot = FrostBot.getInstance();
		_eventChannel = new HashSet<>();
	}

	public void setDefaultPermissions(final ChannelInfo channel)
	{
		setDefaultPermissions(channel.getId());
		if (isEventChannel(channel.getParentChannelId()))
		{
			addEventChannel(channel.getId());
		}
	}

	public void setDefaultPermissions(final int channelId)
	{
		LOGGER.info(String.format("Set ChannelDefault Permissions for: %s", channelId));
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_modify_power", 0);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_delete_power", 60);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_join_power", 10);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_description_view_power", 10);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_permission_modify_power", 60);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_subscribe_power", 7);
	}

	public void refreshChannelList()
	{
		_bot.TS3API.getChannels().onSuccess(channels -> {
			refreshChanelList(channels);
		});
	}

	public void refreshChanelList(List<Channel> channels)
	{
		_lock.lock();
		_eventChannel.clear();
		_eventChannel.add(BotSettings.eventParentChannelId);
		try
		{
			for (Channel channel : channels)
			{
				int PID = channel.getParentChannelId();
				if (_eventChannel.contains(PID))
					_eventChannel.add(channel.getId());
			}

		} finally
		{
			_lock.unlock();
		}
	}

	public void addEventChannel(int channelId)
	{
		_lock.lock();
		try
		{
			_eventChannel.add(channelId);
		} finally
		{
			_lock.unlock();
		}
	}

	public void removeEventChannel(int channelId)
	{
		_lock.lock();
		try
		{
			_eventChannel.remove(channelId);
		} finally
		{
			_lock.unlock();
		}
	}

	public boolean isEventChannel(int channelId)
	{
		_lock.lock();
		try
		{
			return _eventChannel.contains(channelId);
		} finally
		{
			_lock.unlock();
		}
	}
}
