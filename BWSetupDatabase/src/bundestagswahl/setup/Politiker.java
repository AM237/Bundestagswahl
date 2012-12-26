package bundestagswahl.setup;

public class Politiker {
	public String name;
	public int politikernummer;

	public Politiker(int politikernummer, String name, String vorname) {
		super();
		this.name = vorname + " " + name;
		this.politikernummer = politikernummer;
	}

	public String toString() {

		return this.politikernummer + ",'" + name + "'";
	}

}
