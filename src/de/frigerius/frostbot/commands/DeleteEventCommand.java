package de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import de.frigerius.frostbot.ColoredText;
import de.frigerius.frostbot.WvWEvents;

public class DeleteEventCommand extends BaseCommand
{

	public DeleteEventCommand(int cmdPwr)
	{
		super("delevent", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args)
	{
		try
		{
			int id = Integer.parseInt(args[0]);
			if (WvWEvents.removeEvent(id, c.getUniqueIdentifier()))
			{
				_bot.TS3API.sendPrivateMessage(c.getId(), ColoredText.green("Das Event wurde erfolgreich entfernt."));
			} else
			{
				_bot.TS3API.sendPrivateMessage(c.getId(),
						ColoredText.red("Das Event konnte nicht entfernt werden, entweder ist die ID falsch, oder das Event wurde nicht von dir erstellt."));
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
		return "[ID]";
	}

	@Override
	public String getDescription()
	{
		return "(Beta!) Entfernt dein Event mit der angegebenen ID.";
	}

	@Override
	protected String getDetails()
	{
		return "";
	}

}
