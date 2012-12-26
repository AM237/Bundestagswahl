package testbw.server;

import testbw.client.SetupStaticDBService;
import testbw.setup.Direktkandidat;
import testbw.setup.Listenkandidat;
import testbw.setup.Partei;
import testbw.setup.Politiker;
import au.com.bytecode.opencsv.CSVReader;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.sql.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressWarnings("serial")
public class SetupStaticDBServiceImpl extends RemoteServiceServlet implements
		SetupStaticDBService {
	
	// CSV Dateien
	// Anpassungen in den CSV Dateien:
	// Alle UTF-8 Format
	// wahlergebniss2009.csv: VIOLETTEN zu DIE VIOLETTEN wie in kerg umbenannt
	// Volksabst. zu Volksabstimmung wie in kerg umbenannt
	// Tierschutzpartei -"-
	public static String wahlbewerber05Pfad = "..\\csv\\wahlbewerber2005.csv";//"csv\\wahlbewerber2005.csv";
	public static String wahlbewerber09Pfad = "..\\csv\\wahlbewerber2009.csv";
	public static String ergebnis05Pfad = "..\\csv\\StruktBtwkr2005.csv";
	public static String ergebnis09Pfad = "..\\csv\\StruktBtwkr2009.csv";

	@Override
	public String setupStaticDB(String[] properties) {
		
		// Setup Bundeslaender
		SortedMap<String, String> bundeslaenderAbkuerzung = new TreeMap<String, String>();
		bundeslaenderAbkuerzung.put("Baden-W�rttemberg", "BW");
		bundeslaenderAbkuerzung.put("Bayern", "BY");
		bundeslaenderAbkuerzung.put("Berlin", "BE");
		bundeslaenderAbkuerzung.put("Brandenburg", "BB");
		bundeslaenderAbkuerzung.put("Bremen", "HB");
		bundeslaenderAbkuerzung.put("Hamburg", "HH");
		bundeslaenderAbkuerzung.put("Hessen", "HE");
		bundeslaenderAbkuerzung.put("Mecklenburg-Vorpommern", "MV");
		bundeslaenderAbkuerzung.put("Niedersachsen", "NI");
		bundeslaenderAbkuerzung.put("Nordrhein-Westfalen", "NW");
		bundeslaenderAbkuerzung.put("Rheinland-Pfalz", "RP");
		bundeslaenderAbkuerzung.put("Saarland", "SL");
		bundeslaenderAbkuerzung.put("Sachsen", "SN");
		bundeslaenderAbkuerzung.put("Sachsen-Anhalt", "ST");
		bundeslaenderAbkuerzung.put("Schleswig-Holstein", "SH");
		bundeslaenderAbkuerzung.put("Th�ringen", "TH");
		
		try{
			
			// Verbindung zu CSV Daten aufbauen
			CSVReader readerWahlbewerber[] = new CSVReader[2];
			readerWahlbewerber[0] = new CSVReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(
							wahlbewerber05Pfad), "UTF-8")), ';');
			readerWahlbewerber[1] = new CSVReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(
							wahlbewerber09Pfad), "UTF-8")), ';');

			CSVReader readerErgebnis[] = new CSVReader[2];
			readerErgebnis[0] = new CSVReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(ergebnis05Pfad),
							"UTF-8")), ';');
			readerErgebnis[1] = new CSVReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(ergebnis09Pfad),
							"UTF-8")), ';');

			String[] readLineErgebnis;
			String[] readLineWahlbewerber;
		
			
			// Datenbank Verbindungsdaten
			String postgresqlurl = "jdbc:postgresql://localhost/" +
					properties[0] + "?user="+properties[1]+"&password="+properties[2];
			Connection conn;
			Statement st;
			ResultSet rs = null;
			
			
			try {
			
				// Datenbankverbindung aufbauen
				Class.forName("org.postgresql.Driver");
				conn = DriverManager.getConnection(postgresqlurl);
				st = conn.createStatement();
				
				
				// Tabellen loeschen, Reihenfolge relevant---------------------
				st.executeUpdate("DROP TABLE IF EXISTS erststimme CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS zweitstimme CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS listenkandidat CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS direktkandidat CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS wahlberechtigte CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS wahlkreis CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS politiker CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS partei CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS bundesland CASCADE;");
				st.executeUpdate("DROP TABLE IF EXISTS stimmenpropartei CASCADE;");
				
				
				// Tabellen anlegen, Reihenfolge relevant----------------------
				st.executeUpdate("CREATE TABLE bundesland(bundeslandnummer integer , name text NOT NULL, abkuerzung text, PRIMARY KEY (bundeslandnummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE partei( parteinummer integer, name text , PRIMARY KEY (parteinummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE politiker( politikernummer integer, name text , PRIMARY KEY (politikernummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE wahlkreis( jahr integer, wahlkreisnummer integer, name text, bundesland integer , PRIMARY KEY (jahr,wahlkreisnummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE wahlberechtigte(jahr integer, wahlkreis integer UNIQUE,wahlberechtigte integer , PRIMARY KEY (jahr,wahlkreis))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE direktkandidat(jahr integer,kandidatennummer integer UNIQUE, politiker integer, partei int, wahlkreis integer, PRIMARY KEY (jahr,kandidatennummer)) WITH ( OIDS=FALSE );");
				st.executeUpdate("CREATE TABLE listenkandidat(jahr integer, partei integer, bundesland int, listenplatz integer, politiker integer, PRIMARY KEY (jahr,partei,bundesland,listenplatz)) WITH ( OIDS=FALSE );");
				st.executeUpdate("CREATE TABLE erststimme( jahr integer, stimmzettelnummer integer UNIQUE, kandidatennummer integer, PRIMARY KEY (Jahr,stimmzettelnummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE zweitstimme( jahr integer, stimmzettelnummer integer UNIQUE, partei integer, bundesland integer, PRIMARY KEY (Jahr,stimmzettelnummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE erststimmen( jahr integer, kandidatennummer integer UNIQUE, anzahl integer, PRIMARY KEY (Jahr,kandidatennummer))WITH (OIDS=FALSE);");
				st.executeUpdate("CREATE TABLE zweitstimmen( jahr integer, partei integer UNIQUE, anzahl integer, PRIMARY KEY (Jahr,partei))WITH (OIDS=FALSE);");

				
				// Tabelle Bundesl�nder f�llen---------------------------------
				int bundeslandnummer = 1;
				for (Map.Entry<String, String> entry : bundeslaenderAbkuerzung
						.entrySet()) {
					st.executeUpdate("INSERT INTO bundesland VALUES ("
							+ bundeslandnummer + ",'"
							+ (String) entry.getKey() + "','"
							+ (String) entry.getValue() + "');");
					bundeslandnummer++;
				}
				
				
				// Wahlkreise anlegen------------------------------------------
				// Anfangszeilen �berspringen
				// Statische Daten und Ergebnisse eintragen
				HashSet<Partei> parteien = new HashSet<Partei>();
				HashSet<Politiker> politiker = new HashSet<Politiker>();
				HashSet<Direktkandidat> direktkandidaten = new HashSet<Direktkandidat>();
				HashSet<Listenkandidat> listenkandidaten = new HashSet<Listenkandidat>();

				int kandidatennummer = 1;

				for (int jahr = 0; jahr < 2; jahr++) {
					String jahrName = Integer.toString(2005 + jahr * 4);

					// ReadLineErgebnis
					while ((readLineErgebnis = readerErgebnis[jahr]
							.readNext()) != null) {
						
						// In Datei stehen Wahlkreise vor Bundeslandnamen
						// Deswegen zwischenspeichern der Wahlkreise
						if (!readLineErgebnis[0].trim().equals("")
								&& !readLineErgebnis[1].trim().equals("0")
								&& !readLineErgebnis[1].trim().equals("")
								&& !readLineErgebnis[2].trim().equals("")
								&& !(Integer.parseInt(readLineErgebnis[1]) > 900)) {

							String bundesland = readLineErgebnis[0];
							bundeslandnummer = Integer
									.parseInt(getQueryResult(st, rs,
											"SELECT bundeslandnummer FROM bundesland WHERE name = '"
													+ bundesland
													+ "' OR abkuerzung = '"
													+ bundesland + "' ;"));

							// Neuen Wahlkreis hinzuf�gen
							int wahlkreisnummer = Integer
									.parseInt(readLineErgebnis[1]);
							String wahlkreisname = readLineErgebnis[2];
							st.executeUpdate("INSERT INTO wahlkreis VALUES ("
									+ jahrName
									+ ","
									+ wahlkreisnummer
									+ ",'"
									+ wahlkreisname
									+ "',"
									+ bundeslandnummer + ");");
						}
					}
					
					readerErgebnis[jahr].close();

					// Anfangszeilen �berspringen
					int skipLinesWahlbewerber = 1; // Anzahl Zeilen
					for (int j = 1; j <= skipLinesWahlbewerber; j++) {
						readerWahlbewerber[jahr].readNext();
					}
					

					// ReadLineWahlbewerber (fuelle Datenstrukturen)
					while ((readLineWahlbewerber = readerWahlbewerber[jahr]
							.readNext()) != null) {
						if (readLineWahlbewerber.length >= 1) {

							int parteinummer = 0;
							int politikernummer = 0;
							String parteiname = "";
							String wahlkreisnummer = "";
							String bundesland = "";
							String listenplatz = "";
							String politikername = "";
							String politikervorname = "";

							switch (jahr) {
							case 0:
								parteiname = readLineWahlbewerber[7].trim();
								wahlkreisnummer = readLineWahlbewerber[8]
										.trim();
								bundesland = readLineWahlbewerber[9].trim();
								listenplatz = readLineWahlbewerber[10]
										.trim();
								politikername = readLineWahlbewerber[0];
								String[] temp;
								temp = politikername.split(",");
								politikername = temp[0];
								politikervorname = temp[1];
								break;
							case 1:
								parteiname = readLineWahlbewerber[3].trim();
								wahlkreisnummer = readLineWahlbewerber[4]
										.trim();
								bundesland = readLineWahlbewerber[5].trim();
								listenplatz = readLineWahlbewerber[6]
										.trim();
								politikername = readLineWahlbewerber[0];
								politikervorname = readLineWahlbewerber[1];
								break;
							}

							// Politiker hinzuf�gen
							if (!politikername.equals("")) {
								boolean exists = false;
								for (Politiker actPolitiker : politiker) {
									if (actPolitiker.name
											.equals(parteiname)) {
										politikernummer = actPolitiker.politikernummer;
										exists = true;
										break;
									}
								}
								if (!exists) {
									politikernummer = politiker.size() + 1;
									politiker.add(new Politiker(
											politikernummer, politikername,
											politikervorname));
								}
							}

							// Parteien hinzuf�gen
							if (!parteiname.equals("")) {
								boolean exists = false;
								for (Partei actPartei : parteien) {
									if (actPartei.name.equals(parteiname)) {
										parteinummer = actPartei.parteinummer;
										exists = true;
										break;
									}
								}
								if (!exists) {
									parteinummer = parteien.size() + 1;
									Partei partei = new Partei(
											parteinummer, parteiname);
									parteien.add(partei);
								}
							}

							// Direktkandidaten hinzuf�gen
							if (!wahlkreisnummer.equals("")) {
								direktkandidaten.add(new Direktkandidat(
										2005 + jahr * 4, kandidatennummer,
										parteinummer, politikernummer,
										Integer.parseInt(wahlkreisnummer)));
								kandidatennummer++;
							}
							// Listenkandidaten hinzuf�gen
							if (!bundesland.equals("")) {
								bundeslandnummer = Integer
										.parseInt(getQueryResult(
												st,
												rs,
												"SELECT bundeslandnummer FROM bundesland WHERE name = '"
														+ bundesland
														+ "' OR abkuerzung = '"
														+ bundesland
														+ "' ;"));

								listenkandidaten.add(new Listenkandidat(
										2005 + jahr * 4, parteinummer,
										bundeslandnummer, Integer
										.parseInt(listenplatz),
										politikernummer));
							}
						}
						
					} // end while(readLineBewerber)
					
				} // end for loop

				
				// Ung�ltige und �brige Parteien aufl�sen
				int parteinummer = parteien.size() + 1;
				int politikernummer = politiker.size() + 1;
				parteien.add(new Partei(0, "Ung�ltige"));
				parteien.add(new Partei(parteinummer, "�brige"));
				politiker.add(new Politiker(politikernummer, "Politiker", "�briger "));
				
				
				
				// Daten in Tabellen schreiben---------------------------------
				Iterator<Politiker> itrPolitiker = politiker.iterator();
				while (itrPolitiker.hasNext()) {
					st.executeUpdate("INSERT INTO politiker VALUES ("
							+ itrPolitiker.next() + ");");
				}

				Iterator<Partei> itrPartei = parteien.iterator();
				while (itrPartei.hasNext()) {
					st.executeUpdate("INSERT INTO partei VALUES ("
							+ itrPartei.next() + ");");
				}

				Iterator<Direktkandidat> itrDirektkandidat = direktkandidaten
						.iterator();
				while (itrDirektkandidat.hasNext()) {
					st.executeUpdate("INSERT INTO direktkandidat VALUES ("
							+ itrDirektkandidat.next() + ");");
				}

				Iterator<Listenkandidat> itrListenkandidat = listenkandidaten
						.iterator();
				while (itrListenkandidat.hasNext()) {
					st.executeUpdate("INSERT INTO listenkandidat VALUES ("
							+ itrListenkandidat.next() + ");");
				}

				for (int jahr = 0; jahr < 2; jahr++) {
					String jahrName = Integer.toString(2005 + jahr * 4);
					// Direktkandidaten der �brigen Parteien generieren
					int anzahlWahlkreise = Integer.parseInt(getQueryResult(
							st, rs,
							"SELECT count(*) FROM wahlkreis WHERE jahr = "
									+ jahrName + ";"));
					for (int j = 0; j < anzahlWahlkreise; j++) {
						st.executeUpdate("INSERT INTO direktkandidat VALUES ("
								+ jahrName
								+ ","
								+ kandidatennummer
								+ ","
								+ politikernummer
								+ ","
								+ parteinummer
								+ "," + (j + 1) + ");");
						kandidatennummer++;
					}
				}
				
				
				
				
				
				
				
				
				
				
			
			//-- SitzeProJahr
			//-- Typ: vordefiniert
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS sitzeprojahr CASCADE;");
			st.executeUpdate("CREATE TABLE sitzeprojahr ( jahr INTEGER PRIMARY KEY, sitze INTEGER NOT NULL);");
			st.executeUpdate("INSERT INTO sitzeprojahr VALUES ('2013', '700');");
			st.executeUpdate("INSERT INTO sitzeprojahr VALUES ('2005', '598');");
			st.executeUpdate("INSERT INTO sitzeprojahr VALUES ('2008', '598');");
						
			//-- Divisoren
			//-- Typ: vordefiniert
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS divisoren CASCADE;");
			st.executeUpdate("DROP SEQUENCE IF EXISTS divisoren_div_seq CASCADE;");
			st.executeUpdate("CREATE TABLE divisoren ( div SERIAL PRIMARY KEY);");
			st.executeUpdate("ALTER SEQUENCE divisoren_div_seq RESTART WITH 1 INCREMENT BY 2;");
			
			
			//-- Trigger Divisoren -> Divisoren
			//-- Typ: vordefiniert
			//-- Verweist auf: sitzeprojahr
			st.executeUpdate("DROP TRIGGER IF EXISTS fill_divisoren ON divisoren CASCADE;");
			st.executeUpdate("DROP FUNCTION IF EXISTS trigfill() CASCADE;");
			
			st.executeUpdate("CREATE OR REPLACE FUNCTION trigfill() RETURNS trigger AS $$" +
							 "DECLARE divcount INTEGER; " + 
							 "DECLARE sitzecount INTEGER; " + 
							 "BEGIN " +
							 "  SELECT COUNT(*) INTO divcount FROM divisoren; " +
							 //"  SELECT sitze INTO sitzecount FROM sitzeprojahr WHERE jahr = 2013; " +
							 "  SELECT MAX(sitze) INTO sitzecount FROM sitzeprojahr; " +
							 "  IF divcount < sitzecount THEN " +
							 "  INSERT INTO divisoren VALUES(DEFAULT); " +
							 "  END IF; " + 
							 "  RETURN NEW; " +
							 "END; " + 
							 "$$ LANGUAGE plpgsql;");
			
			st.executeUpdate("CREATE TRIGGER fill_divisoren " +
							 "AFTER INSERT ON divisoren " +
					         "FOR EACH ROW " + 
							 "EXECUTE PROCEDURE trigfill();");
			
			
			//-- ItrErgebnisse
			//-- Typ: vordefiniert
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS itrergebnisse CASCADE;");
			st.executeUpdate("CREATE TABLE itrergebnisse ( partei CHARACTER VARYING NOT NULL, anzahl NUMERIC NOT NULL, PRIMARY KEY(partei, anzahl));");
			
			
			/*
			//-- StimmenProPartei [Mock]
			//-- Typ: wird berechnet (mehrmals) -> Initialisiere mit 0
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS sitzepropartei CASCADE;");
			st.executeUpdate("DROP TABLE IF EXISTS stimmenpropartei CASCADE;");
			st.executeUpdate("CREATE TABLE stimmenpropartei ( partei CHARACTER VARYING PRIMARY KEY, anzahl INTEGER);");
			st.executeUpdate("INSERT INTO stimmenpropartei VALUES ('X', '5200'), ('Y', '1700'), ('Z', '3100');");
			*/
			
			
			//-- Trigger Divisoren -> ItrErgebnisse
			//-- Typ: vordefiniert
			//-- Verweist auf: stimmenpropartei
			st.executeUpdate("DROP TRIGGER IF EXISTS berechne_ItrErgebnisse ON divisoren CASCADE;");
			st.executeUpdate("DROP FUNCTION IF EXISTS berechneitr() CASCADE;");
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
			//-- Typ: Vorberechnung
			//-- Verweist auf: divisoren
			st.executeUpdate("INSERT INTO divisoren VALUES (DEFAULT);");
			
			
			/*
			//-- Erststimmen [Mock]
			//-- Typ: wird berechnet (periodisch aus grosser 'Erststimme' Relation) -> Initialisiere mit 0
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS erststimmen CASCADE;");
			st.executeUpdate("CREATE TABLE erststimmen ( jahr INTEGER, kandnum INTEGER, wahlkreis INTEGER, " + 
								"quantitaet INTEGER, CONSTRAINT erststimmen_pkey PRIMARY KEY (jahr, kandnum, wahlkreis));");
			
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '1', '11', '5');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '2', '11', '3');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '3', '11', '8');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '4', '12', '1');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '5', '12', '20');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '6', '12', '3');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '7', '13', '12');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '8', '13', '5');");
			st.executeUpdate("INSERT INTO erststimmen VALUES ('2013', '9', '13', '4');");
			
			
			//-- Direktkandidaten [Mock]
			//-- Typ: vordefiniert
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS direktkandidaten CASCADE;");
			st.executeUpdate("CREATE TABLE direktkandidaten( jahr INTEGER, kandnum INTEGER, polnr INTEGER, partei INTEGER, wahlkreis INTEGER, " + 
							 "CONSTRAINT direktkandidaten_pkey PRIMARY KEY (jahr, kandnum));");
			
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '1', '1', '21', '11');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '2', '2', '22', '11');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '3', '3', '23', '11');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '4', '4', '21', '12');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '5', '5', '22', '12');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '6', '6', '23', '12');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '7', '7', '21', '13');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '8', '8', '22', '13');");
			st.executeUpdate("INSERT INTO direktkandidaten VALUES ('2013', '9', '9', '23', '13');");
			
			//-- Parteien [Mock]
			//-- Typ: vordefiniert
			//-- Verweist auf: -
			st.executeUpdate("DROP TABLE IF EXISTS parteien CASCADE;");
			
			st.executeUpdate("CREATE TABLE parteien ( parteinum INTEGER PRIMARY KEY, name CHARACTER VARYING NOT NULL);");
			st.executeUpdate("INSERT INTO parteien VALUES ('21', 'X'), ('22', 'Y'), ('23', 'Z');");
			*/
			
			st.close();
			
			return "Setup successful.";

			} catch (SQLException e) {
				e.printStackTrace();
				return "Setup unsuccessful. Problem with SQL setup queries.";
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
				return "Setup unsuccessful. Check JDBC Driver declaration on server side.";
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return "Setup unsuccessful, input files not found.";
		} catch (IOException e) {
			e.printStackTrace();
			return "Setup unsuccessful, error reading input files.";
		}
	}
	
	
	
	/**
	 * Gibt den ResultSet fuer eine Query und einen Statement zurueck.
	 */
	public static String getQueryResult(Statement st, ResultSet rs, String query)
			throws SQLException {
		String returnString = "";
		rs = st.executeQuery(query);
		if (rs.next()) {
			try {
				returnString = rs.getString(1);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		return returnString;
	}
}