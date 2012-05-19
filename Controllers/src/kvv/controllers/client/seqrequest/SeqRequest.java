package kvv.controllers.client.seqrequest;

import java.util.LinkedList;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.rpc.AsyncCallback;

@SuppressWarnings("deprecation")
interface Req {
	void exec();
}

public abstract class SeqRequest<T> implements Req, AsyncCallback<T> {

	private static LinkedList<Req> requests = new LinkedList<Req>();

	public static void clear() {
		requests.clear();
	}

	public abstract void _onFailure(Throwable caught);

	public abstract void _onSuccess(T result);

	@SuppressWarnings("deprecation")
	public SeqRequest() {
		requests.add(this);
		if (requests.size() == 1) {
			DeferredCommand.addCommand(new Command() {
				public void execute() {
					exec();
				}
			});
		}
	}

	@Override
	public final void onFailure(Throwable caught) {
		_onFailure(caught);
		if (!requests.isEmpty()) {
			Req req = requests.removeFirst();
			req.exec();
		}
	}

	@Override
	public final void onSuccess(T result) {
		_onSuccess(result);
		if (!requests.isEmpty()) {
			Req req = requests.removeFirst();
			req.exec();
		}
	}

}
