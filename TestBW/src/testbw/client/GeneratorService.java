package testbw.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("generator")
public interface GeneratorService extends RemoteService {

  String generateStimmen(String[] properties);
}
