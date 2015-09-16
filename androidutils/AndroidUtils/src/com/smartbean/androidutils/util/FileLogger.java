package com.smartbean.androidutils.util;

import android.annotation.SuppressLint;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressLint("SimpleDateFormat")
public class FileLogger {

	private final File file;
	private final int maxSize;
	private final boolean addDateTime;
	private static ExecutorService msgQueue = Executors.newSingleThreadExecutor();
	private static SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");

	public FileLogger(File file, int maxSize, boolean addDateTime) {
		this.file = file;
		this.maxSize = maxSize;
		this.addDateTime = addDateTime;
		file.getParentFile().mkdirs();
	}

	public synchronized void add(final String txt) {
		msgQueue.submit(new Runnable() {
			@Override
			public void run() {
				try {
					_add(txt);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void _add(String txt) throws IOException {
		FileWriter wr = new FileWriter(file, true);
		if (addDateTime)
			wr.write(sdf.format(new Date()) + " ");

		wr.write(txt);
		wr.write("\r\n");
		wr.close();

		long l = file.length();
		if (l > maxSize) {
			File temp = File.createTempFile("tmp", "", file.getParentFile());
			BufferedInputStream is = new BufferedInputStream(
					new FileInputStream(file));
			BufferedOutputStream os = new BufferedOutputStream(
					new FileOutputStream(temp));

			is.skip(l / 3);

			byte[] buf = new byte[4096];
			for (;;) {
				int n = is.read(buf);
				if (n == -1)
					break;
				os.write(buf, 0, n);
			}

			os.close();
			is.close();

			temp.renameTo(file);
		}
	}
}
