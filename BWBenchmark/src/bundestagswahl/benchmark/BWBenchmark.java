/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */
package bundestagswahl.benchmark;

import java.util.Scanner;
import java.util.concurrent.CountDownLatch;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;

public class BWBenchmark {
	static String serverUrl;
	static int numberTerminals;
	static int numberRequests;
	static double requestDelay;

	
	static double[] resultTime;

	
	public static void main(String[] args) throws Exception {

		resultTime = new double[6];

		Scanner scanner = new Scanner(System.in);
		System.out.print("Please enter URL: ");
		serverUrl = scanner.nextLine();
		System.out.print("Please enter number of terminals: ");
		numberTerminals = scanner.nextInt();
		System.out.print("Please enter number of requests: ");
		numberRequests = scanner.nextInt();
		System.out
				.print("Please enter delay between two requests in seconds: ");
		requestDelay = scanner.nextDouble();
		scanner.close();

		PoolingClientConnectionManager cm = new PoolingClientConnectionManager();
		cm.setMaxTotal(numberTerminals);
		HttpClient httpclient = new DefaultHttpClient(cm);
		httpclient
				.getParams()
				.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 300000)
				.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
						300000)
				.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
						8 * 1024)
				.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
		try {
			final CountDownLatch latch = new CountDownLatch(numberTerminals);
			for (int i = 0; i < numberTerminals; i++) {
				BenchmarkTerminal terminal = new BenchmarkTerminal(httpclient,
						latch, serverUrl, numberRequests, requestDelay);
				terminal.start();
			}
			latch.await();
			httpclient.getConnectionManager().shutdown();
		} finally {

		}

		printResultTimes();
		System.out.println(" ");
		System.out.println("Done");
	}

	public static synchronized void addResultTime(int query, double time) {
		resultTime[query] += time;
	}

	public static synchronized void printResultTimes() {
		for (int i = 0; i < 6; i++) {
			System.out.println("Query " + Integer.toString(i + 1) + " : "
					+ resultTime[i] / numberRequests /1000+ " s" );
		}
	}

}