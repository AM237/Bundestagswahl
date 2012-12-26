package testbw.setup;

public class Direktkandidat {
	public int jahr;
	public int partei;
	public int kandidatennummer;
	public int politiker;
	public int wahlkreis;

	public Direktkandidat(int jahr, int kandidatennummer, int partei,
			int politiker, int wahlkreis) {
		super();
		this.jahr = jahr;
		this.partei = partei;
		this.kandidatennummer = kandidatennummer;
		this.politiker = politiker;
		this.wahlkreis = wahlkreis;
	}

	public String toString() {
		return this.jahr + "," + kandidatennummer + "," + politiker + ","
				+ this.partei + "," + this.wahlkreis;
	}
}
