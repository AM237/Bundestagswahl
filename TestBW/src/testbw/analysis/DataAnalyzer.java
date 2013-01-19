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

	public DataAnalyzer(Statement st, ResultSet rs){
		this.st = st;
		this.rs = rs;
	}

	/**
	 * Returns seat distribution of the Bundestag.
	 */
	public ArrayList<ArrayList<String>> getSeatDistribution(String[] queryInput) throws SQLException {

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahr = queryInput[0];

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
				"   LIMIT (SELECT sitze FROM sitzeprojahr WHERE jahr = '"+jahr+"')), " +
				
				"unfiltered AS ( " +
				"	SELECT partei AS parteiname, COUNT(*) AS sitze " +
				"	FROM sitzzuweisung " +
				"	GROUP BY partei), " +
				
				"filtered AS ( " +
					"SELECT * FROM unfiltered " +
					"WHERE sitze >= 0.05 * (SELECT sum(sitze) FROM unfiltered)) " + 
				
				"SELECT parteiname, (sitze * (SELECT SUM(sitze) FROM unfiltered) / (SELECT SUM(sitze) FROM filtered))::bigint AS sitze " +
				"FROM filtered);");
		
		
		//-- Auswertungsanfrage: Endergebnisse (Erststimmen - wiederverwendbare Tabellen)
		st.executeUpdate("CREATE OR REPLACE VIEW maxvotes AS (" +
				"  SELECT wahlkreis, max(anzahl) AS max " + 
				"  FROM erststimmen " +
				"  WHERE jahr = '"+jahr+"' " +
				"  GROUP BY wahlkreis);");
		
		st.executeUpdate("CREATE OR REPLACE VIEW maxvoteskand AS (" +
				"	SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max " +
				"	FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '"+jahr+"') e " +
				"	ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl);");

		st.executeUpdate("CREATE OR REPLACE VIEW maxvotesuniquekand AS (" +
				"	SELECT wahlkreis, max, min(kandnum) AS kandnum " +
				"	FROM maxvoteskand " +
				"	GROUP BY wahlkreis, max);");
		
		//-- Auswertungsanfrage: Endergebnisse (Erststimmen)
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmenergebnis AS (" +
				
				// Tabellen werden wiederverwendet - > als eigene Views definiert
				/*
				"WITH maxvotes AS ( " + 
				"  SELECT wahlkreis, max(anzahl) AS max " + 
				"  FROM erststimmen " +
				"  WHERE jahr = '"+jahr+"' " +
				"  GROUP BY wahlkreis), " +

				"maxvoteskand AS ( " +
					 "SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max " +
					 "FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '"+jahr+"') e " +
					 "ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl), " +

				"maxvotesuniquekand AS ( " +
					 "SELECT wahlkreis, max, min(kandnum) AS kandnum " +
					 "FROM maxvoteskand " +
					 "GROUP BY wahlkreis, max), " +
				 */
					 
				 
				"WITH parteinsitze AS ( " +
					 "SELECT partei, count(*) AS sitze " +
					 "FROM maxvotesuniquekand m left outer join (SELECT * FROM direktkandidat dk WHERE dk.jahr = '"+jahr+"') d " +
					 "ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis " +
					 "GROUP BY partei) " +

				"SELECT p.name AS parteiname, pn.sitze AS sitze " +
				"FROM parteinsitze pn join partei p " + 
				"ON pn.partei = p.parteinummer);");

		// Gesamtverteilung = zweitstimmenergebnis + ueberhangsmandate -> siehe ueberhangsmandate
		/*
		//-- Auswertung der Gesamtverteilung (Sitze -> Partei)
		st.executeUpdate("CREATE OR REPLACE VIEW gesamtverteilung AS (" +
				"WITH verteilung AS ( " + 
				"	SELECT * " + 
				"	FROM erststimmenergebnis  " +
				"  union all " +
				" 	SELECT * " +
				" 	FROM zweitstimmenergebnis)" +

				"SELECT parteiname, sum(sitze)::float8 AS anteil " +
				"FROM verteilung " +
				"GROUP BY parteiname)");
		 */
		
		// parteien aus dem zweitstimmenergebnis
		st.executeUpdate("CREATE OR REPLACE VIEW ergebnisparteienzweitstimmen AS ( " +
		"	SELECT z.parteiname, p.parteinummer " +
		"	FROM zweitstimmenergebnis z JOIN partei p " +
		"	ON z.parteiname = p.name); ");
		
		st.executeUpdate("CREATE OR REPLACE VIEW ergebnisparteienerststimmen AS ( " +
		"	SELECT e.parteiname, p.parteinummer " +
		"	FROM erststimmenergebnis e JOIN partei p " +
		"	ON e.parteiname = p.name); ");
		
		//Ergaenzung wk with bl info
		st.executeUpdate("CREATE OR REPLACE VIEW wkbundeslandzweitstimmen AS ( " +
		"	SELECT b.abkuerzung AS bundesland, z.wahlkreis AS wahlkreis, z.partei AS partei, z.anzahl AS anzahl " +
		"	FROM  (SELECT * FROM zweitstimmen WHERE jahr = '"+jahr+"') z JOIN (SELECT * FROM wahlkreis WHERE jahr = '"+jahr+"') w  " +
		"	ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer); ");
		
		st.executeUpdate("CREATE OR REPLACE VIEW wkbundeslanderststimmen AS ( " +
		"	SELECT b.abkuerzung AS bundesland, z.wahlkreis AS wahlkreis, z.kandidatennummer AS kandidatennummer, z.anzahl AS anzahl " +
		"	FROM  (SELECT * FROM erststimmen WHERE jahr = '"+jahr+"') z JOIN (SELECT * FROM wahlkreis WHERE jahr = '"+jahr+"') w  " +
		"	ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer); ");
		
		// anzahl stimmen pro "finalisten" partei/kandidaten und bundesland
		st.executeUpdate("CREATE OR REPLACE VIEW parteibluebersichtzweitstimmen AS ( " +
		"	SELECT p.name AS partei, w.bundesland AS bundesland, SUM(w.anzahl)::numeric AS anzahl " +
		"	FROM wkbundeslandzweitstimmen w JOIN partei p ON w.partei = p.parteinummer " +
		"   WHERE w.partei IN (SELECT parteinummer FROM ergebnisparteienzweitstimmen) " +
		"	GROUP BY p.name, w.bundesland);");
		
		st.executeUpdate("CREATE OR REPLACE VIEW parteibluebersichterststimmen AS ( " +
		"	SELECT p.name AS partei, w.bundesland AS bundesland, SUM(w.anzahl)::numeric AS anzahl " +
		"	FROM wkbundeslanderststimmen w JOIN (SELECT * FROM direktkandidat WHERE jahr = '"+jahr+"') d " +
		"	ON w.kandidatennummer = d.kandidatennummer JOIN partei p ON d.partei = p.parteinummer" +
		"   WHERE d.partei IN (SELECT parteinummer FROM ergebnisparteienerststimmen) " +
		"	GROUP BY p.name, w.bundesland);");
		
		
		
		
		// Table meta info
		List<String> tableNames = Arrays.asList(
				"zweitstimmenergebnis",
				"parteibluebersichtzweitstimmen",
				"erststimmenergebnis",
				"parteibluebersichterststimmen"
				);
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Parteiname", "Sitze"),
				Arrays.asList("Parteiname", "Bundesland", "Stimmen"),
				Arrays.asList("Parteiname", "Sitze"),
				Arrays.asList("Parteiname", "Bundesland", "Stimmen")
		);

		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}

		return result;
	}
	
	/**
	 * Get the winners for every Wahlkreis
	 */
	public ArrayList<ArrayList<String>> getWahlkreissieger(String[] queryInput) throws SQLException, NumberFormatException {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahrName = queryInput[0];
		/*
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
		*/
	
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT * FROM partei");
		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmengewinner AS SELECT * FROM partei");
		
		List<String> tableNames = Arrays.asList("erststimmengewinner", 
												"zweitstimmengewinner");
		
		/*
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Wahlkreis", "Kandidatennummer", "Anzahl"),
				Arrays.asList("Wahlkreis", "Partei", "Anzahl")
		);*/
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Parteinummer", "Name"),
				Arrays.asList("Parteinummer", "Name")
		);
		
		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}

		return result;
	}
	
	/**
	 * Return the members of the Bundestag
	 */
	public ArrayList<ArrayList<String>> getMembers(String[] queryInput) throws SQLException{
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahr = queryInput[0];
		
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
		
		*/
		
		st.executeUpdate("CREATE OR REPLACE VIEW mitglieder AS SELECT * FROM partei");
			
		List<String> tableNames = Arrays.asList(
				"mitglieder"
				);
		/*
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Partei", "Name")
		);*/
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Parteinummer", "Name")
		);

		
		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}
		
		
		return result;
	}
	
	/**
	 * Return a view of all Ueberhangsmandate
	 */
	public ArrayList<ArrayList<String>> getUeberhangsmandate(String[] queryInput) throws SQLException {

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahr = queryInput[0];
						
				
				// Ueberhangsmandate
				st.executeUpdate("CREATE OR REPLACE VIEW ueberhangerststimmen AS ( " +
	
						// Also computed in getSeatDistribution -> use those results
						/*
						// groesste Stimmenanzahl fuer jeden Wahlkreis
						"WITH maxvotes AS ( " +
						"  SELECT wahlkreis, max(anzahl) AS max " + 
						"  FROM erststimmen " +
						"  WHERE jahr = '"+jahr+"' " +
						"  GROUP BY wahlkreis), " +
						
						// pro Wahlkreis, Kandidaten mit groessten Stimmenanzahl
						"maxvoteskand AS ( " +
						"	SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max " +
						"	FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '"+jahr+"') e " +
						"	ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl), " +
						
						"maxvotesuniquekand AS ( " + 
						"	SELECT wahlkreis, max, min(kandnum) AS kandnum " +
						"	FROM maxvoteskand " +
						"	GROUP BY wahlkreis, max), " +
						*/
						
						// in wahlkreis x won party y
						"WITH parteigewinner AS ( " +
						"	SELECT m.wahlkreis AS wahlkreis, d.partei AS partei " +
						"	FROM maxvotesuniquekand m left outer join (SELECT * FROM direktkandidat dk WHERE dk.jahr = '"+jahr+"') d " +
						"	ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis), " +
						
						// in bundesland z, wahlkreis x won party y
						"blgewinner AS ( " +
						"	SELECT b.name AS bundesland,  p.wahlkreis AS wahlkreis,  p.partei AS partei " + 
						"	FROM parteigewinner p JOIN (SELECT * FROM wahlkreis WHERE jahr = '"+jahr+"') w ON p.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) " +
						
						"SELECT b.bundesland AS bundesland, p.name AS parteiname, COUNT(*) AS mandate " + 
						"FROM blgewinner b JOIN partei p ON b.partei = p.parteinummer " +
						"GROUP BY b.bundesland, p.name);");
				
				
				st.executeUpdate("CREATE OR REPLACE VIEW ueberhangzweitstimmen AS ( " +
			
						
						// parteien aus dem zweitstimmenergebnis
						"WITH ergebnisparteien AS ( " +
						"	SELECT z.parteiname, p.parteinummer " +
						"	FROM zweitstimmenergebnis z JOIN partei p " +
						"	ON z.parteiname = p.name), " +
						
						
						// iteratoren 1, 3, 5, ... max(sitze) fuer alle parteien im BT
						"iterators AS ( " +
						"	SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM zweitstimmenergebnis), 2) AS iterator ), " +
						
						
						// iteratoren reduziert pro Partei
						"parteiiterators AS ( " +
						" 	SELECT * " +
						"	FROM ergebnisparteien e, iterators i " +
						"	WHERE i.iterator <= 2 * (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = e.parteiname)), " +
						
						
						//anzahl stimmen pro "finalisten" partei und bundesland
						"parteibluebersicht AS ( " +
						"	SELECT b.name AS bundesland, z.partei AS partei, SUM(z.anzahl)::numeric AS anzahl " +
						"	FROM  (SELECT * FROM zweitstimmen WHERE jahr = '"+jahr+"' AND partei IN (SELECT parteinummer FROM ergebnisparteien)) z JOIN (SELECT * FROM wahlkreis WHERE jahr = '"+jahr+"') w  " +
						"	ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer " +
						"	GROUP BY z.partei, b.name), " +
						
						
						// tree: partei x iterator x bundesland builds to result for iterator
						"parteiiteratorbl AS ( " +
						"	SELECT p1.parteiname AS parteiname, p1.parteinummer AS parteinummer, p2.bundesland AS bundesland, (p2.anzahl::numeric / p1.iterator::numeric) + RANDOM() AS itrergebnis " +
						"	FROM parteiiterators p1 JOIN parteibluebersicht p2 " +
						"	ON p1.parteinummer = p2.partei " +
						"   ORDER BY partei ASC, itrergebnis DESC), " +
			
						
						/*
						// auswahl bundeslaender mit groessten zwischenergebnissen
						"filtered AS ( " +
						"	SELECT * " +
						"	FROM parteiiteratorbl p1 " +
						"	WHERE " +
						"		(SELECT COUNT(*) FROM parteiiteratorbl p2 " +
						"		WHERE p1.parteinummer = p2.parteinummer " +
						"		AND p2.itrergebnis > p1.itrergebnis) " +
						
						"		< (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p1.parteiname)) " +
						
						
						// aggregation
						"SELECT parteiname, bundesland, COUNT(*) as mandate " +
						"FROM filtered " +
						"GROUP BY parteiname, parteinummer, bundesland); "
						*/
						
					
						"partitionen AS ( " +
						"	SELECT p.parteiname AS parteiname, p.bundesland AS bundesland, p.itrergebnis AS itrergebnis, ROW_NUMBER() OVER (PARTITION BY p.parteiname ORDER BY p.itrergebnis DESC) AS rn " +
						"	FROM parteiiteratorbl p), " +
						
						"filter AS ( " +
						"	SELECT * FROM partitionen p " +
						"	WHERE p.rn <= (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p.parteiname)) " +
						
						"SELECT f.parteiname AS parteiname, f.bundesland AS bundesland, COUNT(*) as mandate " +
						"FROM filter f " +
						"GROUP BY f.parteiname, f.bundesland);"
						
						);
				
				
				st.executeUpdate("CREATE OR REPLACE VIEW umandate AS ( " +
				
						"WITH unfiltered AS ( " +
						"	SELECT e.bundesland AS bundesland, e.parteiname AS parteiname, e.mandate - z.mandate AS mandate " +
						"	FROM ueberhangerststimmen e JOIN ueberhangzweitstimmen z " +
						"	ON e.bundesland = z.bundesland AND e.parteiname = z.parteiname) " +
						
						"SELECT * FROM unfiltered " +
						"WHERE mandate > 0);"
					
				);
				
				
		
		List<String> tableNames = Arrays.asList(
				"umandate"
		);
		
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Bundesland", "Parteiname", "Ueberhangsmandate")
		);
		

		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}

		return result;
	}
	
	/**
	 * Return overview of the Wahlkreise
	 */
	public ArrayList<ArrayList<String>> getWahlkreisOverview(String[] queryInput) throws SQLException {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		String jahrName = queryInput[0];
		String wahlkreis = queryInput[1];
		
		try {
			
			/*
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsolut AS SELECT sum(anzahl) FROM erststimmen  WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis);

			
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativ AS "
					+ "SELECT (SELECT * FROM wahlbeteiligungabsolut)::float / (  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = "
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
			 */
			
			// Dummy tables
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsolut AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativ AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidat AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolut AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilrelativ AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahr AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilveraenderung AS SELECT * FROM partei");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		List<String> tableNames = Arrays.asList(
				"wahlbeteiligungabsolut",
				"wahlbeteiligungrelativ",
				"erststimmengewinnerkandidat",
				"parteinenanteilabsolut",
				"parteinenanteilrelativ",
				"parteinenanteilabsolutvorjahr",
				"parteinenanteilveraenderung"
				);
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name")
		);

		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}
		
		return result;
	}
	
	/**
	 * Knappster Sieger
	 */
	public ArrayList<ArrayList<String>> getKnappsterSieger(String[] queryInput) throws SQLException {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		String jahrName = queryInput[0];
		String wahlkreis = queryInput[1];
		
		try {
			
			/*
			st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinner AS "
							+ "SELECT s1.wahlkreis, s1.kandidatennummer, d.partei , "
							+ "( SELECT min(s1.anzahl - s2.anzahl) FROM erststimmen s2 WHERE jahr = "
							+ jahrName
							+ " AND s1.anzahl - s2.anzahl > 0 AND s1.wahlkreis = s2.wahlkreis AND s1.kandidatennummer != s2.kandidatennummer) AS differenz"
							+ " FROM erststimmen s1 , direktkandidat d WHERE s1.jahr = "
							+ jahrName
							+ " AND d.jahr = "
							+ jahrName
							+ " AND s1.kandidatennummer = d.kandidatennummer");

					st.executeUpdate("CREATE OR REPLACE VIEW knappsteergebnisse AS "
							+ "(SELECT * FROM knappstegewinner ) UNION "
							+ "(SELECT s1.wahlkreis, s1.kandidatennummer, d.partei , "
							+ " ( SELECT min( differenz ) FROM ( SELECT (s2.anzahl - s1.anzahl) As differenz FROM erststimmen s2 WHERE jahr = "
							+ jahrName
							+ " AND s2.wahlkreis = s1.wahlkreis AND ( s2.anzahl - s1.anzahl ) > 0 ) AS ergebnissedifferenzen )"
							+ " FROM erststimmen s1 , direktkandidat d WHERE s1.jahr = "
							+ jahrName
							+ " AND d.jahr = "
							+ jahrName
							+ " AND s1.kandidatennummer = d.kandidatennummer AND d.partei NOT IN ( SELECT k.partei from knappstegewinner k) )");
			 */
			
			// Dummy tables
			st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinner AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW knappsteergebnisse AS SELECT * FROM partei");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		List<String> tableNames = Arrays.asList(
				"knappstegewinner",
				"knappsteergebnisse"
		);
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name")
		);

		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}
		
		return result;
	}

	/**
	 * Wahlkreis Overview Erststimmen
	 */
	public ArrayList<ArrayList<String>> getOverview(String[] queryInput) throws SQLException {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		String jahrName = queryInput[0];
		String wahlkreis = queryInput[1];

		try {

			/*
					st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsoluteinzelstimmen AS "
							+ "SELECT sum(anzahl) FROM (SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr) AS stimmen WHERE jahr = "
							+ jahrName + " AND wahlkreis = " + wahlkreis);

					st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativeinzelstimmen AS "
							+ "SELECT (SELECT * FROM wahlbeteiligungabsoluteinzelstimmen) / (  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = "
							+ jahrName
							+ " AND wahlkreis = "
							+ wahlkreis
							+ " )::float ;");

					st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidateinzelstimmen AS "
							+ "SELECT name FROM politiker p , direktkandidat d WHERE p.politikernummer = d.politiker AND d.kandidatennummer = (SELECT e.kandidatennummer FROM (SELECT  s1.wahlkreis, s1.kandidatennummer, s1.anzahl FROM ( SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr) s1 , wahlkreis w WHERE s1.jahr = "
							+ jahrName
							+ " AND w.jahr = "
							+ jahrName
							+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = (SELECT max(s2.anzahl) FROM ( SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr) s2 WHERE s2.jahr = "
							+ jahrName
							+ " AND s2.wahlkreis = w.wahlkreisnummer)) e ORDER BY RANDOM() LIMIT 1)");

					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsoluteinzelstimmen AS "
							+ "SELECT p.parteinummer as parteinummer, (SELECT sum(zs.anzahl) FROM (SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr) zs WHERE zs.jahr = "
							+ jahrName
							+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");

					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilrelativeinzelstimmen AS "
							+ "SELECT pa.parteinummer as parteinummer, pa.anzahl/(SELECT * FROM wahlbeteiligungabsoluteinzelstimmen) as anteil FROM parteinenanteilabsoluteinzelstimmen pa ");

					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahreinzelstimmen AS "
							+ "SELECT p.parteinummer, (SELECT sum(zs.anzahl) FROM (SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr) zs WHERE zs.jahr = "
							+ Integer.toString(Integer.parseInt(jahrName) - 4)
							+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");

					st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilveraenderungeinzelstimmen AS "
							+ "SELECT pa1.parteinummer as parteinummer , pa1.anzahl-pa2.anzahl as anzahl FROM parteinenanteilabsolutvorjahreinzelstimmen pa2, parteinenanteilabsoluteinzelstimmen pa1 WHERE pa1.parteinummer = pa2.parteinummer");

			 */

			// Dummy tables
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsoluteinzelstimmen AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativeinzelstimmen AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidateinzelstimmen AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsoluteinzelstimmen AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilrelativeinzelstimmen AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahreinzelstimmen AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilveraenderungeinzelstimmen AS SELECT * FROM partei");
			
		} catch (SQLException e) {
			e.printStackTrace();
		}

		
		List<String> tableNames = Arrays.asList(
				"wahlbeteiligungabsoluteinzelstimmen",
				"wahlbeteiligungrelativeinzelstimmen",
				"erststimmengewinnerkandidateinzelstimmen",
				"parteinenanteilabsoluteinzelstimmen",
				"parteinenanteilrelativeinzelstimmen",
				"parteinenanteilabsolutvorjahreinzelstimmen",
				"parteinenanteilveraenderungeinzelstimmen"
				);
		
		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name")
		);

		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}
		
		return result;
	}
	
	/**
	 * Request vote forms (tables)
	 */
	public ArrayList<ArrayList<String>> requestVote(String[] queryInput) throws SQLException {
		
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahrName = queryInput[0];
		String wahlkreisNr = queryInput[1];
		
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmeliste AS ( " +
				"SELECT p.name AS kandidatenname, t.name AS parteiname " +
				"FROM " +
				"	(SELECT * FROM direktkandidat WHERE jahr = "+jahrName+" AND wahlkreis = "+ wahlkreisNr +") d " +
				"	JOIN politiker p ON p.politikernummer = d.politiker " +
				"   JOIN partei t ON d.partei = t.parteinummer);");
		
		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmeliste AS ( " +
				"SELECT l.listenplatz AS listenplatz, t.name AS parteiname, p.name AS kandidatenname " +
				"FROM " +
				"	(SELECT listenplatz, partei, politiker, bundesland " +
				"    FROM listenkandidat WHERE jahr = "+jahrName+") l " +
				"	JOIN politiker p ON p.politikernummer = l.politiker " +
				"	JOIN (SELECT * FROM wahlkreis WHERE jahr = "+jahrName+") w ON w.bundesland = l.bundesland " +
				"	JOIN partei t ON t.parteinummer = l.partei " +
				"WHERE wahlkreisnummer = "+ wahlkreisNr + " " + 
				"ORDER BY l.listenplatz);");	
	
		List<String> tableNames = Arrays.asList("erststimmeliste", 
												"zweitstimmeliste");

		List<List<String>> colNames = Arrays.asList(
				Arrays.asList("Kandidatenname", "Partei"),
				Arrays.asList("Listenplatz", "Parteiname", "Kandidatenname")
		);
		
		
		for (int i = 0; i < tableNames.size(); i++){
			
			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++){
				header.add(colNames.get(i).get(j));
			}
			
			result.add(header);
			collectFromQuery(result, tableNames.get(i));			
		}

		return result;
	}
	
	
	
	// Get data from ResultSet into required table format
	private void collectFromQuery(ArrayList<ArrayList<String>> result, 
								 String tableName) throws SQLException {
		
		result.add(new ArrayList<String>());
		rs = st.executeQuery("SELECT * FROM " + tableName+ ";");
		ResultSetMetaData meta = rs.getMetaData();
		int anzFields = meta.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < anzFields; i++) {
				result.get(result.size()-1).add(rs.getString(i+1));
			}
			// add delimiter
			result.get(result.size()-1).add("$$");
		}
	}
}

