package testbw.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface LoaderServiceAsync {

	void loadData(String[] properties, AsyncCallback<String> callback);
}
