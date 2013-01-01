package testbw.generator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

import testbw.util.DBManager;
import testbw.util.InputDirectory;

public class StimmenGenerator {
	
	// Input Dateien
	private static String ergebnis05Pfad = InputDirectory.ergebnis05Pfad;
	private static String ergebnis09Pfad = InputDirectory.ergebnis09Pfad;
	private static String erststimmen05Pfad = InputDirectory.erststimmen05Pfad;
	private static String erststimmen09Pfad = InputDirectory.erststimmen09Pfad;
	private static String zweitstimmen05Pfad = InputDirectory.zweitstimmen05Pfad;
	private static String zweitstimmen09Pfad = InputDirectory.zweitstimmen09Pfad;

	public String generateStimmen(String[] properties){

		CSVReader readerErgebnis[] = new CSVReader[2];
		CSVWriter writerErststimmen[] = new CSVWriter[2];
		CSVWriter writerZweitstimmen[] = new CSVWriter[2];

		try { // CSV Reader/Writer initialisieren -----------------------------

			readerErgebnis[0] = new CSVReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(
							ergebnis05Pfad), "UTF-8")), ';');
			readerErgebnis[1] = new CSVReader(new BufferedReader(
					new InputStreamReader(new FileInputStream(
							ergebnis09Pfad), "UTF-8")), ';');

			writerErststimmen[0] = new CSVWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(
							erststimmen05Pfad), "UTF-8")), ';');
			writerErststimmen[1] = new CSVWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(
							erststimmen09Pfad), "UTF-8")), ';');

			writerZweitstimmen[0] = new CSVWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(
							zweitstimmen05Pfad), "UTF-8")), ';');
			writerZweitstimmen[1] = new CSVWriter(new BufferedWriter(
					new OutputStreamWriter(new FileOutputStream(
							zweitstimmen09Pfad), "UTF-8")), ';');		


		} catch (FileNotFoundException e){
			e.printStackTrace();
			return "Generation unsuccessful, input files not found.";
		} catch (UnsupportedEncodingException e){
			e.printStackTrace();
			return "Generation unsuccessful, file encoding is not supported.";
		}

		// Datenbank Verbindungsdaten -----------------------------------------
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
		ResultSet rs = null;

		try { // Stimmengenerierung --------------------------------------------------
			
			for (int jahr = 0; jahr < 2; jahr++) {
				String jahrName = Integer.toString(2005 + jahr * 4);
				
				System.out.println("Generating started from thread" + Thread.currentThread().getId());
				String[] readLineErgebnis;

				// Wahlberechtigte Tabelle leeren
				st.executeUpdate("DELETE FROM wahlberechtigte;");

				// kerg.csv------------------------------------

				// Anfangszeilen überspringen und Header auslesen
				readerErgebnis[jahr].readNext();
				readerErgebnis[jahr].readNext();
				readLineErgebnis = readerErgebnis[jahr].readNext();
				readerErgebnis[jahr].readNext();
				readerErgebnis[jahr].readNext();

				// Beschriftung einlesen und Spalten Parteien zuordnen
				HashMap<Integer, String> parteienSpalte = new HashMap<Integer, String>();
				for (int i = 19; i < 132; i = i + 4) {
					parteienSpalte.put(i, readLineErgebnis[i]);
				}

				// Benötige Variablen
				int erststimmzettelnummer = 1;
				int zweitstimmzettelnummer = 1;

				int aktuelleKandidatennummer = 0;
				int aktuelleParteinummer = 0;

				while ((readLineErgebnis = readerErgebnis[jahr].readNext()) != null) {

					if (!readLineErgebnis[0].trim().equals("")
							&& !readLineErgebnis[2].trim().equals("")
							&& !readLineErgebnis[2].equals("99")) {

						int wahlkreisnummer = Integer.parseInt(readLineErgebnis[0]);

						String wahlkreisname = readLineErgebnis[1];
						System.out.println("\n" + wahlkreisnummer
								+ " - " + wahlkreisname);// Ladebalken

						int wahlberechtigte = Integer.parseInt(readLineErgebnis[3]);

						System.out.println(wahlberechtigte);

						int aktelleBundeslandnummer = Integer.parseInt(manager.getQueryResult(
										//st,
										rs,
										"SELECT bundesland FROM wahlkreis WHERE jahr = "
												+ jahrName
												+ " AND wahlkreisnummer = "
												+ wahlkreisnummer + ";"));

						// Wahlberechtigte einfuegen
						st.executeUpdate("INSERT INTO wahlberechtigte VALUES ("
								+ jahrName
								+ ","
								+ wahlkreisnummer
								+ "," + wahlberechtigte + ");");

						for (int i = 19; i < 132; i = i + 4) {

							System.out.print(".");// Ladebalken

							String partei = parteienSpalte.get(i);

							int erststimmenAnzahl = 0;
							int zweitstimmenAnzahl = 0;

							if (!readLineErgebnis[i].equals(""))
								erststimmenAnzahl = Integer.parseInt(readLineErgebnis[i]);

							if (!readLineErgebnis[i + 2].equals(""))
								zweitstimmenAnzahl = Integer.parseInt(readLineErgebnis[i + 2]);

							aktuelleParteinummer = Integer.parseInt(manager.getQueryResult(
											//st, 
											rs,
											"SELECT parteinummer FROM partei WHERE name = '"
													+ partei + "';"));

							if (zweitstimmenAnzahl > 0) {

								for (int j = 0; j < zweitstimmenAnzahl; j++) {
									String[] writeLine = {
											jahrName,
											Integer.toString(zweitstimmzettelnummer),
											Integer.toString(aktuelleParteinummer),
											Integer.toString(wahlkreisnummer),
											Integer.toString(aktelleBundeslandnummer) };

									writerZweitstimmen[jahr].writeNext(writeLine);
									zweitstimmzettelnummer++;
								}
							}

							if (erststimmenAnzahl > 0) {
								aktuelleKandidatennummer = Integer.parseInt(manager.getQueryResult(
												//st,
												rs,
												"SELECT kandidatennummer FROM direktkandidat WHERE jahr = "
														+ jahrName
														+ " AND wahlkreis = "
														+ wahlkreisnummer
														+ " AND partei = "
														+ aktuelleParteinummer
														+ ";"));

								for (int j = 0; j < erststimmenAnzahl; j++) {
									String[] writeLine = {
											jahrName,
											Integer.toString(erststimmzettelnummer),
											Integer.toString(aktuelleKandidatennummer),
											Integer.toString(wahlkreisnummer) };

									writerErststimmen[jahr].writeNext(writeLine);
									erststimmzettelnummer++;
								}
							}
						}
					}
				}
				
				writerZweitstimmen[jahr].close();
				writerErststimmen[jahr].close();
				readerErgebnis[jahr].close();

				System.out.println("\nGenerating finished.");
			}
			
		st.close();
		conn.close();
		
		return "Data generation successful.";
			
		} catch (SQLException e) {
			e.printStackTrace();
			if (e.getMessage().contains("violates unique constraint"))
				return "Unique key constraint violation encoutered, please wait ...";
			else return "Problem with SQL setup queries";
		} catch (IOException e) {
			e.printStackTrace();
			return "Data generation unsuccessful, problem reading or writing files.";
		}
	}	
}
