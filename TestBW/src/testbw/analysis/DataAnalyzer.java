package testbw.analysis;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.util.DBManager;

public class DataAnalyzer {
	

	/**
	 * Analyzes data stored in database with SQL queries.
	 * @param properties - DB data container
	 * @return Mapping of parties to number of seats representing the distribution of seats
	 * in the Bundestag
	 */
	public ArrayList<String> getSeatDistribution(String[] properties) {
				
		ArrayList<String> result = new ArrayList<String>();
		
		// Datenbankverbindung ------------------------------------------------
		DBManager manager = new DBManager(properties);
		try {
			manager.connect();
		} catch (SQLException e) {
			e.printStackTrace();
			//return "Setup unsuccessful. Problem setting up connection to database.";
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			//return "Setup unsuccessful. Check JDBC Driver declaration on server side.";
		}
		// Connection conn = manager.getConnection();
		Statement st = manager.getStatement();
		ResultSet rs = null;
		
		
		try { // Auswertung ---------------------------------------------------
			
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
			
			st.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
		}	
		return null;
	}
}

