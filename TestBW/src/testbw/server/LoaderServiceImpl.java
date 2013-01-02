package testbw.server;

import testbw.client.LoaderService;
import testbw.loader.DataLoader;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class LoaderServiceImpl extends RemoteServiceServlet implements
		LoaderService {
	
	@Override
	public String loadData(String[] properties) {
			
		return new DataLoader().loadData(properties);
	}
}