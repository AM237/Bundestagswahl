
/*package testbw.server;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import testbw.client.AnalysisService;
import testbw.util.DBManager;
import testbw.analysis.DataAnalyzer;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class AnalysisServiceImpl extends RemoteServiceServlet implements AnalysisService {

	private  DBManager manager;
	private  Statement st;
	private  ResultSet rs;
	private  DataAnalyzer analyzer;

	// Initialize analyzer
	public int initialize(String[] projectInput){

		// Datenbankverbindung
		manager = new DBManager(projectInput);
		try {
			manager.connect();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		st = manager.getStatement();
		rs = null;
		analyzer = new DataAnalyzer(st, rs);
		
		return 1;
	}
	
	// Close analyzer
	public int close(){
	
		try { // Close DB connection
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
		
		return 1;
	}

	// Get seat distribution
	public ArrayList<String> getSeatDistribution(String[] queryInput) {
		try { 
			return analyzer.getSeatDistribution(queryInput);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;

	}

	// Get Wahlkreis winners
	public ArrayList<ArrayList<String>> getWahlkreissieger(String[] queryInput){
		try { 
			return analyzer.getWahlkreissieger(queryInput);
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// Get Bundestag members
	public ArrayList<String> getMembers(String[] queryInput){
		try { 
			return analyzer.getMembers(queryInput);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return null;
	}
	
	// Get Ueberhangsmandate
	public ArrayList<String> getMandate(String[] queryInput){
		try {
			return analyzer.getUeberhangsmandate(queryInput);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return null;
	}
}*/
