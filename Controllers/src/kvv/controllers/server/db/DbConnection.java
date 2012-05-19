package kvv.controllers.server.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DbConnection {
	public static Connection dbConnection;

	public static void createDB() throws SQLException {
		Statement statement = null;
		statement = dbConnection.createStatement();
		try {
			/*
			 * try { statement.execute("drop table STATELOG"); } catch
			 * (SQLException e) { }
			 */
			statement.execute("CREATE table STATELOG ("
					+ "ID INTEGER PRIMARY KEY GENERATED ALWAYS AS IDENTITY, "
					+ "date TIMESTAMP, controller VARCHAR(30), "
					+ "reg INTEGER, val INTEGER)");
		} catch (Exception e) {
		} finally {
			statement.close();
		}
	}

}
