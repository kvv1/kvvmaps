package kvv.gwtutils.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class Callback<T> implements AsyncCallback<T>{

	@Override
	public void onFailure(Throwable caught) {
	}

	@Override
	public void onSuccess(T result) {
	}

}
