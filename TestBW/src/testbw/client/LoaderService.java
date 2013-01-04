package testbw.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("loader")
public interface LoaderService extends RemoteService {

	// Load data into database
	String loadData(String[] properties);
}
