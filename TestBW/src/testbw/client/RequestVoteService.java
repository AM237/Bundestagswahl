package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("requestVote")
public interface RequestVoteService extends RemoteService {

	ArrayList<ArrayList<String>> requestVote(String[] projectInput, String[] queryInput);
}
