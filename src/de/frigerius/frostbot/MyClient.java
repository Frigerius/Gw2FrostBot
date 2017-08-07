package de.frigerius.frostbot;

import java.util.Collection;

import com.github.theholywaffle.teamspeak3.api.event.ClientJoinEvent;
import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public class MyClient
{
	public enum RequestionState
	{
		None, AutomatedVerificationFailed, ManualVerificationRequested
	}

	private int _id;
	private String _uid;
	private String _name;
	private int _lastChannelId;
	private int _databaseId;
	private RequestionState _requestionState;

	public MyClient(Client client)
	{
		_id = client.getId();
		_uid = client.getUniqueIdentifier();
		_name = client.getNickname();
		_lastChannelId = client.getChannelId();
		_databaseId = client.getDatabaseId();
	}

	public MyClient(ClientJoinEvent e)
	{
		_id = e.getClientId();
		_uid = e.getUniqueClientIdentifier();
		_name = e.getClientNickname();
		_lastChannelId = -1;
		_databaseId = e.getClientDatabaseId();
	}

	public MyClient(Client client, RequestionState state)
	{
		this(client);
		_requestionState = state;
	}

	public int getId()
	{
		return _id;
	}

	public String getUID()
	{
		return _uid;
	}

	public String getName()
	{
		return _name;
	}

	public int getLastChannelId()
	{
		return _lastChannelId;
	}

	public void setLastChannelId(int id)
	{
		_lastChannelId = id;
	}

	public RequestionState getRequestionState()
	{
		return _requestionState;
	}

	public void setRequestionState(RequestionState state)
	{
		_requestionState = state;
	}

	public static boolean isInServerGroup(int[] cgroups, Collection<Integer> other)
	{
		for (int id : other)
		{
			if (isInServerGroup(cgroups, id))
			{
				return true;
			}
		}
		return false;
	}

	public static boolean isInServerGroup(int[] myGroups, int group)
	{
		if (group == -1)
			return false;
		for (int sg : myGroups)
		{
			if (sg == group)
				return true;
		}
		return false;
	}

	public static boolean isInServerGroup(String groups, int group)
	{
		return isInServerGroup(makeStringToServerGroups(groups), group);
	}

	public static int[] makeStringToServerGroups(String myGroups)
	{
		String[] parts = myGroups.split(",");
		int[] groups = new int[parts.length];
		for (int i = 0; i < parts.length; i++)
		{
			groups[i] = Integer.parseInt(parts[i]);
		}
		return groups;
	}

	public int getDatabaseIde()
	{
		return _databaseId;
	}

	public static int GetCmdPower(int[] groups)
	{
		int power = 0;
		for (int i : groups)
		{
			int tmp = FrostBot.getInstance().getCmdPower(i);
			if (tmp > power)
				power = tmp;
		}
		return power;
	}

	public static boolean HasGreaterOrEqualCmdPower(int[] groups, int pwr)
	{
		for (int i : groups)
		{
			int tmp = FrostBot.getInstance().getCmdPower(i);
			if (tmp >= pwr)
				return true;
		}
		return false;
	}

	public static boolean HasCmdPower(int[] groups, int pwr)
	{
		for (int i : groups)
		{
			int tmp = FrostBot.getInstance().getCmdPower(i);
			if (tmp == pwr)
				return true;
		}
		return false;
	}
}
