package main.java.de.frigerius.frostbot;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WvWEvents {
	private static final Logger LOGGER = LoggerFactory.getLogger(WvWEvents.class);

	public static boolean CreateNewEvent(String name, Date startTime, Date endTime, int channelid, String creatorUID, String cName) {
		if (startTime.after(endTime))
			return false;
		try (Connection con = FrostBot.getSQLConnection()) {
			con.setAutoCommit(false);
			outdateAll(con, creatorUID);
			insert(con, name, startTime, endTime, channelid, creatorUID, cName);
			con.commit();
			con.setAutoCommit(true);
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return true;
	}

	private static void insert(Connection con, String name, Date startTime, Date endTime, int channelid, String creatorUID, String cName) {
		if (!UserDatabase.AddUser(con, creatorUID, cName))
			return;
		try (PreparedStatement insrt = con.prepareStatement("INSERT INTO Events (EventName, StartTime, EndTime, ChannelID, UserUID) VALUES (?, ?, ?, ?, ?)")) {
			insrt.setString(1, name);
			insrt.setTimestamp(2, new Timestamp(startTime.getTime()));
			insrt.setTimestamp(3, new Timestamp(endTime.getTime()));
			insrt.setInt(4, channelid);
			insrt.setString(5, creatorUID);
			insrt.executeUpdate();
		} catch (SQLException e) {
			LOGGER.error("Error on inserting WvWEvent.");
		}
	}

	private static List<String> select(Connection con) {
		String stmt = "SELECT Events.EventID, Events.EventName, Events.ChannelID, Users.UserName, Events.StartTime, Events.EndTime "
				+ "From Events INNER JOIN Users ON Events.UserUID = Users.UserUID WHERE Events.StartTime <= ? AND Events.EndTime > ? AND IsCanceled = ? order by Events.StartTime ASC";
		try (PreparedStatement sel = con.prepareStatement(stmt)) {
			Date now = new Date();
			Timestamp tNow = new Timestamp(now.getTime());
			sel.setTimestamp(1, tNow);
			sel.setTimestamp(2, tNow);
			sel.setBoolean(3, false);
			ResultSet result = sel.executeQuery();
			List<String> events = new LinkedList<String>();
			while (result.next()) {
				events.add(MakeEventString(result));
			}
			return events;
		} catch (SQLException e) {
			LOGGER.error("Error on select WvWEvent.", e);
		}
		return null;
	}

	public static List<String> GetCurrentEvents() {
		try (Connection con = FrostBot.getSQLConnection()) {
			return select(con);
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return null;
	}

	public static int GetCurrentEventCount() {
		try (Connection con = FrostBot.getSQLConnection()) {
			String sql = "SELECT Count(*) From Events WHERE Events.StartTime <= ? AND Events.EndTime > ? AND IsCanceled = ?";
			try (PreparedStatement stmt = con.prepareStatement(sql)) {
				Date now = new Date();
				Timestamp tNow = new Timestamp(now.getTime());
				stmt.setTimestamp(1, tNow);
				stmt.setTimestamp(2, tNow);
				stmt.setBoolean(3, false);
				ResultSet result = stmt.executeQuery();
				if (result.next()) {
					return result.getInt(1);
				}
			}
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return 0;
	}

	private static String MakeEventString(ResultSet result) {
		try {
			Timestamp stamp = result.getTimestamp("EndTime");
			Date now = Calendar.getInstance().getTime();
			long dif = stamp.getTime() - now.getTime();
			return String.format("ID: %s Veranstalter: \"%s\" Titel: \"%s\" Endet in: %s", result.getInt("EventID"), result.getString("UserName"), result.getString("EventName"),
					getTimeFormat(dif));
		} catch (SQLException e) {
			LOGGER.error("Error on Reading entry.", e);
		}
		return "";
	}

	private static String getTimeFormat(long time) {
		long hours = TimeUnit.MILLISECONDS.toHours(time);
		long min = TimeUnit.MILLISECONDS.toMinutes(time) - 60 * hours;
		return String.format("%02d:%02d", hours, min);
	}

	public static int getChannelId(int id) {
		try (Connection con = FrostBot.getSQLConnection()) {
			return selectEventById(con, id);
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return -1;
	}

	private static int selectEventById(Connection con, int id) {
		try (PreparedStatement sel = con.prepareStatement("SELECT ChannelID FROM Events WHERE EventID = ? AND StartTime <= ? AND EndTime > ? AND IsCanceled = ?")) {
			sel.setInt(1, id);
			Date now = new Date();
			Timestamp tNow = new Timestamp(now.getTime());
			sel.setTimestamp(2, tNow);
			sel.setTimestamp(3, tNow);
			sel.setBoolean(4, false);
			ResultSet result = sel.executeQuery();
			if (result.next()) {
				return result.getInt(1);
			}
		} catch (SQLException e) {
			LOGGER.error("Error on select WvWEvent.", e);
		}
		return -1;
	}

	public static boolean removeEvent(int id, String uid) {
		try (Connection con = FrostBot.getSQLConnection()) {
			return outdate(con, id, uid);
		} catch (SQLException e) {
			LOGGER.error("Couldn't connect to Databse.", e);
		}
		return false;
	}

	// private static boolean remove(Connection con, int id, String uid)
	// {
	// try (PreparedStatement del = con.prepareStatement("DELETE FROM Events WHERE EventID = ? AND UserUID = ?"))
	// {
	// del.setInt(1, id);
	// del.setString(2, uid);
	// return del.executeUpdate() == 1;
	// } catch (SQLException ex)
	// {
	// Logger.error("Error in delete: ", ex);
	// }
	// return false;
	// }
	//
	// private static boolean removeAll(Connection con, String uid)
	// {
	// try (PreparedStatement del = con.prepareStatement("DELETE FROM Events WHERE UserUID = ?"))
	// {
	// del.setString(1, uid);
	// return del.executeUpdate() == 1;
	// } catch (SQLException ex)
	// {
	// Logger.error("Error in delete: ", ex);
	// }
	// return false;
	// }

	private static boolean outdateAll(Connection con, String uid) {
		try (PreparedStatement outdate = con.prepareStatement("UPDATE Events Set IsCanceled = ? WHERE UserUID = ? AND EndTime > ? AND IsCanceled = ?")) {
			Date now = new Date();
			Timestamp tNow = new Timestamp(now.getTime());
			outdate.setBoolean(1, true);
			outdate.setString(2, uid);
			outdate.setTimestamp(3, tNow);
			outdate.setBoolean(4, false);
			return outdate.executeUpdate() == 1;
		} catch (SQLException ex) {
			LOGGER.error("Error in delete: ", ex);
		}
		return false;
	}

	private static boolean outdate(Connection con, int id, String uid) {
		try (PreparedStatement del = con.prepareStatement("UPDATE Events Set IsCanceled = ? WHERE EventID = ? AND UserUID = ?")) {
			del.setBoolean(1, true);
			del.setInt(2, id);
			del.setString(3, uid);
			return del.executeUpdate() == 1;
		} catch (SQLException ex) {
			LOGGER.error("Error in delete: ", ex);
		}
		return false;
	}
}
