package kvv.controllers.server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

	private static final SimpleDateFormat df = new SimpleDateFormat(
			"yyyy_MM_dd");
	private static final SimpleDateFormat tf = new SimpleDateFormat("HH:mm:ss");

	private static OutputStream os = new OutputStream() {
		private final int[] buf = new int[4096];
		private int idx = 0;

		@Override
		public synchronized void write(int b) throws IOException {
			System.out.write(b);

			if (idx == buf.length)
				fl();

			buf[idx++] = b;

			if (b == '\n')
				fl();
		}

		private void fl() throws IOException {
			Date date = new Date();
			FileWriter wr = new FileWriter(Constants.logDir + df.format(date)
					+ ".log", true);
			wr.write(tf.format(date));
			wr.write("  ");
			for (int i = 0; i < idx; i++)
				wr.write(buf[i]);
			idx = 0;
			wr.close();
		}
	};

	public static PrintStream out = new PrintStream(os, true);
}
