package testbw.util;

public class FieldVerifier {

	/**
	 * Return true iff any part input is empty
	 * @param input - String array
	 * @return - bool
	 */
	public static boolean missingInput(String[] input) {
		for (int i = 0; i < input.length; i++)
			if (input[i].equals(""))
				return true;
		return false;
	}
}
