package kvv.aplayer;

import java.util.LinkedList;

import android.os.AsyncTask;
import android.os.Handler;

public class RemoteData2<DATA, MSG, CONTEXT> {

	// static class MSG{}

	private static final long DELAY = 10000;

	private LinkedList<MSG> messages = new LinkedList<MSG>();

	public void dataReceived(DATA data) {
	}

	public CONTEXT getContext() {
		return null;
	}

	protected void _send(CONTEXT context, MSG msg) {
	}

	protected DATA _read(CONTEXT context) {
		return null;
	}

	protected boolean isValid(CONTEXT context) {
		return false;
	}

	protected void onError(CONTEXT context) {
	}

	protected CONTEXT _login(CONTEXT context) {
		return null;
	}

	protected void setContext(CONTEXT result) {
	}

	private AsyncTask<?, ?, ?> asyncTask = null;

	private Handler handler = new Handler();

	public void send(MSG msg) {
		messages.add(msg);
		process(true);
	}

	private void process(boolean immed) {
		handler.removeCallbacksAndMessages(null);

		if (asyncTask != null)
			return;

		final CONTEXT context = getContext();

		if (!isValid(context)) {
			AsyncTask<Void, Void, CONTEXT> task = new AsyncTask<Void, Void, CONTEXT>() {
				@Override
				protected CONTEXT doInBackground(Void... params) {
					try {
						return _login(context);
					} catch (Exception e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(CONTEXT result) {
					asyncTask = null;
					setContext(result);
					process(true);
				}

				@Override
				protected void onCancelled() {
					asyncTask = null;
					onError(context);
					process(true);
				}
			};
			asyncTask = task;
			task.execute();
		} else if (!messages.isEmpty()) {
			final MSG msg = messages.getFirst();
			AsyncTask<MSG, Void, Boolean> task = new AsyncTask<MSG, Void, Boolean>() {
				@Override
				protected Boolean doInBackground(MSG... params) {
					try {
						_send(context, params[0]);
						return true;
					} catch (Exception e) {
						return false;
					}
				}

				@Override
				protected void onPostExecute(Boolean result) {
					asyncTask = null;
					if (result)
						messages.remove(msg);
					else
						onError(context);
					process(true);
				}

				@Override
				protected void onCancelled() {
					asyncTask = null;
					onError(context);
					process(true);
				}
			};
			asyncTask = task;
			task.execute(msg);
		} else if (immed) {
			AsyncTask<Void, Void, DATA> task = new AsyncTask<Void, Void, DATA>() {
				@Override
				protected DATA doInBackground(Void... params) {
					try {
						return _read(context);
					} catch (Exception e) {
						return null;
					}
				}

				@Override
				protected void onPostExecute(DATA result) {
					asyncTask = null;
					if (result == null) {
						onError(context);
						process(true);
					} else if (!messages.isEmpty()) {
						process(true);
					} else {
						if(isValid(context));
						dataReceived(result);
						process(false);
					}
				}

				@Override
				protected void onCancelled() {
					asyncTask = null;
					onError(context);
					process(true);
				}
			};
			asyncTask = task;
			task.execute();
		} else {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					process(true);
				}
			}, DELAY);
		}

	}

}
