package testbw.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SeatDistributionServiceAsync {

	void getSeatDistribution(String[] projectInput, String[] queryInput,
			AsyncCallback<ArrayList<ArrayList<String>>> callback);

}
