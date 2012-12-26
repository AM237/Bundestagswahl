package testbw.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("setupStaticDB")
public interface SetupStaticDBService extends RemoteService {
	
	String setupStaticDB(String[] properties);
}
