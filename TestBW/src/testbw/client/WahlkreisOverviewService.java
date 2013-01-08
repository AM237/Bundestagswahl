package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("wahlkreisoverview")
public interface WahlkreisOverviewService extends RemoteService {

	ArrayList<ArrayList<String>> getWKOverview(String[] projectInput, String[] queryInput);
}