package main.java.de.frigerius.frostbot.commands;

import java.util.HashMap;
import java.util.Map;

import com.github.theholywaffle.teamspeak3.api.ChannelProperty;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.BotSettings;

public class SetChannelRecording extends BaseCommand
{

	public SetChannelRecording(int cmdPwr)
	{
		super("setrec", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		Map<ChannelProperty, String> properties = new HashMap<ChannelProperty, String>();
		properties.put(ChannelProperty.CHANNEL_ICON_ID, Long.toString(BotSettings.recordIconId));
		_bot.TS3API.editChannel(client.getChannelId(), properties);
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "";
	}

	@Override
	public String getDescription()
	{
		return "Stellt den aktuellen Channel als Aufnahme-Channel ein.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
