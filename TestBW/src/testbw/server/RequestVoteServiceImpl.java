package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.client.RequestVoteService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class RequestVoteServiceImpl extends RemoteServiceServlet implements
		RequestVoteService {

	// Get vote forms (tables)
	public ArrayList<ArrayList<String>> requestVote(String[] projectInput,
			String[] queryInput) {

		// Datenbankverbindung
		DBManager manager = new DBManager(projectInput);
		manager.connect();

		Statement st = manager.getStatement();
		ResultSet rs = null;
		DataAnalyzer analyzer = new DataAnalyzer(st, rs);

		// Query
		try {
			ArrayList<ArrayList<String>> result = analyzer.requestVote(queryInput);
			st.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}
