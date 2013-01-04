package testbw.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoaderServiceAsync {

	// Load data into database
	void loadData(String[] properties, AsyncCallback<String> callback);
}
