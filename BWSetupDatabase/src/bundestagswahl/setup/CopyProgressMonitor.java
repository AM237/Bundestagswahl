package bundestagswahl.setup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;

import javax.swing.ProgressMonitorInputStream;

public class CopyProgressMonitor {

	public static ProgressMonitorInputStream getCopyProgressMonitor(
			String path, String progressString) throws FileNotFoundException {
		return new ProgressMonitorInputStream(null, progressString,
				new FileInputStream(path) {

					private long gelesenByte = 0;
					private long diffGelesen = 0;
					private long zuLesen = 0;
					DecimalFormat fromat = new DecimalFormat("#0.00");

					public int read() throws IOException {
						update(1);
						return super.read();
					}

					public int read(byte[] b) throws IOException {
						update(1);
						return super.read(b);
					}

					public int read(byte[] b, int off, int len)
							throws IOException {
						update(len);
						return super.read(b, off, len);
					}

					public void update(int len) throws IOException {

						if (gelesenByte == 0)
							zuLesen = super.available();

						gelesenByte = gelesenByte + len;
						diffGelesen = diffGelesen + len;
						if (diffGelesen > 1024 * 1024) {
							diffGelesen = 0;
							System.out.println(gelesenByte
									/ 1024
									/ 1024
									+ "MB von "
									+ zuLesen
									/ 1024
									/ 1024
									+ " MB - "
									+ fromat.format((double) gelesenByte
											/ zuLesen * 100.0) + "%");
						}
					}

				});

	}

}
