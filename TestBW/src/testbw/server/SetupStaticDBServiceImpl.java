package testbw.server;

import testbw.client.SetupStaticDBService;
import testbw.setup.BWSetupDatabase;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class SetupStaticDBServiceImpl extends RemoteServiceServlet implements
		SetupStaticDBService {
	
	@Override
	public String setupStaticDB(String[] properties) {
			
		return new BWSetupDatabase().setupDatabase(properties);
	}
}