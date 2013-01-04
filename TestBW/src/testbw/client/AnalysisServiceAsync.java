package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnalysisServiceAsync {

	void analyze(String[] projectInput, String[] queryInput, AsyncCallback< ArrayList<String> > callback);
}
