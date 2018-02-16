package main.java.de.frigerius.frostbot.commands;

import java.util.HashSet;
import java.util.concurrent.locks.ReentrantLock;

/**
 * A user can use this command only once at same time.
 * 
 * @author Vinzenz
 *
 */
public abstract class RequestingBaseCommand extends BaseCommand {

	private HashSet<String> _clientUIDs = new HashSet<>();
	private ReentrantLock _lock = new ReentrantLock();

	public RequestingBaseCommand(String command, int cmdPwr) {
		super(command, cmdPwr);
	}

	/**
	 * @param uid
	 *            Reqeuster-uid
	 * @return true if added, false else
	 */
	protected boolean AddRequester(String uid) {
		_lock.lock();
		try {
			if (_clientUIDs.contains(uid)) {
				return false;
			} else {
				_clientUIDs.add(uid);
				return true;
			}
		} finally {
			_lock.unlock();
		}
	}

	/**
	 * Removes a requester.
	 * 
	 * @param uid
	 *            Requester-uid
	 */
	protected void RemoveRequester(String uid) {
		_lock.lock();
		try {
			_clientUIDs.remove(uid);
		} finally {
			_lock.unlock();
		}
	}
}
