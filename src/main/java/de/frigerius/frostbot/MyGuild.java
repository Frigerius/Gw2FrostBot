package main.java.de.frigerius.frostbot;

public class MyGuild
{
	private String _name;
	private String _id;
	private int _tsGroupId;

	public MyGuild(String name, String id, int tsGroupId)
	{
		_name = name;
		_id = id;
		_tsGroupId = tsGroupId;
	}

	public String getName()
	{
		return _name;
	}

	public String getID()
	{
		return _id;
	}

	public int getTsGroupId()
	{
		return _tsGroupId;
	}

}
