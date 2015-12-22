package kvv.controllers.server.history;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogFile {

	private static final DateFormat fileDF = new SimpleDateFormat("yyyy_MM_dd");
	private static final DateFormat timeDF = new SimpleDateFormat("HH:mm:ss");

	private final String dir;

	private String fileName;
	private PrintStream ps;
	private boolean stopped;

	public LogFile(String dir) {
		this.dir = dir;
		new File(dir).mkdirs();
	}

	public synchronized void println(String s) throws IOException {
		if (stopped)
			return;

		Date d = new Date();

		String fileName = fileDF.format(d);

		if (!fileName.equals(this.fileName)) {
			this.fileName = null;
			if (ps != null)
				ps.close();
			ps = new PrintStream(new FileOutputStream(dir + "/" + fileName,
					true), true, "Windows-1251");
			this.fileName = fileName;
		}

		ps.println(timeDF.format(d) + (s == null ? "" : " " + s));
	}

	public synchronized void stop() {
		stopped = true;
		if (ps != null)
			ps.close();
	}

}
