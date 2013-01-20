package testbw.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class DBManager {

	private String connectUrl;
	private Connection conn;
	private Statement st;

	// Konstruktor
	public DBManager(String[] properties) {
		this.connectUrl = "jdbc:postgresql://localhost/" + properties[0]
				+ "?user=" + properties[1] + "&password=" + properties[2];
	}

	// Datenbankverbindung aufbauen
	public void connect() {
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(connectUrl);
			st = conn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	// Datenbankverbindung trennen
	public void disconnect() {
		try {
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// Gibt das erste Ergebnistupel der Abfrage zurueck
	public String getQueryResult(ResultSet rs, String query)
			throws SQLException {
		String returnString = "";
		rs = st.executeQuery(query);
		if (rs.next()) {
			try {
				returnString = rs.getString(1);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return returnString;
	}

	// Gibt Ausgabe einer Query als Tabelle in der Konsole zurueck
	public static void printQueryResult(Statement st, ResultSet rs, String table)
			throws SQLException {
		// String returnString = "";
		rs = st.executeQuery("SELECT * FROM " + table + " ;");
		System.out.println(" ");
		System.out.println("------------------------------");
		System.out.println(table + " :");
		ResultSetMetaData meta = rs.getMetaData();
		int anzFields = meta.getColumnCount();
		while (rs.next()) {
			try {
				for (int i = 0; i < anzFields; i++) {
					System.out.print(rs.getString(i + 1) + " | ");

				}
				System.out.print("\n");
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		System.out.println("------------------------------");
		System.out.println(" ");
	}

	// Get-Methoden
	public Connection getConnection() {
		return conn;
	}

	public Statement getStatement() {
		return st;
	}
}
