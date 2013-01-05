package bundestagswahl.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.nio.reactor.IOReactorException;

public class BenchmarkTerminal extends Thread {

	private int numberRequests;
	private double requestDelay;
	private CountDownLatch latch;
	private HttpClient httpclient;
	private String serverUrl;
	private CardRandom cardRandom;

	public BenchmarkTerminal(HttpClient httpclient, CountDownLatch latch,
			String serverUrl, int numberRequests, double requestDelay)
			throws IOReactorException {
		this.numberRequests = numberRequests;
		this.serverUrl = serverUrl;

		this.requestDelay = requestDelay;
		this.latch = latch;
		this.httpclient = httpclient;
		cardRandom = new CardRandom();
		cardRandom.setFrequencies(new int[] { 5, 2, 5, 2, 2, 4 });
	}

	public void run() {
		for (int i = 0; i < numberRequests; i++) {
			int query = cardRandom.getRandom();
			String url = this.serverUrl + Integer.toString(query);
			HttpGet httpget = new HttpGet(url);
			System.out.println(url);
			try {
				long time = System.currentTimeMillis();
				HttpResponse response = httpclient.execute(httpget);
				long delay = System.currentTimeMillis() - time;
				BWBenchmark.addResultTime(query - 1, delay);
				System.out.println(response.getStatusLine());
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					InputStream instream = entity.getContent();
					instream.close();
				}
				sleep((long) (1000.0f * requestDelay * (Math.random() * 0.4 + 0.8)));
			} catch (IOException e) {
				httpget.abort();
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		latch.countDown();
	}
}
