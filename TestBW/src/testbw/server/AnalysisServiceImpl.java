package testbw.server;

import java.util.ArrayList;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

import testbw.client.AnalysisService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class AnalysisServiceImpl extends RemoteServiceServlet implements
		AnalysisService {

	@Override
	public ArrayList<String> getAnalysis(String[] properties) {
		
		String postgresqlurl = "jdbc:postgresql://localhost/" +
				properties[0] + "?user="+properties[1]+"&password="+properties[2];
		Connection conn;
		Statement st;
		ResultSet rs = null;
		
		ArrayList<String> result = new ArrayList<String>();
		
		try {
			
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(postgresqlurl);
			st = conn.createStatement();
			
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
					 "	SELECT wahlkreis, max(quantitaet) AS max " + 
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
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	
		return null;
	}

}
