package testbw.setup;

public class Stimme {
	public int stimmzettelnummer;
	public int kandidatennummer;
	public String partei;
	public String bundesland;

	public Stimme(int stimmzettelnummer, int kandidatennummer, String partei,
			String bundesland) {
		super();
		this.stimmzettelnummer = stimmzettelnummer;
		this.kandidatennummer = kandidatennummer;
		this.partei = partei;
		this.bundesland = bundesland;
	}
}
