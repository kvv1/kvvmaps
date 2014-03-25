package com.smartbean.androidutils.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

public abstract class BaseService extends Service {

	protected final int iconId;
	protected final int iconIdBW;
	private final Class<? extends Activity> activityClass;
	private final int appNameId;
	private final int notifTextId;

	private NotificationManager nm;

	private Notification defaultNotif;
	private Notification grayNotif;
	private Notification alertNotif;
	// private Notification currentNotif;

	private boolean online;
	private boolean alert;
	private boolean alertEnabled;

	public BaseService(int iconId, int iconGrayId,
			Class<? extends Activity> activityClass, int appNameId,
			int notifTextId) {
		this.iconId = iconId;
		this.iconIdBW = iconGrayId;
		this.activityClass = activityClass;
		this.appNameId = appNameId;
		this.notifTextId = notifTextId;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		defaultNotif = createNotif(iconId);
		grayNotif = createNotif(iconIdBW);
		alertNotif = createAlertNotif();
		// currentNotif = grayNotif;

		startForeground(3333, grayNotif);
	}

	private Notification createNotif(int icon) {
		Notification notif = new Notification(icon, "",
				System.currentTimeMillis());
		notif.flags |= Notification.FLAG_NO_CLEAR;
		Intent intent = new Intent(this, activityClass);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
		notif.setLatestEventInfo(this, getString(appNameId), "", pi);
		return notif;
	}

	private Notification createAlertNotif() {
		// 1-я часть
		Notification notif = new Notification(iconId, getString(notifTextId),
				System.currentTimeMillis());
		// ставим флаг, чтобы уведомление пропало после нажатия
		notif.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent intent = new Intent(this, activityClass);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		notif.setLatestEventInfo(this, getString(appNameId),
				getString(notifTextId), pIntent);

		notif.number = 1;

		return notif;
	}

	public void setOnline(boolean online) {
		if (this.online != online && !alert) {
			nm.notify(3333, online ? defaultNotif : grayNotif);
		}
		this.online = online;
	}

	public void setAlert(boolean alert) {
		if (alert && !this.alert && alertEnabled)
			nm.notify(3333, alertNotif);
		else if (!alert && this.alert)
			nm.notify(3333, online ? defaultNotif : grayNotif);
		this.alert = alert;
	}

	public void enableAlert(boolean b) {
		if (!b)
			setAlert(false);
		this.alertEnabled = b;
	}
}
