package testbw.util;

import java.sql.*;

public class DBManager {
	
	private String connectUrl;
	private Connection conn;
	private Statement st;
	
	// Konstruktor
	public DBManager(String[] properties){
		this.connectUrl = "jdbc:postgresql://localhost/" +
				properties[0] + "?user="+properties[1]+"&password="+properties[2];		
	}

	// Datenbankverbindung aufbauen
	public void connect() throws ClassNotFoundException, SQLException {
		Class.forName("org.postgresql.Driver");
		conn = DriverManager.getConnection(connectUrl);
		st = conn.createStatement();
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

	// Get-Methoden
	public Connection getConnection() { return conn; }
	public Statement getStatement() { return st; }
}
