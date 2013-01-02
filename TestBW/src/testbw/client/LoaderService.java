package testbw.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("loader")
public interface LoaderService extends RemoteService {

  String loadData(String[] properties);
}
