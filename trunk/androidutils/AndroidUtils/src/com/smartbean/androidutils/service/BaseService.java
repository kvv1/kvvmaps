package com.smartbean.androidutils.service;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;

public abstract class BaseService extends Service {

	protected final int iconId;
	protected final int iconGrayId;
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
	private final boolean notifSound;

	private final boolean sticky;

	public BaseService(int iconId, int iconGrayId,
			Class<? extends Activity> activityClass, int appNameId,
			int notifTextId, boolean notifSound, boolean sticky) {
		this.iconId = iconId;
		this.iconGrayId = iconGrayId;
		this.activityClass = activityClass;
		this.appNameId = appNameId;
		this.notifTextId = notifTextId;
		this.notifSound = notifSound;
		this.sticky = sticky;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (sticky)
			return START_STICKY;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		defaultNotif = createNotif(iconId);
		grayNotif = createNotif(iconGrayId);
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

		if (notifSound)
			notif.defaults |= Notification.DEFAULT_SOUND;

		return notif;
	}

	public void setOnline(boolean online) {
//		if (this.online != online && !alert) {
//			nm.notify(3333, online ? defaultNotif : grayNotif);
//		}
		this.online = online;
		updateNotif();
	}

	public void setAlert(boolean alert) {
//		if (alert && !this.alert && alertEnabled)
//			nm.notify(3333, alertNotif);
//		else if (!alert && this.alert)
//			nm.notify(3333, online ? defaultNotif : grayNotif);
		this.alert = alert;
		updateNotif();
	}

	public void enableAlert(boolean b) {
		if (!b)
			alert = false;
			//setAlert(false);
		this.alertEnabled = b;
		updateNotif();
	}

	private void updateNotif() {
		if (alert && alertEnabled)
			nm.notify(3333, alertNotif);
		else
			nm.notify(3333, online ? defaultNotif : grayNotif);
	}
}
