package main.java.de.frigerius.frostbot;

import java.util.concurrent.ConcurrentHashMap;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.Extensions.SurveyExtension;
import main.java.de.frigerius.frostbot.exceptions.ChannelAlreadyHasBotException;

public class SurveyController
{

	private static SurveyController _instance;
	private ChannelBotCommander _commander;
	private ConcurrentHashMap<String, SurveyExtension> _surveys = new ConcurrentHashMap<>();

	private SurveyController()
	{
		_instance = this;
		_commander = FrostBot.getInstance().getChannelBotCommander();
	}

	public static SurveyController getInstance()
	{
		return _instance == null ? new SurveyController() : _instance;
	}

	public boolean RequestNewSurvey(Client client, String text)
	{
		SurveyExtension survey = _surveys.get(client.getUniqueIdentifier());
		if (survey == null)
		{
			String uid = client.getUniqueIdentifier();
			try
			{
				ChannelBot bot = _commander.createChannelBot(client, "SurveyBot", client.getChannelId());
				if (!bot.isInit())
				{
					survey = new SurveyExtension(() -> {
						_surveys.remove(uid);
					}, text);
					_surveys.put(uid, survey);
					bot.addExtension(survey);
					bot.init();
					return true;
				}
			} catch (ChannelAlreadyHasBotException e)
			{
				return false;
			}
		}
		survey.StartNewSurvey(text);
		return true;
	}

}
