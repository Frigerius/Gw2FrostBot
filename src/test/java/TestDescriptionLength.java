package test.java;

import static org.junit.Assert.fail;

import org.junit.Test;

import main.java.de.frigerius.frostbot.FrostBot;
import main.java.de.frigerius.frostbot.commands.BaseCommand;
import main.java.de.frigerius.frostbot.commands.Commands;

public class TestDescriptionLength
{

	@Test
	public void test()
	{
		Commands commands = FrostBot.getInstance().getCommands();
		commands.init();
		for (BaseCommand cmd : commands.commands.values())
		{
			String des = cmd.getDetailedDescription();
			if (des.length() > 1024)
			{
				System.out.println(des);
				fail("Description too long for !" + cmd.getCommand());
			}
		}
	}

}
