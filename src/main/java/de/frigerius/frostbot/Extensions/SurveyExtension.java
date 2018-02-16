package main.java.de.frigerius.frostbot.Extensions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantLock;

import com.github.theholywaffle.teamspeak3.api.event.TextMessageEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ChannelBot;
import main.java.de.frigerius.frostbot.IChannelBotExtension;

public class SurveyExtension implements IChannelBotExtension {
	enum Vote {
		Pro, Contra, DontCare
	}

	private ChannelBot _bot;
	private HashMap<String, Vote> _votes = new HashMap<>();
	private ReentrantLock _lock = new ReentrantLock();
	private HashMap<String, Vote> _possibleVotes = new HashMap<>();
	private boolean _isSurveyOpen = false;
	private Runnable _onExit;
	private String _surveyText;
	private final String _helpMessage;

	public SurveyExtension(Runnable onExit, String surveyText) {
		_onExit = onExit;
		_surveyText = surveyText;
		// Pro
		_possibleVotes.put("+", Vote.Pro);
		_possibleVotes.put("pro", Vote.Pro);
		_possibleVotes.put("ja", Vote.Pro);
		_possibleVotes.put("yes", Vote.Pro);
		_possibleVotes.put("y", Vote.Pro);
		_possibleVotes.put("j", Vote.Pro);
		// Con
		_possibleVotes.put("-", Vote.Contra);
		_possibleVotes.put("nein", Vote.Contra);
		_possibleVotes.put("no", Vote.Contra);
		_possibleVotes.put("n", Vote.Contra);
		_possibleVotes.put("con", Vote.Contra);
		// Egal
		_possibleVotes.put("=", Vote.DontCare);
		_possibleVotes.put("dc", Vote.DontCare);
		_possibleVotes.put("egal", Vote.DontCare);
		_possibleVotes.put("mir egal", Vote.DontCare);

		_helpMessage = createHelpMessage();
	}

	public void StartNewSurvey(String text) {
		_lock.lock();
		try {
			if (_isSurveyOpen)
				CloseSurvey();
			// Start
			_isSurveyOpen = true;
			_surveyText = text;
			String noMsg = String.format("%s hat eine Umfrage gestartet.", _bot.getOwner().getName());
			if (text.length() > 0) {
				if (text.length() + noMsg.length() + /* \n */2 < 1024) {
					_bot.TS3API.sendChannelMessage(String.format("%s hat eine Umfrage gestartet:\n%s", _bot.getOwner().getName(), text));
				} else {
					_bot.TS3API.sendChannelMessage(String.format("%s hat eine Umfrage gestartet:", _bot.getOwner().getName()));
					_bot.TS3API.sendChannelMessage(text);
				}
			} else {
				_bot.TS3API.sendChannelMessage(noMsg);
			}
		} finally {
			_lock.unlock();
		}
	}

	public void CloseSurvey() {
		_lock.lock();
		try {
			if (_isSurveyOpen) {
				_isSurveyOpen = false;
				_bot.TS3API.sendChannelMessage("Die Umfrage ist nun beendet.");
				int pros = 0;
				int cons = 0;
				int dc = 0;
				for (Vote vote : _votes.values()) {
					if (vote == Vote.Pro)
						pros++;
					if (vote == Vote.Contra)
						cons++;
					if (vote == Vote.DontCare)
						dc++;
				}
				_bot.TS3API.sendChannelMessage(String.format("Die Umfrage ergab:\nDafür: %d\nDagegen: %d\nEnthaltungen: %d", pros, cons, dc));
			}
		} finally {
			_lock.unlock();
		}
	}

	@Override
	public void handleClientCommand(String cmd, Client c) {
		_lock.lock();
		try {
			if (!_isSurveyOpen)
				return;
			if (cmd.startsWith("!") && c.getUniqueIdentifier().equals(_bot.getOwner().getUID())) {
				if (cmd.equals("!close")) {
					CloseSurvey();
					return;
				}
			}
			String UID = c.getUniqueIdentifier();
			if (!_votes.containsKey(UID)) {
				Vote vote = _possibleVotes.get(cmd);
				if (vote != null) {
					_votes.put(UID, vote);
				}
			}
		} finally {
			_lock.unlock();
		}
	}

	@Override
	public void handleRawMessage(final TextMessageEvent e) {
		_lock.lock();
		try {
			if (e.getMessage().length() < 10 && _isSurveyOpen) {
				_bot.TS3API.getClientInfo(e.getInvokerId()).onSuccess(c -> {
					handleClientCommand(e.getMessage(), c);
				});
			}
		} finally {
			_lock.unlock();
		}
	}

	@Override
	public void onConnected() {
		_bot.TS3API.sendChannelMessage(_helpMessage);
		StartNewSurvey(_surveyText);
	}

	private String createHelpMessage() {
		LinkedList<String> proList = new LinkedList<>();
		LinkedList<String> conList = new LinkedList<>();
		LinkedList<String> dcList = new LinkedList<>();
		for (Entry<String, Vote> entry : _possibleVotes.entrySet()) {
			switch (entry.getValue()) {
			case Pro:
				proList.add(entry.getKey());
				break;
			case Contra:
				conList.add(entry.getKey());
				break;
			case DontCare:
				dcList.add(entry.getKey());
				break;
			}
		}
		String pro = String.join(", ", proList);
		String con = String.join(", ", conList);
		String dc = String.join(", ", dcList);
		return String.format("Hey Leute, mögliche Antworten auf eine Umfrage sind:\nDafür: %s\nDagegen: %s\nEnthaltung: %s", pro, con, dc);
	}

	@Override
	public void setBot(ChannelBot bot) {
		_bot = bot;

	}

	@Override
	public void onBeforeClose() {
		CloseSurvey();
		if (_onExit != null)
			_onExit.run();

	}

}
