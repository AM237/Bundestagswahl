package testbw.util;

import java.util.ArrayList;

import testbw.client.TestBW.WKTableEntry;

public class Parser {

	public ArrayList<WKTableEntry> extractRows(ArrayList<String> toBeParsed, int colLength){
		
		ArrayList<WKTableEntry> result = new ArrayList<WKTableEntry>();
		for (int i = 0; i < toBeParsed.size(); i=i+colLength+1){
			ArrayList<String> temp = new ArrayList<String>();
			for (int j = 0; j < colLength; j++){
				temp.add(toBeParsed.get(i+j));
			}
			result.add(new WKTableEntry(temp));
		}
		
		return result;
	}
}
