package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("submitVote")
public interface SubmitVoteService extends RemoteService {

	void submitVote(String[] projectInput, String[] queryInput, ArrayList<ArrayList<String>> selection);
}
