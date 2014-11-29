package com.smartbean.androidutils.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

public class FTPUtils {

	public static void append(String server, String username, String password,
			String remotePath, String text) throws IOException {
		append(server, username, password, remotePath,
				new ByteArrayInputStream(text.getBytes()));
	}

	public static void append(String server, String username, String password,
			String remotePath, InputStream is) throws IOException {
		FTPClient mFtp = new FTPClient();
		try {
			mFtp.connect(server, FTP.DEFAULT_PORT);
			mFtp.login(username, password);
			mFtp.setFileType(FTP.BINARY_FILE_TYPE);
			mFtp.enterLocalPassiveMode();
			mFtp.appendFile(remotePath, is);
		} finally {
			try {
				if (mFtp.isConnected()) {
					mFtp.logout();
					mFtp.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void upload(String server, String username, String password,
			String remotePath, File file) throws IOException {
		FTPClient mFtp = new FTPClient();
		try {
			mFtp.connect(server, FTP.DEFAULT_PORT);
			mFtp.login(username, password);
			mFtp.setFileType(FTP.BINARY_FILE_TYPE);
			mFtp.enterLocalPassiveMode();
			InputStream is = new FileInputStream(file);
			mFtp.storeFile(remotePath, is);
			is.close();
		} finally {
			try {
				if (mFtp.isConnected()) {
					mFtp.logout();
					mFtp.disconnect();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

}
