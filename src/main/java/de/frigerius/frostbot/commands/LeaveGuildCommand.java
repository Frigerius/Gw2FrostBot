package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.MyClient;

public class LeaveGuildCommand extends RequestingBaseGuildCommand {

	public LeaveGuildCommand(int cmdPwr) {
		super("leaveguild", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		if (AddRequester(client.getUniqueIdentifier())) {
			try {
				_guildManager.LeaveGuild(client);
			} finally {
				RemoveRequester(client.getUniqueIdentifier());
			}

		}
		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client client, int cmdPwr) {
		return MyClient.HasCmdPower(client.getServerGroups(), getCmdPwr());
	}

	@Override
	public String getArguments() {
		return "";
	}

	@Override
	public String getDescription() {
		return "Du verlässt die ServerGruppe deiner gilde.";
	}

	@Override
	protected String getDetails() {
		return ColoredText.green(String.format("Beispiel: !%s 42", getCommand()));
	}

}
