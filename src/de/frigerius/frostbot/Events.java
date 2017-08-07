package de.frigerius.frostbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.event.ChannelCreateEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDeletedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelDescriptionEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ChannelPasswordChangedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientLeaveEvent;
import com.github.theholywaffle.teamspeak3.api.event.ClientMovedEvent;
import com.github.theholywaffle.teamspeak3.api.event.PrivilegeKeyUsedEvent;
import com.github.theholywaffle.teamspeak3.api.event.ServerEditedEvent;
import com.github.theholywaffle.teamspeak3.api.event.TS3Listener;
import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;

import de.frigerius.frostbot.commands.Commands;
import de.frigerius.frostbot.commands.HelpVerify;

public class Events
{
	private final Logger LOGGER = LoggerFactory.getLogger(Events.class);
	private FrostBot _bot;
	private AfkMover _afkMover;
	private ChannelController _channelController;
	private ClientController _clientController;
	private Commands _commands;
	private HelpVerify _helpVerifyCommand;

	private boolean _init = false;

	public Events()
	{
		_bot = FrostBot.getInstance();
		_afkMover = _bot.getAfkMover();
		_channelController = ChannelController.getInstance();
	}

	public void init()
	{
		if (_init)
			return;
		_init = true;
		_clientController = _bot.getClientController();
		_commands = _bot.getCommands();
		_helpVerifyCommand = (HelpVerify) _commands.commands.get("!helpverify");
		LOGGER.info("Initializing Events...");
		_bot.TS3API.addTS3Listeners(new TS3Listener()
		{
			public void onTextMessage(final TextMessageEvent e)
			{
				if (e.getInvokerId() != _bot.QueryID)
				{
					final String message = e.getMessage();
					if (message.startsWith("!"))
					{
						_bot.TS3API.getClientInfo(e.getInvokerId()).onSuccess(c -> {
							_commands.handleClientCommand(message, c);
						});
					}
				}
			}

			public void onClientJoin(final ClientJoinEvent e)
			{
				if (e.getClientType() == 0)
				{
					_bot.getClientController().clientJoined(e);
				}
			}

			public void onServerEdit(final ServerEditedEvent e)
			{

			}

			public void onClientMoved(final ClientMovedEvent e)
			{
				if (e.getInvokerId() != _bot.QueryID)
				{
					if (_afkMover != null)
						_afkMover.removeClient(e.getClientId());
					_bot.getClientController().removeAfk(e.getClientId());
					if (e.getTargetChannelId() == BotSettings.botChannel)
					{
						_bot.TS3API.sendPrivateMessage(e.getClientId(), "Hallo, wie kann ich dir helfen?");
					}
					if (e.getClientId() == _bot.QueryID && e.getTargetChannelId() != BotSettings.botChannel)
					{
						_bot.TS3API.moveClient(_bot.QueryID, BotSettings.botChannel);
					}
					_bot.TS3API.getChannelInfo(e.getTargetChannelId()).onSuccess(channel -> {
						if (channel.getIconId() == (long) (int) BotSettings.recordIconId)
						{
							_bot.TS3API.sendPrivateMessage(e.getClientId(), "Du befindest dich in einem Aufnahme-Channel! Deine Stimme könnte aufgenommen werden!");
						}
					});
					MyClient sup = _clientController.getActiveSupporter();
					if (sup != null && e.getClientId() == sup.getId())
					{
						_clientController.setActiveSupporter(null);
					}
					if (e.getTargetChannelId() == BotSettings.supporterChannelID)
					{
						_bot.TS3API.getClientInfo(e.getClientId()).onSuccess(client -> {
							if (MyClient.HasCmdPower(client.getServerGroups(), 20))
							{
								LOGGER.info(String.format("%s (Automatic): !helpverify", client.getNickname()));
								LOGGER.info(String.format("%s (Automatic): !helpverify (%s)", client.getNickname(), _helpVerifyCommand.handleAutomatic(client)));
							}
						});
					}
				}
			}

			public void onClientLeave(final ClientLeaveEvent e)
			{
				_bot.getClientController().clientLeft(e);
			}

			public void onChannelEdit(final ChannelEditedEvent e)
			{

			}

			public void onChannelDescriptionChanged(final ChannelDescriptionEditedEvent e)
			{

			}

			public void onChannelCreate(final ChannelCreateEvent e)
			{
				_bot.TS3API.getChannelInfo(e.getChannelId()).onSuccess(result -> {
					if (result.getParentChannelId() == BotSettings.eventParentChannelId)
					{
						_channelController.setDefaultPermissions(e.getChannelId());
					}
					if (_afkMover != null)
						_afkMover.addSpectateChannel(result.getParentChannelId(), e.getChannelId());
				});
			}

			public void onChannelDeleted(final ChannelDeletedEvent e)
			{
				if (_afkMover != null)
					_afkMover.removeSpecateChannel(e.getChannelId());
			}

			public void onChannelMoved(final ChannelMovedEvent e)
			{
				if (_afkMover != null)
					_afkMover.onChannelMoved(e.getChannelParentId(), e.getChannelId());
			}

			public void onChannelPasswordChanged(final ChannelPasswordChangedEvent e)
			{

			}

			@Override
			public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e)
			{

			}
		});
		LOGGER.info("Events initialized");
	}

}
