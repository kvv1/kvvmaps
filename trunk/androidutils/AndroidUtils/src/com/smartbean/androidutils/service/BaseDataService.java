package com.smartbean.androidutils.service;

import android.app.Activity;

import com.smartbean.androidutils.service.RemoteData.RemoteDataListener;
import com.smartbean.androidutils.util.Utils;

public abstract class BaseDataService<TData, TMsg> extends BaseService
		implements RemoteDataListener<TData, TMsg> {
	public RemoteData<TData, TMsg> remoteData;

	protected abstract void dataChanged();

	protected abstract TData merge(TData stored, TData remote);

	public BaseDataService(int iconId, int iconGrayId,
			Class<? extends Activity> activityClass, int appNameId,
			int notifTextId, boolean notifSound, boolean sticky) {
		super(iconId, iconGrayId, activityClass, appNameId, notifTextId,
				notifSound, sticky);
	}

	// public class CarabiDataListener implements RemoteDataListener<TData,
	// TMsg> {
	@Override
	public void remoteDataReceived(TData remote) {
		TData stored = getStoredData();
		TData merged = merge(stored, remote);
		setOnline(true);
		if (merged != null) {
			storeData(merged);
			dataChanged();
			setAlert(true);
		}
	}

	@Override
	public void remoteDataFailure(Exception cause) {
		// cause.printStackTrace();
		setOnline(false);
	}

	// }

	@Override
	public void onCreate() {
		Utils.log(this, getClass().getSimpleName() + ".onCreate");
		super.onCreate();

		remoteData = createRemoteData();

		updateSettings();
		remoteData.start();
	}

	protected abstract RemoteData<TData, TMsg> createRemoteData();

	@Override
	public void onDestroy() {
		Utils.log(this, getClass().getSimpleName() + ".onDestroy");
		remoteData.stop();
		// stopForeground(true);
		// super.onDestroy();
	}

	protected abstract TData getStoredData();

	protected abstract void storeData(TData d);

	protected abstract void updateSettings();

}
