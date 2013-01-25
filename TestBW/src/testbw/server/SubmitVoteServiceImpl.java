package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.analysis.DataAnalyzer;
import testbw.client.SubmitVoteService;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SubmitVoteServiceImpl extends RemoteServiceServlet implements SubmitVoteService {

	// Get vote forms (tables)
	public void submitVote(String[] projectInput, String[] queryInput, ArrayList<ArrayList<String>> selection) {

		// Datenbankverbindung
		DBManager manager = new DBManager(projectInput);
		manager.connect();

		Statement st = manager.getStatement();

		ResultSet rs = null;
		DataAnalyzer analyzer = new DataAnalyzer(st, rs);

		// Query
		try {
			analyzer.submitVote(queryInput, selection, manager.getConnection());

			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}
}
