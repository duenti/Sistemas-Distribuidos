package appl;

import java.util.List;
import java.util.Map;

public class RoundRobin {
	private static int current=0;
	
	public static String[] next(List<String>slavesIDs, Map<String, String[]>slaves){
		
		if(current>=slavesIDs.size()) current =0;
		String[] result = slaves.get(slavesIDs.get(current));
		//System.err.println("round robin... " + result[0] + result[1]);
		current++;
		return result; 
	}

}
