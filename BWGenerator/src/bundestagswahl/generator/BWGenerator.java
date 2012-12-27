package bundestagswahl.generator;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import bundestagswahl.setup.BWSetupDatabase;
import bundestagswahl.setup.CopyProgressMonitor;

public class BWGenerator {

	// public static String progPfad = System.getProperty("user.dir");
	// public static String homePfad = System.getProperty("user.home");

	public static String ergebnis05Pfad = "csv\\kerg2005.csv";
	public static String ergebnis09Pfad = "csv\\kerg2009.csv";

	public static String erststimmen05Pfad = "csv\\erststimmen2005.csv";
	public static String erststimmen09Pfad = "csv\\erststimmen2009.csv";
	public static String zweitstimmen05Pfad = "csv\\zweitstimmen2005.csv";
	public static String zweitstimmen09Pfad = "csv\\zweitstimmen2009.csv";

	public static boolean setupDatabase = false;// Datenbank neu aufsetzen
	public static boolean generateStimmen = false;// Stimmen CSV neu generieren
	public static boolean loadStimmen = false;// Stimmen neu in Datenbank laden
	public static boolean addConstraints = false;// Constraints hinzufügen

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			Class.forName("org.postgresql.Driver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// Datenbankverbindung aufbauen
		String url = "jdbc:postgresql://localhost/Bundestagswahl?user=user&password=1234";
		Connection conn;
		Statement st;

		ResultSet rs = null;

		// Datenbank neu aufsetzen
		if (setupDatabase) {
			String[] init = { "" };
			BWSetupDatabase.main(init);
		}

		// Stimmen pro Wahlkreis aus wkumrechnung200x.csv auslesen
		// und in stimmen200x.csv Einzelstimmen schreiben

		try {
			try {
				conn = DriverManager.getConnection(url);
				st = conn.createStatement();
				CSVReader readerErgebnis[] = new CSVReader[2];
				CSVWriter writerErststimmen[] = new CSVWriter[2];
				CSVWriter writerZweitstimmen[] = new CSVWriter[2];

				if (generateStimmen) {
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
				}

				for (int jahr = 0; jahr < 2; jahr++) {
					String jahrName = Integer.toString(2005 + jahr * 4);

					if (generateStimmen) {
						System.out.println("\nGenerating started");
						String[] readLineErgebnis;

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

						while ((readLineErgebnis = readerErgebnis[jahr]
								.readNext()) != null) {

							if (!readLineErgebnis[0].trim().equals("")
									&& !readLineErgebnis[2].trim().equals("")
									&& !readLineErgebnis[2].equals("99")) {

								int wahlkreisnummer = Integer
										.parseInt(readLineErgebnis[0]);

								String wahlkreisname = readLineErgebnis[1];
								System.out.println("\n" + wahlkreisnummer
										+ " - " + wahlkreisname);// Ladebalken

								int aktelleBundeslandnummer = Integer
										.parseInt(getQueryResult(
												st,
												rs,
												"SELECT bundesland FROM wahlkreis WHERE jahr = "
														+ jahrName
														+ " AND wahlkreisnummer = "
														+ wahlkreisnummer + ";"));

								for (int i = 19; i < 132; i = i + 4) {

									System.out.print(".");// Ladebalken

									String partei = parteienSpalte.get(i);

									int erststimmenAnzahl = 0;
									int zweitstimmenAnzahl = 0;

									if (!readLineErgebnis[i].equals(""))
										erststimmenAnzahl = Integer
												.parseInt(readLineErgebnis[i]);

									if (!readLineErgebnis[i + 2].equals(""))
										zweitstimmenAnzahl = Integer
												.parseInt(readLineErgebnis[i + 2]);

									aktuelleParteinummer = Integer
											.parseInt(getQueryResult(st, rs,
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

											writerZweitstimmen[jahr]
													.writeNext(writeLine);
											zweitstimmzettelnummer++;
										}
									}

									if (erststimmenAnzahl > 0) {
										aktuelleKandidatennummer = Integer
												.parseInt(getQueryResult(
														st,
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
													Integer.toString(wahlkreisnummer)};

											writerErststimmen[jahr]
													.writeNext(writeLine);
											erststimmzettelnummer++;
										}
									}
								}
							}
						}
						writerZweitstimmen[jahr].close();

						writerErststimmen[jahr].close();
						readerErgebnis[jahr].close();

						System.out.println("\nGenerating finished");
					}

					SimpleDateFormat format = new SimpleDateFormat("hh:mm:ss");
					if (loadStimmen) {

						// Bulk Load der
						// ErstStimmen----------------------------------------
						CopyManager copyManager = new CopyManager(
								(BaseConnection) conn);
						String actPfad;
						String progressString;
						String talbeDestination;
						for (int stimme = 0; stimme < 2; stimme++) {
							System.out.println("\nCopying started: "
									+ format.format(new Date()));

							switch (stimme) {
							case 0:
								talbeDestination = "erststimme";
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
								talbeDestination = "zweitstimme";
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

							InputStream in = new BufferedInputStream(
									CopyProgressMonitor.getCopyProgressMonitor(
											actPfad, progressString));
							copyManager.copyIn("COPY " + talbeDestination
									+ " FROM STDIN WITH DELIMITER ';' CSV", in);

							System.out.println("\nCopying finished");
						}
					}
					if (addConstraints) {
						System.out.println("\nAdding Constraints");

						try {
							st.executeUpdate("ALTER TABLE wahlberechtigte  ADD CONSTRAINT wahlkreis FOREIGN KEY (jahr,wahlkreis) REFERENCES wahlkreis(jahr,wahlkreisnummer);");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE direktkandidat  ADD CONSTRAINT politiker FOREIGN KEY (politiker) REFERENCES politiker;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE direktkandidat  ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE direktkandidat  ADD CONSTRAINT wahlkreis FOREIGN KEY (jahr,wahlkreis) REFERENCES wahlkreis(jahr,wahlkreisnummer);");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE listenkandidat  ADD CONSTRAINT partei FOREIGN KEY (partei) REFERENCES partei;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE listenkandidat  ADD CONSTRAINT bundesland FOREIGN KEY (bundesland) REFERENCES bundesland;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE listenkandidat  ADD CONSTRAINT politiker FOREIGN KEY (politiker) REFERENCES politiker;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE erststimme  ADD CONSTRAINT kandidatennummer FOREIGN KEY (kandidatennummer) REFERENCES direktkandidat(kandidatennummer);");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE zweitstimme  ADD CONSTRAINT bundesland FOREIGN  KEY (bundesland) REFERENCES bundesland;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE zweitstimme  ADD CONSTRAINT partei FOREIGN  KEY (partei) REFERENCES partei;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE erststimmen  ADD CONSTRAINT kandidatennummer FOREIGN  KEY (kandidatennummer) REFERENCES direktkandidat(kandidatennummer);");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						try {
							st.executeUpdate("ALTER TABLE zweitstimmen  ADD CONSTRAINT partei FOREIGN  KEY (partei) REFERENCES partei;");
						} catch (SQLException e) {
							e.printStackTrace();
						}
						System.out.println("\nFinished");
					}

				}

				// Stimmen aggregieren
				System.out.println("\n Aggregate Stimmen");

				try {
					st.executeUpdate("INSERT INTO zweitstimmen SELECT jahr, wahlkreis, partei, count(*) FROM zweitstimme GROUP BY wahlkreis, partei,jahr;");

				} catch (SQLException e) {
					e.printStackTrace();
				}
				System.out.println("\nFinished");
				
				
				// Q4: Wahlkreissieger
//				System.out.println("\n Q4: Wahlkreissieger");
//
//				try {
//					st.executeUpdate("SELECT jahr, partei, count(*) FROM zweitstimme GROUP BY wahlkreis,partei,jahr;");
//
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
				System.out.println("\nFinished");
				
				
				
				
				
				
				

				st.close();
				conn.close();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

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
