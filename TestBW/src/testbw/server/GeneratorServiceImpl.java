package testbw.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import testbw.client.GeneratorService;
import testbw.generator.DataGenerator;
import testbw.util.DBManager;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GeneratorServiceImpl extends RemoteServiceServlet implements
		GeneratorService {

	@Override
	public String generateData(String[] properties) {

		// Datenbankverbindung ------------------------------------------------
		DBManager manager = new DBManager(properties);
		manager.connect();

		Connection conn = manager.getConnection();
		Statement st = manager.getStatement();

		DataGenerator generator = new DataGenerator(st, manager);

		try { // Generate data ------------------------------------------------
			generator.generateData();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Generation unsuccessful, input files not found.";
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "Generation unsuccessful, file encoding is not supported.";
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getMessage().contains("violates unique constraint"))
				return "Unique key constraint violation encoutered, please wait ...";
			else
				return "Problem with SQL queries (data generation) ...";
		} catch (IOException e) {
			e.printStackTrace();
			return "Data generation unsuccessful, problem reading or writing files.";
		}

		try { // Close DB connection ------------------------------------------
			st.close();
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem closing connection to database (setup).";
		}

		return "Data successfully generated.";
	}
}
