package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.benchmark.BenchmarkServlet;
import testbw.client.SeatDistributionService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SeatDistributionServiceImpl extends RemoteServiceServlet implements SeatDistributionService {

	// Get seat distribution
	public ArrayList<ArrayList<String>> getSeatDistribution(String[] projectInput, String[] queryInput) {

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
			ArrayList<ArrayList<String>> result = analyzer.getSeatDistribution(queryInput);

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
