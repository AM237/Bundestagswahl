package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.client.AnalysisService;
import testbw.util.DBManager;
import testbw.analysis.DataAnalyzer;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class AnalysisServiceImpl extends RemoteServiceServlet implements AnalysisService {
	
	@Override
	public ArrayList<String> getSeatDistribution(String[] properties) {
		
		// Datenbankverbindung ------------------------------------------------
		DBManager manager = new DBManager(properties);
		try {
			manager.connect();
		} catch (SQLException e) {
			e.printStackTrace();
			//return "Setup unsuccessful. Problem setting up connection to database.";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			//return "Setup unsuccessful. Check JDBC Driver declaration on server side.";
		}
		
		// Connection conn = manager.getConnection();
		Statement st = manager.getStatement();
		ResultSet rs = null;
		
		DataAnalyzer analyzer = new DataAnalyzer(st, rs);
		ArrayList<String> dist = null;
		
		try { // Get seat distribution ---------------------------------------
			dist = analyzer.getSeatDistribution();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		try { // Close DB connection ------------------------------------------
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return dist;
	}
}
