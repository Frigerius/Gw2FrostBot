package main.java.de.frigerius.frostbot;

import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import main.java.de.frigerius.frostbot.commands.Commands;

public class Console {
	private final Logger LOGGER = LoggerFactory.getLogger(Console.class);
	Commands _commands;

	public Console() {
		_commands = FrostBot.getInstance().getCommands();
	}

	public void runReadThread() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				String cmd;
				Scanner txt = new Scanner(System.in);
				while (txt.hasNextLine()) {
					try {
						cmd = txt.nextLine();
						LOGGER.info("Received: " + cmd);
						_commands.handleConsoleCommand(cmd);
					} catch (Exception ex) {
						LOGGER.error("Error in ReadThread.", ex);
					}
				}
				txt.close();
				LOGGER.info("Exiting Thread.");
			}
		});
		t.start();
	}
}
