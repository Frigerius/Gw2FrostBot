package main.java.de.frigerius.frostbot.exceptions;

public class ChannelAlreadyHasBotException extends Exception
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1311993834730410367L;

	public ChannelAlreadyHasBotException()
	{
	}

	public ChannelAlreadyHasBotException(String message)
	{
		super(message);
	}

	public ChannelAlreadyHasBotException(Throwable cause)
	{
		super(cause);
	}

	public ChannelAlreadyHasBotException(String message, Throwable cause)
	{
		super(message, cause);
	}

	public ChannelAlreadyHasBotException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
	{
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
