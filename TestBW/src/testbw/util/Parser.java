package testbw.util;

import java.util.ArrayList;

import testbw.client.TestBW.TableEntry_3;

public class Parser {

	public ArrayList<TableEntry_3> extractRows(ArrayList<String> toBeParsed, int colLength){
		
		ArrayList<TableEntry_3> result = new ArrayList<TableEntry_3>();
		for (int i = 0; i < toBeParsed.size(); i=i+colLength+1){
			ArrayList<String> temp = new ArrayList<String>();
			for (int j = 0; j < colLength; j++){
				temp.add(toBeParsed.get(i+j));
			}
			result.add(new TableEntry_3(temp));
		}
		
		return result;
	}
}
