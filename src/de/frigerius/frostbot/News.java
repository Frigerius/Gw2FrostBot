package de.frigerius.frostbot;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class News
{
	private final Logger LOGGER;
	private ReentrantLock _lock;
	private String _msg;
	private String _fileName;

	public News(String fileName)
	{
		LOGGER = LoggerFactory.getLogger(News.class);
		_lock = new ReentrantLock();
		_msg = "";
		_fileName = fileName;
		refresh();
	}

	public boolean refresh()
	{
		_lock.lock();
		try
		{
			File f = new File(_fileName);
			if (f.exists())
			{

				String content = new String(Files.readAllBytes(Paths.get(_fileName)));
				_msg = content;

			} else
			{
				_msg = "";
			}
		} catch (Exception e)
		{
			LOGGER.error("Error in refresh news", e);
			_msg = "";
			return false;
		} finally
		{
			_lock.unlock();
		}
		return true;
	}

	public String getMsg()
	{
		_lock.lock();
		try
		{
			String toReturn = _msg.toString();
			return toReturn;
		} finally
		{
			_lock.unlock();
		}
	}

}
