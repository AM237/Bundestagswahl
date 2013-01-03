package testbw.server;

import java.util.ArrayList;

import testbw.client.AnalysisService;
import testbw.analysis.DataAnalyzer;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class AnalysisServiceImpl extends RemoteServiceServlet implements AnalysisService {
	
	@Override
	public ArrayList<String> getSeatDistribution(String[] properties) {
			
		return new DataAnalyzer().getSeatDistribution(properties);
	}
}
