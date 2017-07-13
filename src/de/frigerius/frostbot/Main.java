package de.frigerius.frostbot;

public class Main
{

	public static void main(String[] args)
	{
		FrostBot _bot = FrostBot.getInstance();
		_bot.readConfig();
		if (args.length > 0)
		{
			new MaintenanceBot(args);
		} else
		{
			_bot.start();
		}

	}
}
