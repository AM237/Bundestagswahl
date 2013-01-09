package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("mandate")
public interface GetMandateService extends RemoteService {

	ArrayList<ArrayList<String>> getMandate(String[] projectInput, String[] queryInput);
}
