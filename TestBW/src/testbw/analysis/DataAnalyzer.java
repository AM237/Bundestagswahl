package testbw.analysis;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class DataAnalyzer {

	// Datenbankverbindungsdaten
	private Statement st = null;
	private ResultSet rs = null;
	private String jahr =  "";
	private String wahlkreis = "";

	public DataAnalyzer(Statement st, ResultSet rs){
		this.st = st;
		this.rs = rs;
	}

	/**
	 * Analyzes data stored in database with SQL queries.
	 * @return Mapping of parties to number of seats representing the distribution of seats
	 * in the Bundestag
	 */
	public ArrayList<String> getSeatDistribution(String[] queryInput) throws SQLException {

		ArrayList<String> result = new ArrayList<String>();
		this.jahr = queryInput[0];
		this.wahlkreis = queryInput[1];

		// Auswertung ---------------------------------------------------

		//-- View 'stimmenpropartei'
		st.executeUpdate("CREATE OR REPLACE VIEW stimmenpropartei AS ( " +
				"SELECT p.name AS partei, t1.anzahl AS anzahl FROM " +
				"((SELECT partei, sum(anzahl) AS anzahl " +
				" FROM zweitstimmen " +
				" WHERE jahr = '"+jahr+"' " +
				" GROUP BY partei) t1 " +
				"JOIN " +
				"partei p ON t1.partei::text = p.parteinummer::text));"); 
		
		//-- Trigger Divisoren -> ItrErgebnisse
		//-- Typ: vordefiniert?
		//-- Verweist auf: stimmenpropartei
		st.executeUpdate("DROP TRIGGER IF EXISTS berechne_ItrErgebnisse ON divisoren CASCADE;");
		st.executeUpdate("DROP FUNCTION IF EXISTS berechneitr() CASCADE;");
		st.executeUpdate("DELETE FROM itrergebnisse;");
		st.executeUpdate("CREATE OR REPLACE FUNCTION berechneitr() RETURNS trigger AS $$ " +
				"BEGIN " +
				"  INSERT INTO itrergebnisse (SELECT partei, (anzahl / NEW.div::float8) AS anzahl FROM stimmenpropartei); " +
				"  RETURN NEW; " +
				"END; " + 
				"$$ LANGUAGE plpgsql;");

		st.executeUpdate("CREATE TRIGGER berechne_ItrErgebnisse " +
				"AFTER INSERT ON divisoren " +
				"FOR EACH ROW " + 
				"EXECUTE PROCEDURE berechneitr();");

		//-- Load: Divisoren
		//-- Typ: Vorberechnung?
		//-- Verweist auf: divisoren, sitzeprojahr
		st.executeUpdate("DELETE FROM divisoren;");
		st.executeUpdate("INSERT INTO divisoren ( SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM sitzeprojahr), 2));");

		//-- Auswertungsanfrage: Endergebnisse (Zweitstimmen)
		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmenergebnis AS (" +
				"WITH sitzzuweisung AS ( " + 
				"	SELECT * FROM itrergebnisse " + 
				"	ORDER BY anzahl DESC " +
				"  LIMIT (SELECT sitze FROM sitzeprojahr WHERE jahr = '"+jahr+"')) " +
				"SELECT partei as parteiname, COUNT(*) as sitze " +
				"FROM sitzzuweisung " +
				"GROUP BY partei); ");


		//-- Auswertungsanfrage: Endergebnisse (Erststimmen)
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmenergebnis AS (" +
				"WITH maxvotes AS ( " + 
				"  SELECT wahlkreis, max(anzahl) AS max " + 
				"  FROM erststimmen " +
				"  WHERE jahr = '"+jahr+"' " +
				"	GROUP BY wahlkreis), " +

					 "maxvoteskand AS ( " +
					 "SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max " +
					 "FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '"+jahr+"') e " +
					 "ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl), " +

					 "maxvotesuniquekand AS ( " +
					 "SELECT wahlkreis, max, min(kandnum) AS kandnum " +
					 "FROM maxvoteskand " +
					 "GROUP BY wahlkreis, max), " +

					 "parteinsitze AS ( " +
					 "SELECT partei, count(*) AS sitze " +
					 "FROM maxvotesuniquekand m left outer join (SELECT * FROM direktkandidat dk WHERE dk.jahr = '"+jahr+"') d " +
					 "ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis " +
					 "GROUP BY partei) " +

					 "SELECT p.name AS parteiname, pn.sitze AS sitze " +
					 "FROM parteinsitze pn join partei p " + 
				"ON pn.partei = p.parteinummer);");


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
	
	/**
	 * Get the winners for every Wahlkreis
	 */
	public ArrayList<ArrayList<String>> getWahlkreissieger(String[] queryInput) throws SQLException, NumberFormatException {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		String jahrName = queryInput[0];
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT  s1.wahlkreis, s1.kandidatennummer, s1.anzahl FROM erststimmen s1 , wahlkreis w WHERE s1.jahr = "
				+ jahrName
				+ " AND w.jahr = "
				+ jahrName
				+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = ( SELECT max(s2.anzahl) FROM erststimmen s2 WHERE s2.jahr = "
				+ jahrName
				+ " AND s2.wahlkreis = w.wahlkreisnummer)");
		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmengewinner AS SELECT s1.wahlkreis, s1.partei, s1.anzahl FROM zweitstimmen s1 , wahlkreis w WHERE s1.jahr = "
				+ jahrName
				+ " AND w.jahr = "
				+ jahrName
				+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = ( SELECT max(s2.anzahl) FROM zweitstimmen s2 WHERE s2.jahr = "
				+ jahrName
				+ " AND s2.wahlkreis = w.wahlkreisnummer)");
		
	
		List<String> tableNames = Arrays.asList("erststimmengewinner", 
												"zweitstimmengewinner");
		
		for (int i = 0; i < tableNames.size(); i++){
			result.add(new ArrayList<String>());
			collectFromQuery(result, tableNames.get(i), i);			
		}

		return result;
	}
	
	/**
	 * Return the members of the Bundestag
	 */
	public ArrayList<String> getMembers(String[] queryInput) throws SQLException{
		
		ArrayList<String> result = new ArrayList<String>();
		
		/*
		st.executeUpdate("CREATE OR REPLACE VIEW mitgliedererststimme AS ( SELECT esg.kandidatennummer, d.partei FROM erststimmengewinner esg, direktkandidat d WHERE esg.kandidatennummer = d.kandidatennummer)");

		st.executeUpdate("CREATE OR REPLACE VIEW mitglieder AS("
				+ "WITH verteilung AS ( "
				+ "	SELECT * "
				+ "	FROM erststimmenergebnis  "
				+ "  union all "
				+ " 	SELECT * "
				+ " 	FROM zweitstimmenergebnis)"
				+ "SELECT lk.politiker, pa.parteinummer   FROM listenkandidat lk, politiker p,verteilung v, partei pa WHERE pa.name =  v.parteiname AND lk.partei = pa.parteinummer AND lk.listenplatz => v.sitze- (SELECT count(*) FROM mitgliedererststimme mes WHERE mes.partei = pa.parteinummer )"
				+ "UNION (SELECT * FROM mitgliedererststimme) )");
		
		rs = st.executeQuery("SELECT * FROM mitglieder");
		 */
		
		rs = st.executeQuery("SELECT * FROM partei");
		
		while (rs.next()) {
			ResultSetMetaData meta = rs.getMetaData();
			int anzFields = meta.getColumnCount();
			for (int i = 0; i < anzFields; i++) {
				result.add(rs.getString(i+1));
			}
			// add delimiter
			result.add("$$");
		}

		return result;
	}
	
	/**
	 * Return a view of all Ueberhangsmandate
	 */
	public ArrayList<String> getUeberhangsmandate(String[] queryInput) throws SQLException {

		ArrayList<String> result = new ArrayList<String>();
		
		st.executeUpdate("CREATE OR REPLACE VIEW ueberhangsmandate AS "
				+ "SELECT pes.parteiname, pes.sitze - pzs.sitze AS ueberhangsmandate FROM erststimmenergebnis pes, zweitstimmenergebnis pzs WHERE pzs.parteiname = pes.parteiname AND (pes.sitze - pzs.sitze) > 0 ");

		rs = st.executeQuery("SELECT * FROM ueberhangsmandate");
		
		while (rs.next()) {
			ResultSetMetaData meta = rs.getMetaData();
			int anzFields = meta.getColumnCount();
			for (int i = 0; i < anzFields; i++) {
				result.add(rs.getString(i+1));
			}
			// add delimiter
			result.add("$$");
		}

		return result;
	}
	
	/**
	 * Return overview of the Wahlkreise
	 */
	
	public ArrayList<String> getWahlkreisOverview(String[] queryInput) throws SQLException {
		
		String jahrName = this.jahr;
		try {
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsolut AS "
					+ "SELECT sum(anzahl) FROM erststimmen WHERE jahr = "
					+ jahrName + " AND wahlkreis = " + wahlkreis);

			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativ AS "
					+ "SELECT (SELECT * FROM wahlbeteiligungabsolut) / (  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = "
					+ jahrName
					+ " AND wahlkreis = "
					+ wahlkreis
					+ " )::float ;");

			st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidat AS "
					+ "SELECT name FROM politiker p , direktkandidat d WHERE p.politikernummer = d.politiker AND d.kandidatennummer = (SELECT e.kandidatennummer FROM erststimmengewinner e ORDER BY RANDOM() LIMIT 1)");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolut AS "
					+ "SELECT p.parteinummer as parteinummer, (SELECT sum(zs.anzahl) FROM zweitstimmen zs WHERE zs.jahr = "
					+ jahrName
					+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilrelativ AS "
					+ "SELECT pa.parteinummer as parteinummer, pa.anzahl/(SELECT * FROM wahlbeteiligungabsolut) as anteil FROM parteinenanteilabsolut pa ");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahr AS "
					+ "SELECT p.parteinummer, (SELECT sum(zs.anzahl) FROM zweitstimmen zs WHERE zs.jahr = "
					+ Integer.toString(Integer.parseInt(jahrName) - 4)
					+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilveraenderung AS "
					+ "SELECT pa1.parteinummer as parteinummer , pa1.anzahl-pa2.anzahl as anzahl FROM parteinenanteilabsolutvorjahr pa2, parteinenanteilabsolut pa1 WHERE pa1.parteinummer = pa2.parteinummer");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		/*
		printQueryResult(st, rs, "wahlbeteiligungabsolut");
		printQueryResult(st, rs, "wahlbeteiligungrelativ");
		printQueryResult(st, rs, "erststimmengewinnerkandidat");
		printQueryResult(st, rs, "parteinenanteilabsolut");
		printQueryResult(st, rs, "parteinenanteilrelativ");
		printQueryResult(st, rs, "parteinenanteilabsolutvorjahr");
		printQueryResult(st, rs, "parteinenanteilveraenderung");
		 */
		
		return null;
	}
	
	// Get data from ResultSet into required table format
	public void collectFromQuery(ArrayList<ArrayList<String>> result, 
								 String tableName, int index) throws SQLException {
		
		rs = st.executeQuery("SELECT * FROM " + tableName+ ";");
		ResultSetMetaData meta = rs.getMetaData();
		int anzFields = meta.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < anzFields; i++) {
				result.get(index).add(rs.getString(i+1));
			}
			// add delimiter
			result.get(index).add("$$");
		}
	}
}

