package bundestagswahl.benchmark;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardRandom {
	List<Integer> cards;
	int[] frequencies;

	public CardRandom() {
		cards = Collections.synchronizedList(new ArrayList<Integer>());
	}

	private void initCards() {
		int query = 0;
		cards.clear();
		for (final int frequency : frequencies) {
			query++;
			for (int i = 0; i < frequency; i++) {
				cards.add(query);
			}
		}
	}

	public void setFrequencies(int[] frequencies) {
		this.frequencies = frequencies;
		initCards();
	}

	public int getRandom() {
		int random = (int) (Math.random() * cards.size());
		int returnInt = cards.get(random);
		cards.remove(random);
		if (cards.size() == 0)
			initCards();
		return returnInt;
	}
}
