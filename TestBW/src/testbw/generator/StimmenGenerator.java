package testbw.generator;

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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import testbw.setup.CopyProgressMonitor;

public class StimmenGenerator {

	public static String ergebnis05Pfad = "csv\\kerg2005.csv";
	public static String ergebnis09Pfad = "csv\\kerg2009.csv";

	public static String erststimmen05Pfad = "csv\\erststimmen2005.csv";
	public static String erststimmen09Pfad = "csv\\erststimmen2009.csv";
	public static String zweitstimmen05Pfad = "csv\\zweitstimmen2005.csv";
	public static String zweitstimmen09Pfad = "csv\\zweitstimmen2009.csv";
}
