package testbw.server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import testbw.client.LoaderService;
import testbw.loader.DataLoader;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoaderServiceImpl extends RemoteServiceServlet implements LoaderService {

	@Override
	public String loadData(String[] properties) {

		// Datenbankverbindung ------------------------------------------------
		DBManager manager = new DBManager(properties);

		manager.connect();

		Connection conn = manager.getConnection();
		Statement st = manager.getStatement();

		DataLoader loader = new DataLoader(conn, st);

		// try { // Load data
		// ----------------------------------------------------
		// loader.loadData();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// return "Problem with SQL queries (loading).";
		// } catch (IOException e) {
		// e.printStackTrace();
		// return "Data loading unsuccessful, problem copying from files.";
		// }

		try { // Aggregate data -----------------------------------------------
			loader.aggregateData();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem with SQL queries (aggregating).";
		}

		// try { // Add constraints
		// ----------------------------------------------
		// loader.addConstraints();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// return "Problem with SQL queries (adding constraints).";
		// }
		//
		// try { // Close DB connection
		// ------------------------------------------
		// st.close();
		// conn.close();
		// } catch (SQLException e) {
		// e.printStackTrace();
		// return "Problem closing connection to database (loading).";
		// }

		return "Data loaded and constrained successfully.";
	}
}