package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnalysisServiceAsync {

	void getAnalysis(String[] properties, AsyncCallback< ArrayList<String> > callback);

}
