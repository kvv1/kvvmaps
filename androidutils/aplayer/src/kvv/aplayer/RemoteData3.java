package kvv.aplayer;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import android.os.AsyncTask;
import android.os.Handler;

public class RemoteData3<DATA, MSG, CONTEXT> {
	
	private static final long DELAY = 10000;

	private LinkedBlockingQueue<MSG> messages = new LinkedBlockingQueue<MSG>();

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
		messages.notifyAll();
		process(true);
	}

	private final ExecutorService e = Executors.newSingleThreadExecutor();
	
	private volatile boolean stopped;
	
	public void start() {
		e.submit(new Runnable() {
			@Override
			public void run() {
				
				// TODO Auto-generated method stub
				
			}
		});
	}

	public void stop() {
		stopped = true;
	}
	
	private void process(boolean immed) {
	}
	

}
