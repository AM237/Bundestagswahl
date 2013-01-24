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

	public DataAnalyzer(Statement st, ResultSet rs) {
		this.st = st;
		this.rs = rs;
	}

	/**
	 * Returns seat distribution of the Bundestag.
	 */
	public ArrayList<ArrayList<String>> getSeatDistribution(String[] queryInput) throws SQLException {

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahr = queryInput[0];

		// Auswertung ---------------------------------------------------------
		// --------------------------------------------------------------------

		// -- Auswertungsanfrage: Endergebnisse (wiederverwendbare Tabellen)

		st.executeUpdate("DROP VIEW stimmenpropartei CASCADE");

		st.executeUpdate("CREATE OR REPLACE VIEW stimmenpropartei AS ( " + " SELECT partei, sum(anzahl) AS anzahl " + " FROM zweitstimmen " + " WHERE jahr = '" + jahr + "' " + " GROUP BY partei);");

		st.executeUpdate("CREATE OR REPLACE VIEW maxvotes AS (" + "  SELECT wahlkreis, max(anzahl) AS max " + "  FROM erststimmen " + "  WHERE jahr = '" + jahr + "' " + "  GROUP BY wahlkreis);");

		st.executeUpdate("CREATE OR REPLACE VIEW maxvoteskand AS (" + "	SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max "
				+ "	FROM maxvotes m JOIN (SELECT * FROM erststimmen s WHERE s.jahr = '" + jahr + "') e " + "	ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl);");

		st.executeUpdate("CREATE OR REPLACE VIEW maxvotesuniquekand AS (" + "	SELECT wahlkreis, max, min(kandnum) AS kandnum " + "	FROM maxvoteskand " + "	GROUP BY wahlkreis, max);");

		// -- Auswertungsanfrage: Endergebnisse (Zweitstimmen)

		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmenergebnis AS ( " + "WITH divisoren AS ( " + "	SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM sitzeprojahr), 2) AS div), "

		+ "itrergebnisse AS ( " + "	SELECT s.partei AS parteinum,  (s.anzahl / d.div::float8) AS anzahl " + "	FROM divisoren d, stimmenpropartei s " + "	ORDER BY anzahl DESC "
				+ "   LIMIT (SELECT sitze FROM sitzeprojahr WHERE jahr = '" + jahr + "')), "

				+ "unfiltered AS ( " + "	SELECT parteinum, COUNT(*) AS sitze " + "	FROM itrergebnisse " + "	GROUP BY parteinum), "

				+ "filtered AS ( " + "SELECT * FROM unfiltered " + "WHERE sitze >= 0.05 * (SELECT sum(sitze) FROM unfiltered)) "

				+ "SELECT p.name AS parteiname, (sitze * (SELECT SUM(sitze) FROM unfiltered) / (SELECT SUM(sitze) FROM filtered))::bigint AS sitze "
				+ "FROM filtered f JOIN partei p ON f.parteinum = p.parteinummer);");

		// -- Auswertungsanfrage: Endergebnisse (Erststimmen)
		st.executeUpdate("CREATE OR REPLACE VIEW erststimmenergebnis AS (" + "WITH parteinsitze AS ( " + "SELECT partei, COUNT(*) AS sitze "
				+ "FROM maxvotesuniquekand m JOIN (SELECT * FROM direktkandidat dk WHERE dk.jahr = '" + jahr + "') d " + "ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis "
				+ "GROUP BY partei) " +

				"SELECT p.name AS parteiname, pn.sitze AS sitze " + "FROM parteinsitze pn join partei p " + "ON pn.partei = p.parteinummer);");

		// Karten Info -------------------------------------------------------
		// --------------------------------------------------------------------

		st.executeUpdate("CREATE OR REPLACE VIEW parteibluebersichtzweitstimmen AS ( " + "WITH ergebnisparteienzweitstimmen AS ( " + "		SELECT z.parteiname, p.parteinummer "
				+ "		FROM zweitstimmenergebnis z JOIN partei p " + "		ON z.parteiname = p.name), "

				+ "bundeslandzweitstimmen AS ( " + "		SELECT b.abkuerzung AS bundesland, z.partei AS partei, z.anzahl AS anzahl " + "		FROM  (SELECT * FROM zweitstimmen WHERE jahr = '" + jahr
				+ "') z JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahr + "') w  " + "		ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) "

				+ "	SELECT p.name AS partei, b.bundesland AS bundesland, SUM(b.anzahl)::numeric AS anzahl " + "	FROM bundeslandzweitstimmen b JOIN partei p ON b.partei = p.parteinummer "
				+ " WHERE b.partei IN (SELECT parteinummer FROM ergebnisparteienzweitstimmen) " + "	GROUP BY p.name, b.bundesland);");

		st.executeUpdate("CREATE OR REPLACE VIEW parteibluebersichterststimmen AS ( " + "WITH ergebnisparteienerststimmen AS ( " + "		SELECT e.parteiname, p.parteinummer "
				+ "		FROM erststimmenergebnis e JOIN partei p " + "		ON e.parteiname = p.name), "

				+ "bundeslanderststimmen AS ( " + "		SELECT b.abkuerzung AS bundesland, z.kandidatennummer AS kandidatennummer, z.anzahl AS anzahl "
				+ "		FROM  (SELECT * FROM erststimmen WHERE jahr = '" + jahr + "') z JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahr + "') w  "
				+ "		ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) "

				+ "	SELECT p.name AS partei, b.bundesland AS bundesland, SUM(b.anzahl)::numeric AS anzahl " + "	FROM bundeslanderststimmen b JOIN (SELECT * FROM direktkandidat WHERE jahr = '" + jahr
				+ "') d " + "	ON b.kandidatennummer = d.kandidatennummer JOIN partei p ON d.partei = p.parteinummer" + " WHERE d.partei IN (SELECT parteinummer FROM ergebnisparteienerststimmen) "
				+ "	GROUP BY p.name, b.bundesland);");

		// Table meta info
		List<String> tableNames = Arrays.asList("Zweitstimmenergebnis", "parteibluebersichtzweitstimmen", "Erststimmenergebnis", "parteibluebersichterststimmen");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Parteiname", "Sitze"), Arrays.asList("Parteiname", "Bundesland", "Stimmen"), Arrays.asList("Parteiname", "Sitze"),
				Arrays.asList("Parteiname", "Bundesland", "Stimmen"));

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
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
		st.executeUpdate("CREATE OR REPLACE VIEW ueberhangerststimmen AS ( "
				+

				// Also computed in getSeatDistribution -> use those results
				/*
				 * // groesste Stimmenanzahl fuer jeden Wahlkreis
				 * "WITH maxvotes AS ( " +
				 * "  SELECT wahlkreis, max(anzahl) AS max " +
				 * "  FROM erststimmen " + "  WHERE jahr = '"+jahr+"' " +
				 * "  GROUP BY wahlkreis), " +
				 * 
				 * // pro Wahlkreis, Kandidaten mit groessten Stimmenanzahl
				 * "maxvoteskand AS ( " +
				 * "	SELECT e.kandidatennummer AS kandnum, m.wahlkreis AS wahlkreis, m.max AS max "
				 * +
				 * "	FROM maxvotes m left outer join (SELECT * FROM erststimmen s WHERE s.jahr = '"
				 * +jahr+"') e " +
				 * "	ON m.wahlkreis = e.wahlkreis AND m.max = e.anzahl), " +
				 * 
				 * "maxvotesuniquekand AS ( " +
				 * "	SELECT wahlkreis, max, min(kandnum) AS kandnum " +
				 * "	FROM maxvoteskand " + "	GROUP BY wahlkreis, max), " +
				 */

				// in wahlkreis x won party y
				"WITH parteigewinner AS ( " + "	SELECT m.wahlkreis AS wahlkreis, d.partei AS partei " + "	FROM maxvotesuniquekand m left outer join (SELECT * FROM direktkandidat dk WHERE dk.jahr = '"
				+ jahr + "') d " + "	ON m.kandnum = d.kandidatennummer AND m.wahlkreis = d.wahlkreis), "
				+

				// in bundesland z, wahlkreis x won party y
				"blgewinner AS ( " + "	SELECT b.name AS bundesland,  p.wahlkreis AS wahlkreis,  p.partei AS partei " + "	FROM parteigewinner p JOIN (SELECT * FROM wahlkreis WHERE jahr = '" + jahr
				+ "') w ON p.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer) " +

				"SELECT b.bundesland AS bundesland, p.name AS parteiname, COUNT(*) AS mandate " + "FROM blgewinner b JOIN partei p ON b.partei = p.parteinummer " + "GROUP BY b.bundesland, p.name);");

		st.executeUpdate("CREATE OR REPLACE VIEW ueberhangzweitstimmen AS ( "
				+

				// parteien aus dem zweitstimmenergebnis
				"WITH ergebnisparteien AS ( "
				+ "	SELECT z.parteiname, p.parteinummer "
				+ "	FROM zweitstimmenergebnis z JOIN partei p "
				+ "	ON z.parteiname = p.name), "
				+

				// iteratoren 1, 3, 5, ... max(sitze) fuer alle parteien im BT
				"iterators AS ( "
				+ "	SELECT GENERATE_SERIES(1, 2*(SELECT MAX(sitze) FROM zweitstimmenergebnis), 2) AS iterator ), "
				+

				// iteratoren reduziert pro Partei
				"parteiiterators AS ( "
				+ " 	SELECT * "
				+ "	FROM ergebnisparteien e, iterators i "
				+ "	WHERE i.iterator <= 2 * (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = e.parteiname)), "
				+

				// anzahl stimmen pro "finalisten" partei und bundesland
				"parteibluebersicht AS ( "
				+ "	SELECT b.name AS bundesland, z.partei AS partei, SUM(z.anzahl)::numeric AS anzahl "
				+ "	FROM  (SELECT * FROM zweitstimmen WHERE jahr = '"
				+ jahr
				+ "' AND partei IN (SELECT parteinummer FROM ergebnisparteien)) z JOIN (SELECT * FROM wahlkreis WHERE jahr = '"
				+ jahr
				+ "') w  "
				+ "	ON z.wahlkreis = w.wahlkreisnummer JOIN bundesland b ON w.bundesland = b.bundeslandnummer "
				+ "	GROUP BY z.partei, b.name), "
				+

				// tree: partei x iterator x bundesland builds to result for
				// iterator
				"parteiiteratorbl AS ( "
				+ "	SELECT p1.parteiname AS parteiname, p1.parteinummer AS parteinummer, p2.bundesland AS bundesland, (p2.anzahl::numeric / p1.iterator::numeric) + RANDOM() AS itrergebnis "
				+ "	FROM parteiiterators p1 JOIN parteibluebersicht p2 " + "	ON p1.parteinummer = p2.partei " + "   ORDER BY partei ASC, itrergebnis DESC), " +

				/*
				 * // auswahl bundeslaender mit groessten zwischenergebnissen
				 * "filtered AS ( " + "	SELECT * " +
				 * "	FROM parteiiteratorbl p1 " + "	WHERE " +
				 * "		(SELECT COUNT(*) FROM parteiiteratorbl p2 " +
				 * "		WHERE p1.parteinummer = p2.parteinummer " +
				 * "		AND p2.itrergebnis > p1.itrergebnis) " +
				 * 
				 * "		< (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p1.parteiname)) "
				 * +
				 * 
				 * 
				 * // aggregation
				 * "SELECT parteiname, bundesland, COUNT(*) as mandate " +
				 * "FROM filtered " +
				 * "GROUP BY parteiname, parteinummer, bundesland); "
				 */

				"partitionen AS ( "
				+ "	SELECT p.parteiname AS parteiname, p.bundesland AS bundesland, p.itrergebnis AS itrergebnis, ROW_NUMBER() OVER (PARTITION BY p.parteiname ORDER BY p.itrergebnis DESC) AS rn "
				+ "	FROM parteiiteratorbl p), " +

				"filter AS ( " + "	SELECT * FROM partitionen p " + "	WHERE p.rn <= (SELECT sitze FROM zweitstimmenergebnis WHERE parteiname = p.parteiname)) " +

				"SELECT f.parteiname AS parteiname, f.bundesland AS bundesland, COUNT(*) as mandate " + "FROM filter f " + "GROUP BY f.parteiname, f.bundesland);"

		);

		st.executeUpdate("CREATE OR REPLACE VIEW umandate AS ( " +

		"WITH unfiltered AS ( " + "	SELECT e.bundesland AS bundesland, e.parteiname AS parteiname, e.mandate - z.mandate AS mandate " + "	FROM ueberhangerststimmen e JOIN ueberhangzweitstimmen z "
				+ "	ON e.bundesland = z.bundesland AND e.parteiname = z.parteiname) " +

				"SELECT * FROM unfiltered " + "WHERE mandate > 0);"

		);

		List<String> tableNames = Arrays.asList("umandate");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Bundesland", "Parteiname", "Ueberhangsmandate"));

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
				header.add(colNames.get(i).get(j));
			}

			result.add(header);
			collectFromQuery(result, tableNames.get(i));
		}

		return result;
	}

	/**
	 * Get the winners for every Wahlkreis. Dependend on getSeatdistribution -
	 * SQL View maxvotesuniquekand
	 */
	public ArrayList<ArrayList<String>> getWahlkreissieger(String[] queryInput) throws SQLException, NumberFormatException {

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahrName = queryInput[0];

		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnernummern AS SELECT  mvuk.wahlkreis as wahlkreisnummer, d.politiker, d.partei , mvuk.max as anzahl FROM "
				+ "maxvotesuniquekand mvuk, wahlkreis w , direktkandidat d  WHERE d.jahr = " + jahrName + " AND w.jahr = " + jahrName
				+ " AND  d.kandidatennummer = mvuk.kandnum   AND mvuk.wahlkreis = w.wahlkreisnummer");

		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT  w.name as wahlkreisname, p.name as politikername, pa.name as parteiname, esg.anzahl FROM "
				+ "erststimmengewinnernummern esg  , wahlkreis w, partei pa,  politiker p WHERE  w.jahr = " + jahrName
				+ " AND p.politikernummer = esg.politiker  AND pa.parteinummer = esg.partei  AND esg.wahlkreisnummer = w.wahlkreisnummer");

		st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinner AS SELECT  w.name as wahlkreisname, p.name as politikername, pa.name as parteiname, esg.anzahl FROM "
				+ "erststimmengewinnernummern esg , wahlkreis w, partei pa,  politiker p WHERE w.jahr = " + jahrName
				+ " AND p.politikernummer = esg.politiker  AND pa.parteinummer = esg.partei  AND esg.wahlkreisnummer = w.wahlkreisnummer");

		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmengewinner AS SELECT w.name as wahlkreisname, pa.name as parteiname, s1.anzahl FROM zweitstimmen s1 , wahlkreis w, partei pa WHERE s1.jahr = "
				+ jahrName
				+ " AND w.jahr = "
				+ jahrName
				+ " AND pa.parteinummer = s1.partei"
				+ " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = ( SELECT max(s2.anzahl) FROM zweitstimmen s2 WHERE s2.jahr = " + jahrName + " AND s2.wahlkreis = w.wahlkreisnummer)");

		List<String> tableNames = Arrays.asList("erststimmengewinner", "zweitstimmengewinner");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Wahlkreis", "Kandidatennummer", "Partei", "Stimmen"), Arrays.asList("Wahlkreis", "Partei", "Stimmen"));

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
				header.add(colNames.get(i).get(j));
			}

			result.add(header);
			collectFromQuery(result, tableNames.get(i), "ORDER BY wahlkreisname ASC");
		}

		return result;
	}

	/**
	 * Return the members of the Bundestag. Dependend on getSeatdistribution-
	 * SQL View maxvotesuniquekand ; getWahlkreissieger -
	 * erststimmengewinnernummern ; getUeberhangmandate - ueberhangerststimmen,
	 * ueberhangzweitstimmen
	 */

	public ArrayList<ArrayList<String>> getMembers(String[] queryInput) throws SQLException {

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		String jahrName = queryInput[0];

		st.executeUpdate("CREATE OR REPLACE VIEW listenkanidatohnedirektkandidaten AS("
				+ "SELECT lk.partei , lk.bundesland , lk.politiker , lk.listenplatz - "
				+ "(SELECT count(*) FROM erststimmengewinnernummern esg,listenkandidat lk2 WHERE lk2.jahr = "
				+ jahrName
				+ " AND lk.bundesland = lk2.bundesland AND lk.partei = lk2.partei AND esg.politiker = lk2.politiker AND lk2.listenplatz <  lk.listenplatz )  as listenplatz FROM listenkandidat lk WHERE lk.jahr = "
				+ jahrName + " AND lk.politiker NOT IN (SELECT esg.politiker FROM erststimmengewinnernummern esg ))");

		st.executeUpdate("CREATE OR REPLACE VIEW mitglieder AS("
				+ "WITH zsdiff AS ( "
				+ "SELECT zs.parteiname , zs.bundesland, CASE WHEN es.mandate IS NULL THEN zs.mandate ELSE zs.mandate - es.mandate END as mandate FROM ueberhangzweitstimmen zs LEFT JOIN ueberhangerststimmen es ON es.parteiname =  zs.parteiname AND es.bundesland =  zs.bundesland )"
				+ "(SELECT p.name as politikername, pa.name as parteiname FROM  listenkanidatohnedirektkandidaten lk, politiker p , partei pa , bundesland bl , zsdiff WHERE "
				+ "zsdiff.bundesland = bl.name  AND bl.bundeslandnummer = lk.bundesland AND zsdiff.parteiname = pa.name  AND pa.parteinummer = lk.partei AND p.politikernummer = lk.politiker "
				+ " AND lk.listenplatz <= zsdiff.mandate ) UNION (SELECT politikername , parteiname FROM erststimmengewinner ))");

		// Insert Table for each party

		List<String> tableNames = new ArrayList<String>();
		List<List<String>> colNames = new ArrayList<List<String>>();
		List<String> parteiNames = new ArrayList<String>();

		// Insert Table for each party
		rs = st.executeQuery("SELECT parteiname FROM mitglieder  GROUP BY parteiname ORDER BY parteiname ;");
		while (rs.next()) {
			parteiNames.add(rs.getString(1));
		}
		int parteiNummer = 0;
		for (String partei : parteiNames) {
			parteiNummer++;
			st.executeUpdate("CREATE OR REPLACE VIEW mitglieder" + parteiNummer + " AS( SELECT * FROM mitglieder WHERE parteiname = '" + partei + "' )");
			tableNames.add("mitglieder" + parteiNummer);
		}

		for (int i = 0; i < parteiNames.size(); i++) {
			colNames.add(Arrays.asList("Politiker", "Partei"));
		}

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
				header.add(colNames.get(i).get(j));
			}

			result.add(header);
			collectFromQuery(result, tableNames.get(i), "ORDER BY politikername");
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

			// st.executeUpdate("DROP VIEW wahlbeteiligungrelativ CASCADE");
			// st.executeUpdate("DROP VIEW erststimmengewinnerkandidat CASCADE");
			// st.executeUpdate("DROP VIEW parteinenanteilabsolut CASCADE");
			// st.executeUpdate("DROP VIEW parteinenanteilrelativ CASCADE");

			// erststimmengewinnernummern AS SELECT mvuk.wahlkreis as
			// wahlkreisnummer, d.politiker, d.partei , mvuk.max as anzahl

			st.executeUpdate("CREATE OR REPLACE VIEW wahlberechtigtewahlkreis AS ( SELECT wahlkreis, wahlberechtigte FROM wahlberechtigte WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis
					+ " )");

			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungabsolut AS SELECT sum(anzahl) FROM erststimmen  WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreis);

			st.executeUpdate("CREATE OR REPLACE VIEW wahlbeteiligungrelativ AS "
					+ "SELECT (SELECT * FROM wahlbeteiligungabsolut)::float / (  SELECT wahlberechtigte FROM wahlberechtigtewahlkreis )::float ;");

			st.executeUpdate("CREATE OR REPLACE VIEW erststimmengewinnerkandidat AS "
					+ "SELECT name FROM politiker p  WHERE p.politikernummer  = (SELECT e.politiker FROM erststimmengewinnernummern e ORDER BY RANDOM() LIMIT 1)");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolut AS " + "SELECT p.parteinummer as parteinummer, (SELECT sum(zs.anzahl) FROM zweitstimmen zs WHERE zs.jahr = " + jahrName
					+ " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilrelativ AS "
					+ "SELECT pa.parteinummer as parteinummer, pa.anzahl/(SELECT * FROM wahlbeteiligungabsolut) as anteil FROM parteinenanteilabsolut pa ");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahr AS " + "SELECT p.parteinummer, (SELECT sum(zs.anzahl) FROM zweitstimmen zs WHERE zs.jahr = "
					+ Integer.toString(Integer.parseInt(jahrName) - 4) + " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");

			st.executeUpdate("CREATE OR REPLACE VIEW parteinenanteilveraenderung AS "
					+ "SELECT pa1.parteinummer as parteinummer , pa1.anzahl-pa2.anzahl as anzahl FROM parteinenanteilabsolutvorjahr pa2, parteinenanteilabsolut pa1 WHERE pa1.parteinummer = pa2.parteinummer");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<List<String>> tableNames = Arrays.asList(Arrays.asList("wahlbeteiligungabsolut", "wahlbeteiligungrelativ", "erststimmengewinnerkandidat"), Arrays.asList("parteinenanteilabsolut"),
				Arrays.asList("parteinenanteilrelativ"), Arrays.asList("parteinenanteilabsolutvorjahr"), Arrays.asList("parteinenanteilveraenderung"));

		List<String> valueNames = Arrays.asList("Absolute Wahlbeteiligung", "Relative Wahlbeteiligung", "Erststimmengewinner");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Wahlkreisergebnisse", ""), Arrays.asList("Partei", "Zweitstimmen"), Arrays.asList("Partei", "Relativ"),
				Arrays.asList("Partei Vorjahr", "Zweitstimmen"), Arrays.asList("Partei Vorjahr", "Veränderung"));

		for (int i = 0; i < tableNames.size(); i++) {
			for (int k = 0; k < tableNames.get(i).size(); k++) {

				if (k == 0) {
					ArrayList<String> header = new ArrayList<String>();
					header.add("wahlkreisoverview");
					header.add(colNames.get(i).get(0));
					header.add(colNames.get(i).get(1));
					result.add(header);
					result.add(new ArrayList<String>());
				}

				rs = st.executeQuery("SELECT * FROM " + tableNames.get(i).get(k) + ";");
				ResultSetMetaData meta = rs.getMetaData();
				int anzFields = meta.getColumnCount();

				if (i == 0)
					result.get(result.size() - 1).add(valueNames.get(k));

				while (rs.next()) {
					for (int j = 0; j < anzFields; j++) {
						result.get(result.size() - 1).add(rs.getString(j + 1));
					}
					// add delimiter
					result.get(result.size() - 1).add("$$");

				}
			}
		}

		for (List<String> resultlist : result) {
			for (String string : resultlist) {
				System.out.print("  " + string);
			}
			System.out.print("\n");
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
			 * st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinner AS " +
			 * "SELECT s1.wahlkreis, s1.kandidatennummer, d.partei , " +
			 * "( SELECT min(s1.anzahl - s2.anzahl) FROM erststimmen s2 WHERE jahr = "
			 * + jahrName +
			 * " AND s1.anzahl - s2.anzahl > 0 AND s1.wahlkreis = s2.wahlkreis AND s1.kandidatennummer != s2.kandidatennummer) AS differenz"
			 * + " FROM erststimmen s1 , direktkandidat d WHERE s1.jahr = " +
			 * jahrName + " AND d.jahr = " + jahrName +
			 * " AND s1.kandidatennummer = d.kandidatennummer");
			 * 
			 * st.executeUpdate("CREATE OR REPLACE VIEW knappsteergebnisse AS "
			 * + "(SELECT * FROM knappstegewinner ) UNION " +
			 * "(SELECT s1.wahlkreis, s1.kandidatennummer, d.partei , " +
			 * " ( SELECT min( differenz ) FROM ( SELECT (s2.anzahl - s1.anzahl) As differenz FROM erststimmen s2 WHERE jahr = "
			 * + jahrName +
			 * " AND s2.wahlkreis = s1.wahlkreis AND ( s2.anzahl - s1.anzahl ) > 0 ) AS ergebnissedifferenzen )"
			 * + " FROM erststimmen s1 , direktkandidat d WHERE s1.jahr = " +
			 * jahrName + " AND d.jahr = " + jahrName +
			 * " AND s1.kandidatennummer = d.kandidatennummer AND d.partei NOT IN ( SELECT k.partei from knappstegewinner k) )"
			 * );
			 */

			// Dummy tables
			st.executeUpdate("CREATE OR REPLACE VIEW knappstegewinner AS SELECT * FROM partei");
			st.executeUpdate("CREATE OR REPLACE VIEW knappsteergebnisse AS SELECT * FROM partei");

		} catch (SQLException e) {
			e.printStackTrace();
		}

		List<String> tableNames = Arrays.asList("knappstegewinner", "knappsteergebnisse");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Partei", "Name"), Arrays.asList("Partei", "Name"));

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
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
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW wahlbeteiligungabsoluteinzelstimmen AS "
			 * +
			 * "SELECT sum(anzahl) FROM (SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr) AS stimmen WHERE jahr = "
			 * + jahrName + " AND wahlkreis = " + wahlkreis);
			 * 
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW wahlbeteiligungrelativeinzelstimmen AS "
			 * +
			 * "SELECT (SELECT * FROM wahlbeteiligungabsoluteinzelstimmen) / (  SELECT wahlberechtigte FROM wahlberechtigte WHERE jahr = "
			 * + jahrName + " AND wahlkreis = " + wahlkreis + " )::float ;");
			 * 
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW erststimmengewinnerkandidateinzelstimmen AS "
			 * +
			 * "SELECT name FROM politiker p , direktkandidat d WHERE p.politikernummer = d.politiker AND d.kandidatennummer = (SELECT e.kandidatennummer FROM (SELECT  s1.wahlkreis, s1.kandidatennummer, s1.anzahl FROM ( SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr) s1 , wahlkreis w WHERE s1.jahr = "
			 * + jahrName + " AND w.jahr = " + jahrName +
			 * " AND s1.wahlkreis = w.wahlkreisnummer AND s1.anzahl = (SELECT max(s2.anzahl) FROM ( SELECT jahr, wahlkreis, kandidatennummer, count(*) as anzahl  FROM erststimme GROUP BY wahlkreis, kandidatennummer,jahr) s2 WHERE s2.jahr = "
			 * + jahrName +
			 * " AND s2.wahlkreis = w.wahlkreisnummer)) e ORDER BY RANDOM() LIMIT 1)"
			 * );
			 * 
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW parteinenanteilabsoluteinzelstimmen AS "
			 * +
			 * "SELECT p.parteinummer as parteinummer, (SELECT sum(zs.anzahl) FROM (SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr) zs WHERE zs.jahr = "
			 * + jahrName +
			 * " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");
			 * 
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW parteinenanteilrelativeinzelstimmen AS "
			 * +
			 * "SELECT pa.parteinummer as parteinummer, pa.anzahl/(SELECT * FROM wahlbeteiligungabsoluteinzelstimmen) as anteil FROM parteinenanteilabsoluteinzelstimmen pa "
			 * );
			 * 
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW parteinenanteilabsolutvorjahreinzelstimmen AS "
			 * +
			 * "SELECT p.parteinummer, (SELECT sum(zs.anzahl) FROM (SELECT jahr, wahlkreis, partei, count(*) as anzahl  FROM zweitstimme GROUP BY wahlkreis, partei,jahr) zs WHERE zs.jahr = "
			 * + Integer.toString(Integer.parseInt(jahrName) - 4) +
			 * " AND zs.partei =  p.parteinummer) as anzahl  FROM partei p");
			 * 
			 * st.executeUpdate(
			 * "CREATE OR REPLACE VIEW parteinenanteilveraenderungeinzelstimmen AS "
			 * +
			 * "SELECT pa1.parteinummer as parteinummer , pa1.anzahl-pa2.anzahl as anzahl FROM parteinenanteilabsolutvorjahreinzelstimmen pa2, parteinenanteilabsoluteinzelstimmen pa1 WHERE pa1.parteinummer = pa2.parteinummer"
			 * );
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

		List<String> tableNames = Arrays.asList("wahlbeteiligungabsoluteinzelstimmen", "wahlbeteiligungrelativeinzelstimmen", "erststimmengewinnerkandidateinzelstimmen",
				"parteinenanteilabsoluteinzelstimmen", "parteinenanteilrelativeinzelstimmen", "parteinenanteilabsolutvorjahreinzelstimmen", "parteinenanteilveraenderungeinzelstimmen");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Partei", "Name"), Arrays.asList("Partei", "Name"), Arrays.asList("Partei", "Name"), Arrays.asList("Partei", "Name"),
				Arrays.asList("Partei", "Name"), Arrays.asList("Partei", "Name"), Arrays.asList("Partei", "Name"));

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
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

		st.executeUpdate("CREATE OR REPLACE VIEW erststimmeliste AS ( " + "SELECT p.name AS kandidatenname, t.name AS parteiname, p.politikernummer " + "FROM "
				+ "	(SELECT * FROM direktkandidat WHERE jahr = " + jahrName + " AND wahlkreis = " + wahlkreisNr + ") d " + "	JOIN politiker p ON p.politikernummer = d.politiker "
				+ "   JOIN partei t ON d.partei = t.parteinummer);");

		st.executeUpdate("CREATE OR REPLACE VIEW zweitstimmeliste AS ( " + "SELECT l.listenplatz AS listenplatz, t.name AS parteiname, p.name AS kandidatenname, p.politikernummer " + "FROM "
				+ "	(SELECT listenplatz, partei, politiker, bundesland " + "    FROM listenkandidat WHERE jahr = " + jahrName + ") l " + "	JOIN politiker p ON p.politikernummer = l.politiker "
				+ "	JOIN (SELECT * FROM wahlkreis WHERE jahr = " + jahrName + ") w ON w.bundesland = l.bundesland " + "	JOIN partei t ON t.parteinummer = l.partei " + "WHERE wahlkreisnummer = "
				+ wahlkreisNr + " " + "ORDER BY l.listenplatz);");

		List<String> tableNames = Arrays.asList("erststimmeliste", "zweitstimmeliste");

		List<List<String>> colNames = Arrays.asList(Arrays.asList("Kandidatenname", "Parteiname", "Politikernummer"), Arrays.asList("Listenplatz", "Parteiname", "Kandidatenname", "Politikernummer"));

		for (int i = 0; i < tableNames.size(); i++) {

			ArrayList<String> header = new ArrayList<String>();
			header.add(tableNames.get(i));
			for (int j = 0; j < colNames.get(i).size(); j++) {
				header.add(colNames.get(i).get(j));
			}

			result.add(header);
			collectFromQuery(result, tableNames.get(i));
		}

		return result;
	}

	/**
	 * Submit vote forms (tables)
	 */
	public void submitVote(String[] queryInput, ArrayList<ArrayList<String>> selections) throws SQLException {

		String jahrName = queryInput[0];

		// Wahlkreis nr. where voting took place.
		String wahlkreisNr = queryInput[1];
		
		// Verified to be parseable to an integer
		String tan = queryInput[2];

		// 1 Selection per table, should be looped twice, once for the
		// erststimme,
		// and once for the zweitstimme
		for (int i = 0; i < selections.size(); i++) {
			ArrayList<String> currentSelection = selections.get(i);

			String name = currentSelection.get(0);
			String party = currentSelection.get(1);
			String politicianNr = currentSelection.get(2);

			// Debug
			System.out.println("Updating: year: " + jahrName + ", wkNr: " + wahlkreisNr + ", name: " + name + ", party: " + party + ", politicianNr, " + politicianNr + "tan: " + tan);

			/*
			 * Insert update queries here
			 */
		}
	}

	// Get data from ResultSet into required table format

	private void collectFromQuery(ArrayList<ArrayList<String>> result, String tableName, String orderBy) throws SQLException {

		result.add(new ArrayList<String>());
		rs = st.executeQuery("SELECT * FROM " + tableName + " " + orderBy + ";");
		ResultSetMetaData meta = rs.getMetaData();
		int anzFields = meta.getColumnCount();
		while (rs.next()) {
			for (int i = 0; i < anzFields; i++) {
				result.get(result.size() - 1).add(rs.getString(i + 1));
			}
			// add delimiter
			result.get(result.size() - 1).add("$$");
		}
	}

	private void collectFromQuery(ArrayList<ArrayList<String>> result, String tableName) throws SQLException {
		collectFromQuery(result, tableName, "");
	}

}
