package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("knappsterSieger")
public interface GetKnappsterSiegerService extends RemoteService {

	ArrayList<ArrayList<String>> getKnappsterSieger(String[] projectInput, String[] queryInput);
}