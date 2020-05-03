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
import org.dpppt.android.sdk.InfectionStatus;
import org.dpppt.android.sdk.TracingStatus;
import org.dpppt.android.sdk.backend.models.ApplicationInfo;
import org.dpppt.android.sdk.internal.util.Base64Util;
import org.dpppt.android.sdk.internal.util.ProcessUtil;
import org.dpppt.android.sdk.util.SignatureUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.cert.CertificateFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.Scanner;


public class MainApplication extends Application {

	private static final String NOTIFICATION_CHANNEL_ID = "contact-channel";
	private static final String PUBLIC_KEY_ASSET = "public_key.pem";

	public PublicKey getPublicKey() {
		try {
			String text = null;
			InputStream publicKeyResource = getAssets().open(PUBLIC_KEY_ASSET);
			try (Scanner scanner = new Scanner(publicKeyResource, StandardCharsets.UTF_8.name())) {
				text = scanner.useDelimiter("\\A").next();
			}
			// read Pem object
			text = new String(Base64Util.fromBase64(text))
					.replaceAll("-+(BEGIN|END) PUBLIC KEY-+", "")
					.replaceAll("\\s+", "")
					.trim();
			byte[] pubkeyRaw =  Base64Util.fromBase64(text);
			return KeyFactory.getInstance("EC").generatePublic(new X509EncodedKeySpec(
					pubkeyRaw
			));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (ProcessUtil.isMainProcess(this)) {
			registerReceiver(broadcastReceiver, DP3T.getUpdateIntentFilter());
			DP3T.init(this,
					"org.dpppt.demo",
					BuildConfig.FLAVOR.equals("dev"),
					getPublicKey());
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
				if (InfectionStatus.EXPOSED.equals(status.getInfectionStatus())) {

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
