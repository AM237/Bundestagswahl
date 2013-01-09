package testbw.client;

import java.util.ArrayList;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("members")
public interface GetMembersService extends RemoteService {

	ArrayList<ArrayList<String>>getMembers(String[] projectInput, String[] queryInput);
}