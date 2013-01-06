package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.client.GetMandateService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GetMandateServiceImpl  extends RemoteServiceServlet implements GetMandateService {

	// Get seat distribution
	public ArrayList<String> getMandate(String[] projectInput, String[] queryInput) {

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
			ArrayList<String> result = analyzer.getUeberhangsmandate(queryInput);
			st.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return null;
	}
}
