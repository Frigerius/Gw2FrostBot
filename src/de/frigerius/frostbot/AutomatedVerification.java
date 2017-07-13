package de.frigerius.frostbot;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

import me.xhsun.guildwars2wrapper.GuildWars2;
import me.xhsun.guildwars2wrapper.error.GuildWars2Exception;
import me.xhsun.guildwars2wrapper.model.v2.account.Account;

public class AutomatedVerification
{
	private Map<Integer, String> worlds;
	private GuildWars2 gw2api = GuildWars2.getInstance();
	private final Logger LOGGER = LoggerFactory.getLogger(AutomatedVerification.class);
	private FrostBot _bot;

	public AutomatedVerification()
	{
		_bot = FrostBot.getInstance();
		init();
	}

	public int verify(Client client, String apikey)
	{
		try
		{
			LOGGER.info(String.format("Api wird angefragt für %s", client.getNickname()));
			_bot.TS3API.sendPrivateMessage(client.getId(), "Ich habe deinen Key erhalten. Bitte habe einen Moment Geduld.");
			Account account = gw2api.getSynchronous().getAccountInfo(apikey);
			LOGGER.info(String.format("Anfrage der API abgeschlossen für %s", client.getNickname()));
			String accUID = account.getId();
			if (UserDatabase.getAccountUsages(accUID) < 2)
			{
				String worldName = worlds.get(account.getWorldId());
				if (BotSettings.server_groupMap.containsKey(worldName))
				{
					int groupId = BotSettings.server_groupMap.get(worldName);
					if (_bot.isUserRank(groupId))
					{
						LOGGER.info(String.format("Adding %s to servergroup %s...", client.getNickname(), worldName));
						if (BotSettings.removeGroupIdOnVerify != -1 && MyClient.isInServerGroup(client.getServerGroups(), BotSettings.removeGroupIdOnVerify))
						{
							_bot.TS3API.removeClientFromServerGroup(BotSettings.removeGroupIdOnVerify, client.getDatabaseId());
						}
						_bot.TS3API.addClientToServerGroup(groupId, client.getDatabaseId()).onSuccess(result -> {
							if (result)
							{
								UserDatabase.AddAccountID(accUID, client.getNickname(), client.getUniqueIdentifier(), worldName);
								LOGGER.info(String.format("%s was added to servergroup %s.", client.getNickname(), worldName));
							}
						});
					}
					return 0;
				}
			} else
			{
				LOGGER.info(String.format("%s hat sich zu oft registriert.", client.getNickname()));
				return 2;
			}
		} catch (GuildWars2Exception ex)
		{
			LOGGER.error("Error in AutomatedVerification", ex);
		}
		return 1;
	}

	public static boolean isClientVerified(Client client)
	{
		return isClientVerified(client.getServerGroups());
	}

	public static boolean isClientVerified(ClientJoinEvent clientEvent)
	{
		return isClientVerified(MyClient.makeStringToServerGroups(clientEvent.getClientServerGroups()));
	}

	private static boolean isClientVerified(int[] groups)
	{
		return MyClient.isInServerGroup(groups, BotSettings.server_groupMap.values());
	}

	public static boolean isValidAPIKey(String key)
	{
		int[] lengths = { 8, 4, 4, 4, 20, 4, 4, 4, 12 };
		String[] split = key.split("-");
		if (lengths.length != split.length)
			return false;
		for (int i = 0; i < lengths.length; i++)
		{
			if (split[i].length() != lengths[i])
				return false;
		}
		return true;
	}

	public static boolean checkVerify(ClientJoinEvent clientEvent)
	{
		return checkVerify(MyClient.makeStringToServerGroups(clientEvent.getClientServerGroups()));
	}

	public static boolean checkVerify(Client client)
	{
		return checkVerify(client.getServerGroups());
	}

	private static boolean checkVerify(int[] groups)
	{
		return MyClient.isInServerGroup(groups, BotSettings.guestGroup) || MyClient.isInServerGroup(groups, BotSettings.removeGroupIdOnVerify);
	}

	private void init()
	{
		worlds = new HashMap<>();
		worlds.put(1001, "Anvil Rock");
		worlds.put(1002, "Borlis Pass");
		worlds.put(1003, "Yak's Bend");
		worlds.put(1004, "Henge of Denravi");
		worlds.put(1005, "Maguuma");
		worlds.put(1006, "Sorrow's Furnace");
		worlds.put(1007, "Gate of Madness");
		worlds.put(1008, "Jade Quarry");
		worlds.put(1009, "Fort Aspenwood");
		worlds.put(1010, "Ehmry Bay");
		worlds.put(1011, "Stormbluff Isle");
		worlds.put(1012, "Darkhaven");
		worlds.put(1013, "Sanctum of Rall");
		worlds.put(1014, "Crystal Desert");
		worlds.put(1015, "Isle of Janthir");
		worlds.put(1016, "Sea of Sorrows");
		worlds.put(1017, "Tarnished Coast");
		worlds.put(1018, "Northern Shiverpeaks");
		worlds.put(1019, "Blackgate");
		worlds.put(1020, "Ferguson's Crossing");
		worlds.put(1021, "Dragonbrand");
		worlds.put(1022, "Kaineng");
		worlds.put(1023, "Devona's Rest");
		worlds.put(1024, "Eredon Terrace");
		worlds.put(2001, "Fissure of Woe");
		worlds.put(2002, "Desolation");
		worlds.put(2003, "Gandara");
		worlds.put(2004, "Blacktide");
		worlds.put(2005, "Ring of Fire");
		worlds.put(2006, "Underworld");
		worlds.put(2007, "Far Shiverpeaks");
		worlds.put(2008, "Whiteside Ridge");
		worlds.put(2009, "Ruins of Surmia");
		worlds.put(2010, "Seafarer's Rest");
		worlds.put(2011, "Vabbi");
		worlds.put(2012, "Piken Square");
		worlds.put(2013, "Aurora Glade");
		worlds.put(2014, "Gunnar's Hold");
		worlds.put(2101, "Jade Sea [FR]");
		worlds.put(2102, "Fort Ranik [FR]");
		worlds.put(2103, "Augury Rock [FR]");
		worlds.put(2104, "Vizunah Square [FR]");
		worlds.put(2105, "Arborstone [FR]");
		worlds.put(2201, "Kodash [DE]");
		worlds.put(2202, "Riverside [DE]");
		worlds.put(2203, "Elona Reach [DE]");
		worlds.put(2204, "Abaddon's Mouth [DE]");
		worlds.put(2205, "Drakkar Lake [DE]");
		worlds.put(2206, "Miller's Sound [DE]");
		worlds.put(2207, "Dzagonur [DE]");
		worlds.put(2301, "Baruch Bay [SP]");
	}
}
