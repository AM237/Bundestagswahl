package testbw.loader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import testbw.util.CopyProgressMonitor;
import testbw.util.DBManager;
import testbw.util.InputDirectory;

/**
 * Lade Stimmen in die Datenbank, und fuege Constraints hinzu.
 */
public class DataLoader {

	// Input Dateien
	private static String erststimmen05Pfad = InputDirectory.erststimmen05Pfad;
	private static String erststimmen09Pfad = InputDirectory.erststimmen09Pfad;
	private static String zweitstimmen05Pfad = InputDirectory.zweitstimmen05Pfad;
	private static String zweitstimmen09Pfad = InputDirectory.zweitstimmen09Pfad;

	public String loadData(String[] properties){

		// Datenbankverbindung -----------------------------------------
		DBManager manager = new DBManager(properties);
		try {
			manager.connect();
		} catch (SQLException e) {
			e.printStackTrace();
			return "Setup unsuccessful. Problem setting up connection to database.";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return "Setup unsuccessful. Check JDBC Driver declaration on server side.";
		}
		Connection conn = manager.getConnection();
		Statement st = manager.getStatement();

		try { // Stimmen Laden --------------------------------------------------

			SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");

			for (int jahr = 0; jahr < 2; jahr++) {

				// Bulk Load der ErstStimmen----------------------------------------
				CopyManager copyManager = new CopyManager((BaseConnection) conn);
				String actPfad;
				String progressString;
				String tableDestination;
				for (int stimme = 0; stimme < 2; stimme++) {

					System.out.println("\nCopying started: " + format.format(new Date()));

					switch (stimme) {
					case 0:
						tableDestination = "erststimme";
						switch (jahr) {
						case 0:
							actPfad = erststimmen05Pfad;
							progressString = "Erststimmen 2005 laden";
							break;
						default:
							actPfad = erststimmen09Pfad;
							progressString = "Erststimmen 2009 laden";
							break;
						}
						break;
					default:
						tableDestination = "zweitstimme";
						switch (jahr) {
						case 0:
							actPfad = zweitstimmen05Pfad;
							progressString = "Zweitstimmen 2005 laden";
							break;
						default:
							actPfad = zweitstimmen09Pfad;
							progressString = "Zweitstimmen 2009 laden";
							break;
						}
						break;
					}

					System.out.println(tableDestination + " " + actPfad);
					InputStream in = new BufferedInputStream(
							CopyProgressMonitor.getCopyProgressMonitor(
									actPfad, progressString));
					copyManager.copyIn("COPY " + tableDestination
							+ " FROM STDIN WITH DELIMITER ';' CSV", in);

					System.out.println("\nCopying finished");
				}
			}
			
			
			// Constraints ------------------------------------------------
			System.out.println("\nAdding Constraints ... ");

			st.executeUpdate("ALTER TABLE wahlberechtigte ADD CONSTRAINT wahlkreis FOREIGN KEY (jahr,wahlkreis) REFERENCES wahlkreis(jahr,wahlkreisnummer);");
			st.executeUpdate("ALTER TABLE direktkandidat ADD CONSTRAINT politiker FOREIGN KEY (politiker) REFERENCES politiker;");
			st.executeUpdate("ALTER TABLE direktkandidat ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
			st.executeUpdate("ALTER TABLE direktkandidat ADD CONSTRAINT wahlkreis FOREIGN KEY (jahr,wahlkreis) REFERENCES wahlkreis(jahr,wahlkreisnummer);");
			st.executeUpdate("ALTER TABLE listenkandidat ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
			st.executeUpdate("ALTER TABLE listenkandidat ADD CONSTRAINT bundesland FOREIGN KEY (bundesland) REFERENCES bundesland;");
			st.executeUpdate("ALTER TABLE listenkandidat ADD CONSTRAINT politiker FOREIGN KEY (politiker) REFERENCES politiker;");
			st.executeUpdate("ALTER TABLE erststimme ADD CONSTRAINT kandidatennummer FOREIGN KEY (kandidatennummer) REFERENCES direktkandidat(kandidatennummer);");
			st.executeUpdate("ALTER TABLE zweitstimme ADD CONSTRAINT bundesland FOREIGN KEY (bundesland) REFERENCES bundesland;");
			st.executeUpdate("ALTER TABLE zweitstimme ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
			st.executeUpdate("ALTER TABLE erststimmen ADD CONSTRAINT kandidatennummer FOREIGN KEY (kandidatennummer) REFERENCES direktkandidat(kandidatennummer);");
			st.executeUpdate("ALTER TABLE zweitstimmen ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");

			System.out.println("\nFinished adding constraints");

			st.close();
			conn.close();

		} catch (SQLException e) {
			e.printStackTrace();
			return "Problem with SQL queries ...";
		} catch (IOException e) {
			e.printStackTrace();
			return "Data loading unsuccessful, problem copying from files.";
		}

		return "Data successfully loaded.";
	}	
}
