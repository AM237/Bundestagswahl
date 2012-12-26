package testbw.setup;

public class Partei {
	public String name;
	public int parteinummer;

	public Partei(int parteinummer, String name) {
		super();
		this.parteinummer = parteinummer;
		this.name = name.trim();
	}

	public String toString() {
		return this.parteinummer + ",'" + this.name + "'";
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		Partei p = (Partei) obj;
		return p.name == p.name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.name == null) ? 0 : this.name.hashCode());
		return result;
	}
}
