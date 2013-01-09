package testbw.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface WKOverviewErststimmenServiceAsync {

	void getOverview(String[] projectInput, String[] queryInput,
			AsyncCallback<ArrayList<ArrayList<String>>> callback);

}
