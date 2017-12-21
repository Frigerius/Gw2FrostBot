package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.SurveyController;

public class RequestSurveyCommand extends BaseCommand
{
	private SurveyController _controller;

	public RequestSurveyCommand(int cmdPwr)
	{
		super("survey", cmdPwr);
		_controller = SurveyController.getInstance();
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args)
	{
		String text = "";
		if (args.length > 0)
		{
			text = String.join(" ", args);
		}
		if (_controller.RequestNewSurvey(client, text))
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.green("Deine Umfrage wird erstellt, du kannst sie mit !close im Channel-Chat beenden."));
		} else
		{
			_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Deine Umfrage konnte nicht erstellt werden, da bereits ein anderer Bot in deinem Channel aktiv ist."));
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[(Beschreibung)]";
	}

	@Override
	public String getDescription()
	{
		return "Erstellt eine Umfrage in deinem aktuellen channel.";
	}

	@Override
	protected String getDetails()
	{
		return "Du kannst deine Umfrage mit !close im channel Chat beenden. Verlässt du den Channel, wird dein Bot und die Umfrage automatisch beendet.";
	}

}
