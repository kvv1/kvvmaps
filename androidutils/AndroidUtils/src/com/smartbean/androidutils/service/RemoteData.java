package com.smartbean.androidutils.service;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.os.Handler;

import com.smartbean.androidutils.util.Utils;

public class RemoteData<TData, TMsg> {
	public interface RemoteDataProvider<TData, TMsg> {
		Object _login(Object login) throws IOException;

		TData _getRemoteData(Object login) throws IOException,
				InterruptedException;

		void updateSettings(Context context);

		void _send(Object login, TMsg msg) throws IOException,
				InterruptedException;

		boolean checkLogin(Object login);
	}

	public interface RemoteDataListener<TData, TMsg> {
		void remoteDataReceived(TData remote);

		void remoteDataFailure(Exception cause);
	}

	public interface RemoteDataMessages<TMsg> {
		List<TMsg> loadMessages();

		void storeMessages(List<TMsg> messages);
	}

	private List<TMsg> messages;
	private volatile int updateInterval;

	private Handler handler = new Handler();

	private volatile boolean stopped;

	private final RemoteDataProvider<TData, TMsg> dataProvider;
	private final RemoteDataListener<TData, TMsg> dataListener;
	private final RemoteDataMessages<TMsg> dataMessages;

	public RemoteData(RemoteDataProvider<TData, TMsg> dataProvider,
			RemoteDataListener<TData, TMsg> dataListener,
			RemoteDataMessages<TMsg> dataMessages) {
		this.dataMessages = dataMessages;
		this.dataProvider = dataProvider;
		this.dataListener = dataListener;
		this.messages = dataMessages.loadMessages();
	}

	public void start() {
		thread.start();
	}

	public void stop() {
		stopped = true;
		thread.interrupt();
	}

	public void check() {
		thread.interrupt();
	}

	public void add(TMsg msg) {
		synchronized (messages) {
			messages.add(msg);
			dataMessages.storeMessages(messages);
			messages.notify();
		}
	}

	private void _remove(TMsg msg) {
		synchronized (messages) {
			messages.remove(msg);
		}
		handler.post(new Runnable() {
			@Override
			public void run() {
				synchronized (messages) {
					dataMessages.storeMessages(messages);
				}
			}
		});
	}

	private synchronized TMsg _get() {
		synchronized (messages) {
			if (messages.isEmpty())
				return null;
			return messages.get(0);
		}
	}

	public boolean hasMessages() {
		synchronized (messages) {
			return !messages.isEmpty();
		}
	}

	private Thread thread = new Thread() {
		{
			setDaemon(true);
			setPriority(Thread.MIN_PRIORITY);
		}

		private void step(final Object login) throws IOException,
				InterruptedException {
			TMsg msg;
			while ((msg = _get()) != null) {
				dataProvider._send(login, msg);
				_remove(msg);
			}

			final TData d = dataProvider._getRemoteData(login);
			if (_get() == null) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (_get() == null && dataProvider.checkLogin(login))
							dataListener.remoteDataReceived(d);
					}
				});
			}
			synchronized (messages) {
				if (_get() == null)
					messages.wait(updateInterval * 1000);
			}
		}

		public void run() {
			Object login = null;
			while (!stopped) {
				try {
					login = dataProvider._login(login);
					while (!stopped) {
						step(login);
					}
				} catch (final IOException e) {
					log("* failure " + e.getClass().getName() + " " + e.getMessage());
//					e.printStackTrace();
					handler.post(new Runnable() {
						@Override
						public void run() {
							dataListener.remoteDataFailure(e);
						}
					});
					login = null;
					try {
						sleep(10000);
					} catch (Exception e1) {
					}
				} catch (InterruptedException e) {
					log("* interrupted");
					try {
						sleep(1000);
					} catch (Exception e1) {
					}
				} catch (final Exception e) {
					e.printStackTrace();
					handler.post(new Runnable() {
						@Override
						public void run() {
							dataListener.remoteDataFailure(e);
						}
					});
					login = null;
					try {
						sleep(10000);
					} catch (Exception e1) {
					}
				}
			}
		}

	};

	public void setSettings(int updateIntervalSec) {
		log("setSettings");
		this.updateInterval = updateIntervalSec;
		check();
	}

	private void log(String txt) {
		Utils.log(this, txt);
	}

}
