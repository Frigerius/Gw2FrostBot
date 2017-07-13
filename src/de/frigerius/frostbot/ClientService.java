package de.frigerius.frostbot;

import java.util.List;

import com.github.theholywaffle.teamspeak3.api.wrapper.Client;

public abstract class ClientService
{
	abstract void handle(List<Client> clientList);
}
