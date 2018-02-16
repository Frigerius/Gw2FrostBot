package main.java.de.frigerius.frostbot;

import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public interface IChannelBotExtension {
	public void handleClientCommand(String cmd, Client c);

	public void onConnected();

	public void setBot(ChannelBot bot);

	public void handleRawMessage(final TextMessageEvent e);

	public void onBeforeClose();
}
