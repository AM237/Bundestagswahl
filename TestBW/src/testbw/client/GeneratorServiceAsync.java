package testbw.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GeneratorServiceAsync {

	void generateStimmen(String[] properties, AsyncCallback<String> callback);
}
