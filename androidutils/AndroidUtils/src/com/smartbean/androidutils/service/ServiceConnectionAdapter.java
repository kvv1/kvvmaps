package com.smartbean.androidutils.service;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.smartbean.androidutils.service.BaseService.BaseServiceBinder;
import com.smartbean.androidutils.util.Utils;

public class ServiceConnectionAdapter<ServiceInterface> implements ServiceConnection {
	public ServiceInterface service;
	@SuppressWarnings("unchecked")
	@Override
	public void onServiceConnected(ComponentName name, IBinder binder) {
		service = (ServiceInterface) ((BaseServiceBinder)binder).getService();
		Utils.log(this, "onServiceConnected");
	}

	@Override
	public final void onServiceDisconnected(ComponentName name) {
		service = null;
		Utils.log(this, "onServiceDisconnected");
	}
}