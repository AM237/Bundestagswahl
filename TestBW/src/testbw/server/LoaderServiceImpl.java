package testbw.server;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import testbw.client.LoaderService;
import testbw.loader.DataLoader;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoaderServiceImpl extends RemoteServiceServlet implements
		LoaderService {
	
	@Override
	public String loadData(String[] properties) {
		
		// Datenbankverbindung -----------------------------------------
		DBManager manager = new DBManager(properties);
		try {
			manager.connect();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Loading unsuccessful. Problem setting up connection to database.";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "Loading unsuccessful. Check JDBC Driver declaration on server side.";
		}
		Connection conn = manager.getConnection();
		Statement st = manager.getStatement();
			
		DataLoader loader = new DataLoader(conn, st);
		
		try { // Load data ----------------------------------------------------
			loader.loadData();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem with SQL queries (loading).";
		} catch (IOException e) {
			e.printStackTrace();
			return "Data loading unsuccessful, problem copying from files.";
		}
		
		try { // Add constraints ----------------------------------------------
			loader.addConstraints();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem with SQL queries (adding constraints).";
		}
		
		try { // Close DB connection ------------------------------------------
			st.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem closing connection to database (loading).";
		}
		
		return "Data loaded and constrained successfully.";
	}
}