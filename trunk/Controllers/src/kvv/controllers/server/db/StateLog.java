package kvv.controllers.server.db;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;

public class StateLog {

	public static void main(String[] args) throws IOException {
		try {
			logState("controller1", 100, 110);
			logState("controller1", 200, 210);
			print();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static HashMap<String, HashMap<Integer, Integer>> map = new HashMap<String, HashMap<Integer, Integer>>();

	public static synchronized void logState(String controller, int reg, int val) {
		HashMap<Integer, Integer> regs = map.get(controller);
		if (regs == null) {
			regs = new HashMap<Integer, Integer>();
			map.put(controller, regs);
		}
		if (!regs.containsKey(reg) || regs.get(reg).intValue() != val) {
			try {
				_logState(controller, reg, val);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			regs.put(reg, val);
		}
	}

	private static void _logState(String controller, int reg, int val)
			throws SQLException {
		PreparedStatement statement = DbConnection.dbConnection
				.prepareStatement("insert into STATELOG (date, controller, reg, val) "
						+ "values (?,?,?,?)");
		try {
			Timestamp timestamp = new Timestamp(new java.util.Date().getTime());
			statement.setTimestamp(1, timestamp);
			statement.setString(2, controller);
			statement.setInt(3, reg);
			statement.setInt(4, val);
			statement.executeUpdate();
		} finally {
			statement.close();
		}
	}

	private static void print() throws SQLException {
		Statement statement = DbConnection.dbConnection.createStatement();
		try {
			ResultSet rs = statement
					.executeQuery("select date, controller, reg, val from STATELOG");
			while (rs.next()) {
				System.out.println("" + rs.getTimestamp(1) + " "
						+ rs.getString(2) + " " + rs.getInt(3) + " "
						+ rs.getInt(4));
			}
		} finally {
			statement.close();
		}
	}
}
