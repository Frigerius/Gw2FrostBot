package main.java.de.frigerius.frostbot;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Tasks {
	private final ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
	private final MainLoop mainLoop = new MainLoop();

	private final Logger LOGGER = LoggerFactory.getLogger(Tasks.class);

	public void startMainLoop() {
		try {
			service.scheduleAtFixedRate(mainLoop, 0, 4000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			LOGGER.error("Error on Creating Task", e);
		}
	}

	public void stopAll() {
		if (!service.isShutdown())
			service.shutdownNow();
	}

	public void AddTask(ClientService service) {
		mainLoop.AddTask(service);
	}

}
