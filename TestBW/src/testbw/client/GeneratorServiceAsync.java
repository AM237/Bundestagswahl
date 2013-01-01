package testbw.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GeneratorServiceAsync {

	void generateData(String[] properties, AsyncCallback<String> callback);
}
