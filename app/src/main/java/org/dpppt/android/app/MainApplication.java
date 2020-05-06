/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.dpppt.android.sdk.DP3T;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.internal.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.util.ProcessUtil;


public class MainApplication extends Application {

	private static final String NOTIFICATION_CHANNEL_ID = "contact-channel";

	@Override
	public void onCreate() {
		super.onCreate();
		if (ProcessUtil.isMainProcess(this)) {
			registerReceiver(broadcastReceiver, DP3T.getUpdateIntentFilter());
			DP3T.init(this, new ApplicationInfo("it.noiapp.demo", "https://protetti.app/"));
		}
	}

	@Override
	public void onTerminate() {
		if (ProcessUtil.isMainProcess(this)) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onTerminate();
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			SharedPreferences prefs = context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE);
			if (!prefs.getBoolean("notification_shown", false)) {
				TracingStatus status = DP3T.getStatus(context);
				if (status.wasContactExposed()) {

					if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
						createNotificationChannel();
					}

					Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
					PendingIntent contentIntent = null;
					if (launchIntent != null) {
						contentIntent =
								PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
					}
					Notification notification =
							new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
									.setContentTitle(context.getString(R.string.push_exposed_title))
									.setContentText(context.getString(R.string.push_exposed_text))
									.setPriority(NotificationCompat.PRIORITY_MAX)
									.setSmallIcon(R.drawable.ic_begegnungen)
									.setContentIntent(contentIntent)
									.build();

					NotificationManager notificationManager =
							(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(42, notification);

					prefs.edit().putBoolean("notification_shown", true).commit();
				}
			}
		}
	};

	@RequiresApi(api = Build.VERSION_CODES.O)
	private void createNotificationChannel() {
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		String channelName = getString(R.string.app_name);
		NotificationChannel channel =
				new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
		channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
		notificationManager.createNotificationChannel(channel);
	}

}
