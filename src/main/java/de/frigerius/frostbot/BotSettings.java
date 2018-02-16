package main.java.de.frigerius.frostbot;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.theholywaffle.teamspeak3.TS3Query;
import com.github.theholywaffle.teamspeak3.TS3Query.FloodRate;

public class BotSettings {
	private static final Logger LOGGER = LoggerFactory.getLogger(BotSettings.class);
	// Server
	public static String username = "";
	public static String password = "";
	public static String channelBotUsername = "";
	public static String channelBotPassword = "";
	public static String serverIP = "localhost";
	public static int port = 10011;
	public static int serverID = 1;
	public static String nickName = "FrostBot";
	public static int botChannel = -1;
	public static FloodRate floodRate = TS3Query.FloodRate.DEFAULT;
	public static String myUID = "";
	public static final List<String> notifyUserIDs = new ArrayList<>();
	public static String serverName = "servername";

	// AFK Mover
	public static boolean isAFKMoverEnabled = true;
	public static int afkTime = 900000;
	public static final List<Integer> afkSpectateChannelIDs = new ArrayList<>();
	public static final List<Integer> afkIgnoreServerGroups = new ArrayList<>();
	public static int afkChannelID = -1;
	public static int afkChannelIDLong = -1;
	public static int afkRule = -1; // 0 = In, 1 = Out, 2 = In&Out, 3 = In|Out, default = nothing

	// Messages
	public static String helpMessage = "";
	public static String explMessage = "";
	public static String homepage = "";

	// ChannelBot
	public static String channelBotID;

	// Support
	public static int supporterChannelID = -1;
	public static List<Integer> supporterGroups = new ArrayList<>();
	public static HashMap<String, Integer> server_groupMap = new HashMap<>();
	public static int removeGroupIdOnVerify = -1;
	public static int guestGroup = -1;
	public static int eventParentChannelId = -1;
	public static List<Integer> channelCreateAllowedIds = new ArrayList<Integer>();
	public static int ignoreMeGroup = -1;

	// AntiTrollStuff
	public static int maxMoveCount = 20;
	public static int maxMoverCount = 20;

	// SQL
	public static String sqlUrl = "";
	public static String sqlUsername = "";
	public static String sqlPassword = "";

	public static long recordIconId = 2417525910l;

	public static boolean read(File file) {
		Ini ini;
		try {
			ini = new Ini(file);
		} catch (Exception e) {
			LOGGER.error("File nicht vorhanden oder nicht kompatible.", e);
			makeDefaultIni();
			return false;
		}
		// Read Connection Settings
		Section connection = ini.get("Connection");
		serverIP = connection.get("ip");
		port = connection.get("port", int.class, 10011);
		botChannel = connection.get("botChannel", int.class, -1);
		username = connection.get("username");
		password = connection.get("password");
		serverID = connection.get("serverID", int.class, -1);
		floodRate = (connection.get("can-flood", boolean.class)) ? TS3Query.FloodRate.UNLIMITED : TS3Query.FloodRate.DEFAULT;
		myUID = connection.get("botUID");
		nickName = connection.get("nickname");
		String[] toNotify = connection.getAll("notify", String[].class);
		notifyUserIDs.clear();
		for (String uid : toNotify) {
			notifyUserIDs.add(uid);
		}
		serverName = connection.get("servername");
		channelBotID = connection.get("channelBotID");
		channelBotUsername = connection.get("channelBotUsername");
		channelBotPassword = connection.get("channelBotPassword");

		// Read AfkMover Settings
		Section afkMover = ini.get("AFK Mover");
		afkChannelID = afkMover.get("channelID", int.class, -1);
		afkChannelIDLong = afkMover.get("longChannelID", int.class, -1);
		isAFKMoverEnabled = afkMover.get("enabled", boolean.class, false) && afkChannelID != -1;
		if (isAFKMoverEnabled) {
			afkRule = afkMover.get("afkRule", int.class, -1);
			afkTime = afkMover.get("maxAfkTime", int.class, 900000);
			int[] spectateChannels = afkMover.getAll("spectateChannelId", int[].class);
			for (int i : spectateChannels) {
				afkSpectateChannelIDs.add(i);
			}
			int[] ignoreServerGroup = afkMover.getAll("ignoreGroups", int[].class);
			for (int i : ignoreServerGroup) {
				afkIgnoreServerGroups.add(i);
			}
		}

		// Read Messages
		Section messages = ini.get("Messages");
		helpMessage = messages.get("help");
		explMessage = messages.get("explain");
		homepage = messages.get("website");

		// Support
		Section support = ini.get("Support");
		supporterChannelID = support.get("channelId", int.class, -1);
		int[] supIds = support.getAll("group", int[].class);
		supporterGroups.clear();
		for (int id : supIds) {
			supporterGroups.add(id);
		}
		String[] ids = support.getAll("verifiedId", String[].class);
		server_groupMap.clear();
		for (String idPair : ids) {
			String[] split = idPair.split(":");
			if (split.length == 2) {
				server_groupMap.put(split[1], Integer.parseInt(split[0]));
			}
		}
		removeGroupIdOnVerify = support.get("removeGroupIdOnVerify", int.class, -1);
		guestGroup = support.get("guestGroup", int.class, -1);
		eventParentChannelId = support.get("eventParentChannelId", int.class, -1);
		String[] creatorIds = support.getAll("channelCreateAllowedIds", String[].class);
		channelCreateAllowedIds.clear();
		for (String s : creatorIds) {
			channelCreateAllowedIds.add(Integer.parseInt(s));
		}
		ignoreMeGroup = support.get("ignoreMeGroup", int.class, -1);

		// SQL
		Section sqlSec = ini.get("SQL");
		sqlUrl = sqlSec.get("url");
		sqlUsername = sqlSec.get("username");
		sqlPassword = sqlSec.get("password");

		Section misc = ini.get("Misc");
		recordIconId = misc.get("recordChannelIconId", long.class, 2417525910l);
		return true;
	}

	public static void saveToFile() {
		Ini ini = makeIni();

		Section connection = ini.get("Connection");
		connection.put("ip", serverIP);
		connection.put("port", port);
		connection.put("botChannel", botChannel);
		connection.put("username", username);
		connection.put("password", password);
		connection.put("serverID", serverID);
		connection.put("can-flood", floodRate == FloodRate.UNLIMITED);
		connection.put("botUID", myUID);
		connection.put("nickname", nickName);
		connection.putAll("notify", notifyUserIDs);
		connection.put("servername", serverName);

		// Read AfkMover Settings
		Section afkMover = ini.get("AFK Mover");
		afkMover.put("channelID", afkChannelID);
		afkMover.put("enabled", isAFKMoverEnabled);
		afkMover.put("afkRule", afkRule);
		afkMover.put("maxAfkTime", afkTime);
		afkMover.putAll("spectateChannelId", afkSpectateChannelIDs);
		afkMover.putAll("ignoreGroups", afkIgnoreServerGroups);

		// Read Messages
		Section messages = ini.get("Messages");
		messages.put("help", helpMessage);
		messages.put("explain", explMessage);
		messages.put("website", homepage);

		// Support
		Section support = ini.get("Support");
		support.put("channelId", supporterChannelID);
		support.putAll("group", supporterGroups);
		support.put("verifiedId", "id:servername");
		for (Entry<String, Integer> entry : server_groupMap.entrySet()) {
			support.put("verifiedId", entry.getValue() + ":" + entry.getKey());
		}
		support.put("removeGroupIdOnVerify", removeGroupIdOnVerify);
		support.put("guestGroup", guestGroup);
		support.put("eventParentChannelId", eventParentChannelId);
		support.putAll("channelCreateAllowedIds", channelCreateAllowedIds);

		Section sqlSec = ini.get("SQL");
		sqlSec.put("url", sqlUrl);
		sqlSec.put("username", sqlUsername);
		sqlSec.put("password", sqlPassword);

		Section misc = ini.get("Misc");
		misc.put("recordChannelIconId", recordIconId);

		try {
			ini.store(new FileOutputStream("config.ini"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void makeDefaultIni() {
		Ini defaultIni = makeIni();

		Section connection = defaultIni.get("Connection");
		connection.put("ip", "localhost");
		connection.put("port", 10011);
		connection.put("botChannel", -1);
		connection.put("username", "username");
		connection.put("password", "password");
		connection.put("serverID", -1);
		connection.put("can-flood", false);
		connection.put("botUID", "uid");
		connection.put("nickname", "FrostBot");
		connection.put("notify", "uid");
		connection.put("servername", "servername");

		// Read AfkMover Settings
		Section afkMover = defaultIni.get("AFK Mover");
		afkMover.put("channelID", -1);
		afkMover.put("enabled", false);
		afkMover.putComment("afkRule", "0 = In, 1 = Out, 2 = In&Out, 3 = In|Out, default = nothing");
		afkMover.put("afkRule", -1);
		afkMover.put("maxAfkTime", 900000);
		afkMover.put("spectateChannelId", "id");
		afkMover.put("ignoreGroups", "id");

		// Read Messages
		Section messages = defaultIni.get("Messages");
		messages.put("help", "help");
		messages.put("explain", "explain me the world");
		messages.put("website", "google.de");

		// Support
		Section support = defaultIni.get("Support");
		support.put("channelId", -1);
		support.put("group", -1);
		support.put("verifiedId", "id:servername");
		support.put("removeGroupIdOnVerify", -1);

		Section sqlSec = defaultIni.get("SQL");
		sqlSec.put("url", "jdbc:mysql://...");
		sqlSec.put("username", "user");
		sqlSec.put("password", "password");

		Section misc = defaultIni.get("Misc");
		misc.put("recordChannelIconId", "0");

		try {
			defaultIni.store(new FileOutputStream("config.ini"));

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Ini makeIni() {
		Ini defaultIni = new Ini();
		defaultIni.add("Connection");
		defaultIni.putComment("Connection", "Hier wird die Verbindung zum Server eingerichtet.");
		defaultIni.add("AFK Mover");
		defaultIni.putComment("AFK Mover", "Hier wird der AFK Mover eingerichtet.");
		defaultIni.add("Support");
		defaultIni.putComment("Support", "Richtet die Supporter ein.");
		defaultIni.add("Messages");
		defaultIni.putComment("Messages", "Gib die Standartnachrichten ein.");
		defaultIni.add("SQL");
		defaultIni.add("Misc");

		return defaultIni;
	}
}
