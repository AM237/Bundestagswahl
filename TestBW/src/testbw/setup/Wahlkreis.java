package testbw.setup;

public class Wahlkreis {
public int wahlkreisnummer;
public int wahlberechtigte;
public String name;
public String bundesland;

public Wahlkreis(int wahlkreisnummer, String name, String bundesland,
		int wahlberechtigte) {
	super();
	this.wahlkreisnummer = wahlkreisnummer;
	this.wahlberechtigte = wahlberechtigte;
	this.name = name;
	this.bundesland = bundesland;
}

public String toString() {
	return this.name + ", " + this.bundesland + ", " + this.wahlkreisnummer
			+ ", " + wahlberechtigte;
	}
}
