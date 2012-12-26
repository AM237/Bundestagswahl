package bundestagswahl.setup;

public class Listenkandidat {
	public int partei;
	public int jahr;
	public int bundesland;
	public int politiker;
	public int listenplatz;

	public Listenkandidat(int jahr, int partei, int bundesland,
			int listenplatz, int politiker) {
		super();
		this.jahr = jahr;

		this.partei = partei;
		this.bundesland = bundesland;
		this.listenplatz = listenplatz;

		this.politiker = politiker;
	}

	public String toString() {
		return this.jahr + "," + this.partei + "," + bundesland + ", "
				+ listenplatz + ",  " + politiker;
	}
}
