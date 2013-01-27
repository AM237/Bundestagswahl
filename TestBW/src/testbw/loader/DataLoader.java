package testbw.loader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import testbw.util.CopyProgressMonitor;
import testbw.util.InputDirectory;

public class DataLoader {

	// Input Dateien
	private static String erststimmen05Pfad = InputDirectory.erststimmen05Pfad;
	private static String erststimmen09Pfad = InputDirectory.erststimmen09Pfad;
	private static String zweitstimmen05Pfad = InputDirectory.zweitstimmen05Pfad;
	private static String zweitstimmen09Pfad = InputDirectory.zweitstimmen09Pfad;

	// Datenbankverbindungsdaten
	private Connection conn = null;
	private Statement st = null;

	public DataLoader(Connection conn, Statement st) {
		this.conn = conn;
		this.st = st;
	}

	/**
	 * Load data into the database
	 */
	public void loadData() throws SQLException, IOException {

		// Stimmen Laden --------------------------------------------------
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
				InputStream in = new BufferedInputStream(CopyProgressMonitor.getCopyProgressMonitor(actPfad, progressString));

				try {
					copyManager.copyIn("COPY " + tableDestination + " FROM STDIN WITH DELIMITER ';' CSV", in);
				} catch (Exception e) {
				}

				System.out.println("\nCopying finished");

				System.out.println("\nGenerating TANs");

				st.executeUpdate("DROP TABLE IF EXISTS wahltan CASCADE;");
				st.executeUpdate("CREATE TABLE wahltan(tan integer, valid boolean, voting bigint ,PRIMARY KEY (tan))WITH (OIDS=FALSE);");

				int tan = 0;
				ResultSet rs;
				for (int i = 0; i < 1000; i++) {
					boolean newTan = false;
					while (!newTan) {
						tan = (int) (100000000 + (Math.random() * (Integer.MAX_VALUE - 100000001)));
						rs = st.executeQuery("SELECT * FROM wahltan WHERE tan = " + tan);
						newTan = !rs.next();
					}
					st.executeUpdate("INSERT INTO wahltan VALUES (" + tan + ",true,0)");
				}
				System.out.println("\nGenerating TANs finished");

			}
		}
	}

	/**
	 * Aggregate votes on Wahlkreis level
	 */
	public void aggregateData() throws SQLException {

		System.out.print("Aggregating votes .... ");

		st.executeUpdate("DELETE FROM erststimmen;");
		st.executeUpdate("DELETE FROM zweitstimmen;");

		try {
			st.executeUpdate("INSERT INTO erststimmen SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr ORDER BY wahlkreis, anzahl;");
		} catch (Exception e) {
		}
		try {
			st.executeUpdate("INSERT INTO zweitstimmen SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr ORDER BY wahlkreis, anzahl;");
		} catch (Exception e) {
		}
		System.out.println("finished.");

	}

	/**
	 * Add constraints to all base tables
	 */
	public void addConstraints() throws SQLException {

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
	}
}
