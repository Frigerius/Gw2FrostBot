package de.frigerius.frostbot;

public class ChannelController
{
	private static ChannelController _instance;
	private FrostBot _bot;
	
	private ChannelController()
	{
		_instance = this;
		_bot = FrostBot.getInstance();
	}

	public static ChannelController getInstance()
	{
		return _instance == null ? new ChannelController() : _instance;
	}
	
	public void setDefaultPermissions(int channelId)
	{
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_modify_power", 0);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_delete_power", 60);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_join_power", 10);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_description_view_power", 10);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_permission_modify_power", 60);
		_bot.TS3API.addChannelPermission(channelId, "i_channel_needed_subscribe_power", 7);
	}
}
