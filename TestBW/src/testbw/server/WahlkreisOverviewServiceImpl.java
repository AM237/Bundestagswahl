package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.benchmark.BenchmarkServlet;
import testbw.client.WahlkreisOverviewService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class WahlkreisOverviewServiceImpl extends RemoteServiceServlet implements WahlkreisOverviewService {

	// Get seat distribution
	public ArrayList<ArrayList<String>> getWKOverview(String[] projectInput, String[] queryInput) {

		// Datenbankverbindung
		DBManager manager = new DBManager(projectInput);
		manager.connect();

		Statement st = manager.getStatement();

		ResultSet rs = null;
		DataAnalyzer analyzer = new DataAnalyzer(st, rs);

		// Query
		try {
			if (BenchmarkServlet.isBenchmark)
				manager.getConnection().setAutoCommit(false);
			ArrayList<ArrayList<String>> result = analyzer.getWahlkreisOverview(queryInput);
			if (BenchmarkServlet.isBenchmark)
				manager.getConnection().commit();

			st.close();
			manager.disconnect();

			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		manager.disconnect();

		return null;
	}
}