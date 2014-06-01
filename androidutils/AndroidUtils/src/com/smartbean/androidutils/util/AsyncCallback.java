package com.smartbean.androidutils.util;

public abstract class AsyncCallback<T> {
	public abstract void onSuccess(T res);

	public void onFailure() {
	}
}

