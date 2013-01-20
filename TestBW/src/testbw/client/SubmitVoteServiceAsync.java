package testbw.client;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface SubmitVoteServiceAsync {

	void submitVote(String[] projectInput, String[] queryInput,
			ArrayList<ArrayList<String>> selection, AsyncCallback<Void> callback);

}
