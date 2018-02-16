package main.java.de.frigerius.frostbot.commands;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import main.java.de.frigerius.frostbot.ColoredText;
import main.java.de.frigerius.frostbot.MyClient;

public class JoinGuildCommand extends RequestingBaseGuildCommand {
	public JoinGuildCommand(int cmdPwr) {
		super("joinguild", cmdPwr);
	}

	@Override
	protected CommandResult handleIntern(Client client, String[] args) {
		if (args.length != 2)
			return CommandResult.ArgumentError;
		try {
			if (AddRequester(client.getUniqueIdentifier())) {
				int gId = Integer.parseInt(args[0]);
				String apiKey = args[1];
				if (!_bot.isValidAPIKey(apiKey))
					return CommandResult.ArgumentError;
				if (!_guildManager.joinGuild(apiKey, client, gId)) {
					_bot.TS3API.sendPrivateMessage(client.getId(), ColoredText.red("Bitte überprüfe deine Eingabe. Du scheinst nicht der ausgewählten Gilde anzugehören."));
				}
			}
		} catch (NumberFormatException ex) {
			return CommandResult.ArgumentError;
		} finally {
			RemoveRequester(client.getUniqueIdentifier());
		}

		return CommandResult.NoErrors;
	}

	@Override
	public boolean hasClientRights(Client client, int cmdPwr) {
		return !MyClient.HasCmdPower(client.getServerGroups(), 11) && super.hasClientRights(client, cmdPwr);
	}

	@Override
	public String getArguments() {
		return "[Gilden ID] [API-Key]";
	}

	@Override
	public String getDescription() {
		return "Tritt der Gilde mit der ID bei.";
	}

	@Override
	protected String getDetails() {
		return "Finde mit !listguilds die ID deiner Gilde heraus. Anschließend kannst du ihr wie im folgenden Beispiel beitreten:\n"
				+ ColoredText.green("Beispiel: !joinguild 42 XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXXXXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX");
	}

}
