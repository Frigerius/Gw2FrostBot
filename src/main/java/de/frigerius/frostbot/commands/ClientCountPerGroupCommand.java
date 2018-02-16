package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class ClientCountPerGroupCommand extends BaseCommand {

	public ClientCountPerGroupCommand(int cmdPwr) {
		super("clientcountpergroup", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		int count = 0;
		for (String arg : args) {
			try {
				int id = Integer.parseInt(arg);
				_bot.TS3API.getServerGroupClients(id).onSuccess(clients -> {
					_bot.TS3API.sendPrivateMessage(client.getId(), String.format("Es befinden sich %d Nutzer in Servergruppe %d.", clients.size(), id));
				});
				count++;
			} catch (NumberFormatException e) {

			}

		}
		if (count < 1)
			return CommandResult.ArgumentError;
		return CommandResult.NoErrors;
	}

	@Override
	public String getArguments() {
		return "[ServerGroupId] [(ServerGroupId)] ...";
	}

	@Override
	public String getDescription() {
		return "Gibt die Anzahl der Clients in der/den Servergruppe(n) aus.";
	}

	@Override
	protected String getDetails() {
		return "";
	}

}
