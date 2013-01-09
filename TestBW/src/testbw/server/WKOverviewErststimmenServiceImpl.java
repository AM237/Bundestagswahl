package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.client.WKOverviewErststimmenService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class WKOverviewErststimmenServiceImpl  extends RemoteServiceServlet implements WKOverviewErststimmenService {

	// Get seat distribution
	public ArrayList<ArrayList<String>> getOverview(String[] projectInput, String[] queryInput) {

		// Datenbankverbindung
		DBManager manager = new DBManager(projectInput);
		try {
			manager.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Statement st = manager.getStatement();
		ResultSet rs = null;
		DataAnalyzer analyzer = new DataAnalyzer(st, rs);

		// Query
		try { 
			ArrayList<ArrayList<String>> result = analyzer.getOverview(queryInput);
			st.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return null;
	}
}
