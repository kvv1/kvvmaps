package com.smartbean.androidutils.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Handler;

import com.smartbean.androidutils.util.Utils;

public abstract class RemoteData1<TData, TMsg> {

	protected abstract List<TMsg> loadMessages();

	protected abstract void progress(boolean on);

	protected abstract void storeMessages(List<TMsg> messages);

	protected abstract void _send(TMsg msg) throws IOException;

	protected abstract TData _getRemoteData() throws IOException;

	protected abstract void remoteDataReceived(TData remote);

	protected abstract void remoteDataFailure(Exception cause);

	protected abstract void _error(Exception e);

	private List<TMsg> messages = new LinkedList<TMsg>();
	private Handler handler = new Handler();
	protected int updateInterval;

	private final ExecutorService executorService = Executors
			.newSingleThreadExecutor();

	public void start() {
		messages = loadMessages();
		handler.removeCallbacks(runnable);
		handler.post(runnable);
		posted = true;
	}

	public void stop() {
		executorService.shutdown();
		handler.removeCallbacks(runnable);
	}

	public void add(TMsg msg) {
		messages.add(msg);
		storeMessages(messages);
		if (posted) {
			handler.removeCallbacks(runnable);
			handler.post(runnable);
		}
	}

	private boolean posted;

	private Runnable runnable = new Runnable() {
		@Override
		public void run() {
			log("handler entered");
			progress(true);
			posted = false;
			if (!messages.isEmpty()) {
				final TMsg msg = messages.get(0);
				if (msg != null)
					log("sending message");
				new Step<Void>() {
					@Override
					protected Void asyncFunc() throws Exception {
						if (msg != null)
							_send(msg);
						return null;
					}

					@Override
					protected void onComplete(Void data) {
						if (msg != null)
							log("message sent ok");
						messages.remove(msg);
						storeMessages(messages);
						handler.removeCallbacks(runnable);
						handler.post(runnable);
						posted = true;
					}

					@Override
					protected void handleException(Exception e) {
						if (msg != null)
							log("message not sent");
						progress(false);
						handler.removeCallbacks(runnable);
						log("* failure " + e.getClass().getName() + " "
								+ e.getMessage());
						remoteDataFailure(e);
						handler.postDelayed(runnable, 10000);
						posted = true;
					}
				}.exec();
			} else {
				log("reading data");
				new Step<TData>() {
					@Override
					protected TData asyncFunc() throws Exception {
						return _getRemoteData();
					}

					@Override
					protected void onComplete(TData data) {
						log("data read ok");
						handler.removeCallbacks(runnable);
						if (messages.isEmpty()) {
							if (data != null)
								remoteDataReceived(data);
							progress(false);
							handler.postDelayed(runnable, updateInterval * 1000);
						} else {
							handler.post(runnable);
						}
						posted = true;
					}

					@Override
					protected void handleException(Exception e) {
						log("data not read");
						progress(false);
						handler.removeCallbacks(runnable);
						log("* failure " + e.getClass().getName() + " "
								+ e.getMessage());
						remoteDataFailure(e);
						handler.postDelayed(runnable, 10000);
						posted = true;
					}
				}.exec();
			}
		}
	};

	abstract class Step<T> {
		protected abstract T asyncFunc() throws Exception;

		protected abstract void onComplete(T data);

		protected abstract void handleException(Exception e);

		public void exec() {
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						final T data = asyncFunc();
						handler.post(new Runnable() {
							@Override
							public void run() {
								onComplete(data);
							}
						});
					} catch (final Exception e) {
						_error(e);
						handler.post(new Runnable() {
							@Override
							public void run() {
								handleException(e);
							}
						});
					}
				}
			});
		}
	}

	private void log(String txt) {
		Utils.log(this, txt);
	}

}
