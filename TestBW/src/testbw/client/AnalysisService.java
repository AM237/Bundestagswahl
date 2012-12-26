package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("analysis")
public interface AnalysisService extends RemoteService {

  ArrayList<String> getAnalysis(String[] properties);
}