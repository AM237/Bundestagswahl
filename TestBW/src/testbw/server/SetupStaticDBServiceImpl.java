package testbw.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import testbw.client.SetupStaticDBService;
import testbw.setup.BWSetupDatabase;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SetupStaticDBServiceImpl extends RemoteServiceServlet implements
		SetupStaticDBService {

	@Override
	public String setupStaticDB(String[] properties) {

		// Datenbankverbindung ------------------------------------------------
		DBManager manager = new DBManager(properties);
		manager.connect();

		Connection conn = manager.getConnection();
		Statement st = manager.getStatement();

		BWSetupDatabase setup = new BWSetupDatabase(st, manager);

		try { // Setup database -----------------------------------------------
			setup.setupDatabase();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Setup unsuccessful, input files not found.";
		} catch (IOException e) {
			e.printStackTrace();
			return "Setup unsuccessful, error opening or reading input files.";
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getMessage().contains(
					"violates unique constraint \"pg_type_typname_nsp_index\""))
				return "Index constraint violation encountered, please wait ...";
			else
				return "Problem with SQL setup queries";
		}

		try { // Close DB connection ------------------------------------------
			st.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem closing connection to database (setup).";
		}

		return "Setup successful.";
	}
}