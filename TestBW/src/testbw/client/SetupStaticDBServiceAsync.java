package testbw.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SetupStaticDBServiceAsync {

	void setupStaticDB(String[] properties, AsyncCallback<String> callback);

}
