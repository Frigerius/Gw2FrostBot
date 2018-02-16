package main.java.de.frigerius.frostbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.TS3Config;
import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.api.exception.TS3ConnectionFailedException;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectStrategy;
import com.github.theholywaffle.teamspeak3.api.reconnect.ReconnectingConnectionHandler;
import com.github.theholywaffle.teamspeak3.api.wrapper.ServerQueryInfo;

public class MyConnection {
	private final Logger LOGGER = LoggerFactory.getLogger(MyConnection.class);
	private FrostBot _bot;
	private boolean _initialized = false;
	private Runnable _onConnect;

	public MyConnection(Runnable onConnect) {
		_bot = FrostBot.getInstance();
		_onConnect = onConnect;
	}

	public void init() throws TS3ConnectionFailedException {
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
					LOGGER.info("Initializing FrostBot...");

				} else {
					LOGGER.info("Reinitializing FrostBot...");
				}
				_bot.QUERY = ts3Query;
				_bot.TS3API = _bot.QUERY.getAsyncApi();
				if (_bot.TS3API == null)
					return;
				try {
					connect();
				} catch (InterruptedException ignored) {
				}
			}
		});

		LOGGER.info("Connecting to " + BotSettings.serverIP + ":" + BotSettings.port);

		_bot.QUERY = new TS3Query(config);
		_bot.QUERY.connect();

		_bot.TS3API = _bot.QUERY.getAsyncApi();

	}

	private void connect() throws InterruptedException {
		_bot.TS3API.login(BotSettings.username, BotSettings.password);
		_bot.TS3API.selectVirtualServerById(BotSettings.serverID);
		_bot.TS3API.setNickname(BotSettings.nickName).get();
		ServerQueryInfo serverQueryInfo = _bot.TS3API.whoAmI().get();
		_bot.QueryID = serverQueryInfo.getId();
		if (BotSettings.botChannel != -1 && serverQueryInfo.getChannelId() != BotSettings.botChannel)
			_bot.TS3API.moveClient(_bot.QueryID, BotSettings.botChannel);

		if (_onConnect != null)
			_onConnect.run();
	}
}
