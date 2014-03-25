package com.smartbean.androidutils.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.smartbean.androidutils.service.ServiceConnectionAdapter;
import com.smartbean.androidutils.util.Drawables;
import com.smartbean.androidutils.util.Utils;

public abstract class RLFragment<A extends Activity, IService> extends Fragment {

	protected ServiceConnectionAdapter<IService> conn = new ServiceConnectionAdapter<IService>() {
		public void onServiceConnected(android.content.ComponentName name,
				android.os.IBinder binder) {
			super.onServiceConnected(name, binder);
			createUiIfPossible();
		}
	};

	protected long id;
	protected View rootView;

	// protected A activity;

	private final Class<?> serviceClass;

	protected abstract void createUI(IService service);

	protected abstract int getLayout();

	private void createUiIfPossible() {
		if (conn.service != null && rootView != null)
			createUI(conn.service);
	}

	public RLFragment(Class<?> serviceClass) {
		this.serviceClass = serviceClass;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(getLayout(), container, false);
		createUiIfPossible();
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.log(this, getClass().getSimpleName() + ".onCreate");
		super.onCreate(savedInstanceState);
		getActivity().bindService(new Intent(getActivity(), serviceClass),
				conn, Context.BIND_AUTO_CREATE);
		id = getArguments().getLong("id");
	}

	@Override
	public void onDestroy() {
		Utils.log(this, getClass().getSimpleName() + ".onDestroy");
		getActivity().unbindService(conn);

		Drawables.unbindDrawables(rootView);

		rootView = null;
		super.onDestroy();
	}

}
