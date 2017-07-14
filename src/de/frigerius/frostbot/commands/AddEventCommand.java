package de.frigerius.frostbot.commands;

import java.util.Calendar;
import java.util.Date;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.WvWEvents;

public class AddEventCommand extends BaseCommand
{

	public AddEventCommand(int cmdPwr)
	{
		super("addevent", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		if (args.length != 2)
			return CommandResult.ArgumentError;
		String name = args[0];
		try
		{
			int duration = Integer.parseInt(args[1].substring(0, 1));
			if (duration < 4)
			{
				if (name.length() <= 255)
				{
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					cal.add(Calendar.HOUR_OF_DAY, duration);
					if (WvWEvents.CreateNewEvent(name, new Date(), cal.getTime(), c.getChannelId(), c.getUniqueIdentifier(), c.getNickname()))
					{
						_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.green("Dein Event wurde erfolgreich erstellt."));
					} else
					{
						_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Bei der Erstellung deines Events ist ein Fehler aufgetreten."));
					}
				} else
				{
					_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Der Name des Events ist auf 255 Zeichen beschränkt."));
				}
			} else
			{
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.red("Die maximale Länge eines Events beträgt 4 Stunden."));
			}
		} catch (NumberFormatException ex)
		{
			return CommandResult.ArgumentError;
		}
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments()
	{
		return "[Titel] [Dauer in Stunden]";
	}

	@Override
	public String getDescription()
	{
		return "Erstellt ein Event für den Channel, in dem du dich befindest.";
	}

	@Override
	protected String getDetails()
	{
		return "Das Event startet sofort und wird durch [Dauer, 1-4h] begrenzt.\nTitel, die nicht nur aus einem Wort bestehen mit \" einrahmen.\nBeispiel: !addevent \"Mein langer Titel\" 2";
	}

}
