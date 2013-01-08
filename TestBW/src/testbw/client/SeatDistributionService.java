package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("distribution")
public interface SeatDistributionService extends RemoteService {

	ArrayList<ArrayList<String>> getSeatDistribution(String[] projectInput, String[] queryInput);
}