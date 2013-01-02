package testbw.server;

import testbw.client.GeneratorService;
import testbw.generator.DataGenerator;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class GeneratorServiceImpl extends RemoteServiceServlet implements
		GeneratorService {

	@Override
	public String generateData(String[] properties) {
		
		return new DataGenerator().generateData(properties);
	}
}
