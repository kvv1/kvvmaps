package com.smartbean.androidutils.fragment;

import java.util.ArrayList;
import java.util.List;

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

public abstract class FragmentX<A extends Activity, IService> extends Fragment {

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

	private List<Runnable> runnables = new ArrayList<Runnable>();

	protected abstract void createUI(IService service);

	private final int layout;

	private void createUiIfPossible() {
		if (conn.service != null && rootView != null) {
			createUI(conn.service);
			while (!runnables.isEmpty())
				runnables.remove(0).run();
		}
	}

	public FragmentX(Class<?> serviceClass, int layout) {
		this.serviceClass = serviceClass;
		this.layout = layout;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(layout, container, false);
		createUiIfPossible();
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.log(this, "onCreate");
		super.onCreate(savedInstanceState);
		getActivity().bindService(new Intent(getActivity(), serviceClass),
				conn, Context.BIND_AUTO_CREATE);
		Bundle args = getArguments();
		if (args != null)
			id = getArguments().getLong("id");
	}

	@Override
	public void onDestroy() {
		Utils.log(this, "onDestroy");
		getActivity().unbindService(conn);

		Drawables.unbindDrawables(rootView);

		rootView = null;
		super.onDestroy();
	}

	// YjdsqGfhjkm2014

	@SuppressWarnings("unchecked")
	public A getActivity1() {
		return (A) getActivity();
	}

	public void postForService(Runnable r) {
		if (conn.service != null)
			r.run();
		else
			runnables.add(r);
	}
}
