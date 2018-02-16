package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class StopCommand extends BaseCommand {
	public StopCommand(int cmdPwr) {
		super("stop", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client c, String[] args) {
		System.exit(0);
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client c, int cmdPwr) {
		return true;
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
