package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("wkoverviewerststimmen")
public interface WKOverviewErststimmenService extends RemoteService {

	ArrayList<ArrayList<String>> getOverview(String[] projectInput, String[] queryInput);
}
