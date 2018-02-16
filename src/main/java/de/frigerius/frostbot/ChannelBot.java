package main.java.de.frigerius.frostbot;

import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.TS3ApiAsync;
import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.TextMessageTargetMode;
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
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectingConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo;

public class ChannelBot {
	private final Logger LOGGER = LoggerFactory.getLogger(ChannelBot.class);
	public TS3Query QUERY = null;
	public TS3ApiAsync TS3API = null;
	public int QueryID;
	private boolean _initialized = false;
	private String _name;
	private int _channelId;
	private FrostBot _bot;
	private List<IChannelBotExtension> _extensions;
	private boolean _isInit = false;
	private boolean _handleMessages = false;
	private MyClient _owner;

	public int getChannelId() {
		return _channelId;
	}

	public MyClient getOwner() {
		return _owner;
	}

	public boolean isInit() {
		return _isInit;
	}

	public boolean isHandleMessages() {
		return _handleMessages;
	}

	public void setHandleMessages(boolean handleMessages) {
		_handleMessages = handleMessages;
	}

	public ChannelBot(Client client, String name, int channelId) {
		_owner = new MyClient(client);
		_name = name;
		_channelId = channelId;
		_extensions = new LinkedList<IChannelBotExtension>();
		_bot = FrostBot.getInstance();
	}

	public void addExtension(IChannelBotExtension extension) {
		_extensions.add(extension);
		extension.setBot(this);
	}

	public void init() throws TS3ConnectionFailedException {
		if (_isInit)
			return;
		_isInit = true;
		final TS3Config config = new TS3Config();
		config.setHost(BotSettings.serverIP);
		config.setQueryPort(BotSettings.port);
		config.setFloodRate(BotSettings.floodRate);
		config.setReconnectStrategy(ReconnectStrategy.exponentialBackoff());
		config.setConnectionHandler(new ReconnectingConnectionHandler(null, 5, 180, 2, 0) {
			@Override
			public void onConnect(TS3Query ts3Query) {
				if (!_initialized) {
					_initialized = true;
					LOGGER.info("Initializing ChannelBot...");

				} else {
					LOGGER.info("Reinitializing ChannelBot...");
				}
				QUERY = ts3Query;
				TS3API = QUERY.getAsyncApi();
				if (TS3API == null)
					return;
				try {
					connect();
				} catch (InterruptedException ignored) {
				}
			}
		});

		LOGGER.info("Connecting to " + BotSettings.serverIP + ":" + BotSettings.port);

		QUERY = new TS3Query(config);
		QUERY.connect();

		TS3API = QUERY.getAsyncApi();
		addEventCallbacks();

	}

	public void exit() {
		LOGGER.debug("exit called");
		TS3API.sendChannelMessage(ColoredText.red("Ich bin dann mal weg."));
		for (IChannelBotExtension extension : _extensions) {
			extension.onBeforeClose();
		}
		_bot.getChannelBotCommander().removeBot(this);
		QUERY.exit();
	}

	private void connect() throws InterruptedException {
		TS3API.login(BotSettings.channelBotUsername, BotSettings.channelBotPassword);
		TS3API.login("channelbot", "xrpRRuKa");
		TS3API.selectVirtualServerById(BotSettings.serverID);
		TS3API.setNickname(_name).get();
		ServerQueryInfo serverQueryInfo = TS3API.whoAmI().get();
		QueryID = serverQueryInfo.getId();
		if (serverQueryInfo.getChannelId() != _channelId)
			TS3API.moveClient(QueryID, _channelId);
		TS3API.registerAllEvents();
		for (IChannelBotExtension extension : _extensions) {
			extension.onConnected();
		}
	}

	private void addEventCallbacks() {
		TS3API.addTS3Listeners(new TS3Listener() {
			public void onTextMessage(final TextMessageEvent e) {
				if (e.getInvokerId() != QueryID) {
					if (e.getTargetMode() != TextMessageTargetMode.CHANNEL)
						return;
					final String message = e.getMessage();
					if (_handleMessages && message.startsWith("!")) {
						TS3API.getClientInfo(e.getInvokerId()).onSuccess(c -> {
							for (IChannelBotExtension extension : _extensions) {
								extension.handleClientCommand(message, c);
							}
						});
					} else {
						for (IChannelBotExtension extension : _extensions) {
							extension.handleRawMessage(e);
						}
					}
				}
			}

			public void onClientJoin(final ClientJoinEvent e) {

			}

			public void onServerEdit(final ServerEditedEvent e) {

			}

			public void onClientMoved(final ClientMovedEvent e) {
				if (e.getInvokerId() != _bot.QueryID) {
					if (e.getClientId() == QueryID && e.getTargetChannelId() != _channelId) {
						TS3API.moveClient(QueryID, BotSettings.botChannel);
					}
				}
				if (e.getClientId() == _owner.getId()) {
					TS3API.sendChannelMessage("Oh NO, mein Meister ist verschwunden, ich bin dann mal weg.");
					exit();
				}
			}

			public void onClientLeave(final ClientLeaveEvent e) {

			}

			public void onChannelEdit(final ChannelEditedEvent e) {

			}

			public void onChannelDescriptionChanged(final ChannelDescriptionEditedEvent e) {

			}

			public void onChannelCreate(final ChannelCreateEvent e) {

			}

			public void onChannelDeleted(final ChannelDeletedEvent e) {

			}

			public void onChannelMoved(final ChannelMovedEvent e) {

			}

			public void onChannelPasswordChanged(final ChannelPasswordChangedEvent e) {

			}

			@Override
			public void onPrivilegeKeyUsed(PrivilegeKeyUsedEvent e) {

			}
		});
		LOGGER.info("Events initialized");
	}
}
