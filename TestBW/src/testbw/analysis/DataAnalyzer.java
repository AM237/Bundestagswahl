package testbw.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DataAnalyzer {

	// Datenbankverbindungsdaten
	private Statement st = null;
	private ResultSet rs = null;

	public DataAnalyzer(Statement st, ResultSet rs){
		this.st = st;
		this.rs = rs;
	}

	/**
	 * Analyzes data stored in database with SQL queries.
	 * @return Mapping of parties to number of seats representing the distribution of seats
	 * in the Bundestag
	 */
	public ArrayList<String> getSeatDistribution() throws SQLException {

		ArrayList<String> result = new ArrayList<String>();

		// Auswertung ---------------------------------------------------

		//-- Stimmen aggregrieren
		st.executeUpdate("DELETE FROM erststimmen;");
		st.executeUpdate("DELETE FROM zweitstimmen;");
		st.executeUpdate("INSERT INTO erststimmen SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr ORDER BY wahlkreis, anzahl;");
		st.executeUpdate("INSERT INTO zweitstimmen SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr ORDER BY wahlkreis, anzahl;");


		//-- Auswertungsanfrage: Endergebnisse (Zweitstimmen)
		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmenergebnis AS (" +
				"WITH sitzzuweisung AS ( " + 
				"	SELECT * FROM itrergebnisse " + 
				"	ORDER BY anzahl DESC " +
				"  LIMIT (SELECT sitze FROM sitzeprojahr WHERE jahr = '2013')) " +
				"SELECT partei as parteiname, COUNT(*) as sitze " +
				"FROM sitzzuweisung " +
				"GROUP BY partei); ");


		//-- Auswertungsanfrage: Endergebnisse (Erststimmen)
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmenergebnis AS (" +
				"WITH maxvotes AS ( " + 
				"	SELECT wahlkreis, max(anzahl) AS max " + 
				"	FROM erststimmen " +
				"  WHERE jahr = '2013' " +
				"	GROUP BY wahlkreis), " +

					 "maxvoteskand AS ( " +
					 "SELECT e.kandnum AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max " +
					 "FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '2013') e " +
					 "ON m.wahlkreis = e.wahlkreis AND m.max = e.quantitaet), " +

					 "maxvotesuniquekand AS ( " +
					 "SELECT wahlkreis, max, min(kandnum) AS kandnum " +
					 "FROM maxvoteskand " +
					 "GROUP BY wahlkreis, max), " +

					 "parteinsitze AS ( " +
					 "SELECT partei, count(*) AS sitze " +
					 "FROM maxvotesuniquekand m left outer join (SELECT * FROM direktkandidaten dk WHERE dk.jahr = '2013') d " +
					 "ON m.kandnum = d.kandnum AND m.wahlkreis = d.wahlkreis " +
					 "GROUP BY partei) " +

					 "SELECT p.name AS parteiname, pn.sitze AS sitze " +
					 "FROM parteinsitze pn join parteien p " + 
				"ON pn.partei = p.parteinum);");


		//-- Auswertung der Gesamtverteilung (Sitze -> Partei)
		st.executeUpdate("CREATE OR REPLACE VIEW gesamtverteilung AS (" +
				"WITH verteilung AS ( " + 
				"	SELECT * " + 
				"	FROM erststimmenergebnis  " +
				"  union all " +
				" 	SELECT * " +
				" 	FROM zweitstimmenergebnis)" +	
				"SELECT parteiname, (sum(sitze) * 100 / (SELECT sum(sitze) FROM verteilung)::float8) AS anteil " +
				"FROM verteilung " +
				"GROUP BY parteiname)");

		rs = st.executeQuery("SELECT * FROM gesamtverteilung");

		while(rs.next()) {
			result.add(rs.getString(1));
			result.add(rs.getString(2));
		}

		return result;
	}
}

