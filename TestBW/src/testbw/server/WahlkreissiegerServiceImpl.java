package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.client.WahlkreissiegerService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class WahlkreissiegerServiceImpl extends RemoteServiceServlet implements
		WahlkreissiegerService {

	// Get seat distribution
	public ArrayList<ArrayList<String>> getWahlkreissieger(
			String[] projectInput, String[] queryInput) {

		// Datenbankverbindung
		DBManager manager = new DBManager(projectInput);

		manager.connect();

		Statement st = manager.getStatement();
		ResultSet rs = null;
		DataAnalyzer analyzer = new DataAnalyzer(st, rs);

		// Query
		try {
			ArrayList<ArrayList<String>> result = analyzer
					.getWahlkreissieger(queryInput);
			st.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
}