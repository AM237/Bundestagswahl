package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("wahlkreissieger")
public interface WahlkreissiegerService extends RemoteService {

	ArrayList<ArrayList<String>> getWahlkreissieger(String[] projectInput, String[] queryInput);
}