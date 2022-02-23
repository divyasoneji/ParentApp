package com.cmpt276.parentapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.core.app.NotificationCompat;

import java.util.concurrent.TimeUnit;

/**
 * Service to keep the timer running while using other apps
 */
public class TimerService extends Service {

	private static final String ORIGINAL_TIME_IN_MILLI_SECONDS_TAG = "original_time_in_milli_seconds_tag";
	private static final String SPEED_PERCENTAGE_TAG = "speed_percentage_tag";
	private static final String TIMER_SERVICE_BROADCAST = "timer_service_broadcast";
	private static final String CHANNEL_LOW_NAME = "channel_low";
	private static final String CHANNEL_HIGH_NAME = "channel_high";
	private static final String CHANNEL_LOW_PRIORITY_ID = "low_priority_channel";
	private static final String CHANNEL_HIGH_PRIORITY_ID = "high_priority_channel";
	private static final String STOP_ALARM_BROADCAST = "stop_alarm_broadcast";

	private static final int NOTIFICATION_ID = 1;
	private static final int REPEAT_ONCE = 1;
	private static final long DEFAULT_MINUTES_IN_MILLI_SECONDS = 0L;
	private static final int DEFAULT_SPEED_PERCENTAGE = 100;
	private static final double HUNDRED_PERCENT = 100.0;
	private static final double SECONDS_CONVERSION = 1 / 1000.0;


	private CountDownTimer timer;
	private long remainingMilliSeconds;
	private long originalTimeInMilliSeconds;
	private int speedPercentage;
	private boolean isPaused;
	private boolean isFinish;
	private Vibrator vibrator;
	private MediaPlayer mp;

	/**
	 * https://developer.android.com/guide/components/bound-services#Binder
	 */
	private final IBinder binder = new LocalBinder();

	public static Intent getIntent(Context context, long originalTimeInMilliSeconds, int speed) {
		Intent i = new Intent(context, TimerService.class);
		i.putExtra(ORIGINAL_TIME_IN_MILLI_SECONDS_TAG, originalTimeInMilliSeconds);
		i.putExtra(SPEED_PERCENTAGE_TAG, speed);
		return i;
	}

	public static Intent getIntent(Context context) {
		return TimerService.getIntent(context, DEFAULT_MINUTES_IN_MILLI_SECONDS, DEFAULT_SPEED_PERCENTAGE);
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		setUpNotificationChannel(CHANNEL_LOW_NAME, CHANNEL_LOW_PRIORITY_ID, NotificationManager.IMPORTANCE_DEFAULT);
		setUpNotificationChannel(CHANNEL_HIGH_NAME, CHANNEL_HIGH_PRIORITY_ID, NotificationManager.IMPORTANCE_HIGH);

		originalTimeInMilliSeconds = intent.getLongExtra(ORIGINAL_TIME_IN_MILLI_SECONDS_TAG, DEFAULT_MINUTES_IN_MILLI_SECONDS);
		remainingMilliSeconds = originalTimeInMilliSeconds;
		speedPercentage = intent.getIntExtra(SPEED_PERCENTAGE_TAG, DEFAULT_SPEED_PERCENTAGE);


		setUpTimerBroadcast(originalTimeInMilliSeconds);

		return START_STICKY;
	}

	private long getMilliSecondsAccordingToSpeed(long milliseconds){
		return (long)(milliseconds / (speedPercentage / HUNDRED_PERCENT));
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		timer.cancel();
	}

	private void setUpTimerBroadcast(long milliSeconds) {
		isPaused = false;
		isFinish = false;
		long millisecondsAccordingToSpeed = getMilliSecondsAccordingToSpeed(milliSeconds);
		double intervalFraction = speedPercentage / HUNDRED_PERCENT;
		timer = new CountDownTimer(millisecondsAccordingToSpeed, (long)(1000 / intervalFraction)) {

			@Override
			public void onTick(long l ) {
				remainingMilliSeconds = (long)(l * intervalFraction);

				setUpNotification(getTimeString(), CHANNEL_LOW_PRIORITY_ID);

				Intent broadcastIntent = new Intent();
				broadcastIntent.setAction(TIMER_SERVICE_BROADCAST);
				sendBroadcast(broadcastIntent);
			}

			@Override
			public void onFinish() {
				isFinish = true;
				isPaused = true;
				setUpAlarmNotification();
			}
		};
		timer.start();
	}

	public String getTimeString() {
		int remainingMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(remainingMilliSeconds);
		int remainingSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(remainingMilliSeconds) -
				(int) TimeUnit.MINUTES.toSeconds(remainingMinutes);

		return String.format(getString(R.string.time_label_format), remainingMinutes, remainingSeconds);
	}

	public double getProgress(){
		return remainingMilliSeconds * SECONDS_CONVERSION;
	}

	/**
	 * https://developer.android.com/training/notify-user/build-notification
	 */
	private void setUpNotification(String timeString, String channelId) {

		Intent notificationIntent = TimerActivity.getIntent(this,
				originalTimeInMilliSeconds,
				true,
				speedPercentage);

		PendingIntent pendingIntent = PendingIntent.getActivity(this,
				0,
				notificationIntent,
				PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

		Notification notification = new NotificationCompat.Builder(this, channelId)
				.setContentTitle(getString(R.string.timeout_timer_title))
				.setContentText(timeString)
				.setSmallIcon(R.drawable.timer_icon)
				.setContentIntent(pendingIntent)
				.setOnlyAlertOnce(true)
				.build();

		startForeground(NOTIFICATION_ID, notification);

	}

	private void setUpAlarmNotification() {
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		long[] pattern = {0, 100, 100, 100};
		vibrator.vibrate(VibrationEffect.createWaveform(pattern, REPEAT_ONCE));

		mp = MediaPlayer.create(TimerService.this, R.raw.alarm_sound);
		mp.setLooping(true);
		mp.start();


		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(STOP_ALARM_BROADCAST);
		sendBroadcast(broadcastIntent);


		setUpNotification(getString(R.string.timer_stop_message), CHANNEL_HIGH_PRIORITY_ID);
	}

	public void stopSoundAndVibration() {
		if (vibrator != null) {
			vibrator.cancel();
		}
		if (mp != null) {
			mp.stop();
		}
	}

	/**
	 * https://developer.android.com/training/notify-user/build-notification
	 */
	private void setUpNotificationChannel(String channelName, String channelId, int importance) {

		NotificationChannel notificationChannel = new NotificationChannel(channelId, channelName, importance);
		NotificationManager notificationManager = getSystemService(NotificationManager.class);
		notificationManager.createNotificationChannel(notificationChannel);

	}

	public boolean isPaused() {
		return isPaused;
	}

	public boolean isFinish() {
		return isFinish;
	}

	public void pauseTimer() {
		timer.cancel();
		isPaused = true;
	}

	public void playTimer() {
		setUpTimerBroadcast(remainingMilliSeconds);
	}

	public String getOriginalTimeString() {
		long remainingMinutes = TimeUnit.MILLISECONDS.toMinutes(originalTimeInMilliSeconds);
		long remainingSeconds = TimeUnit.MILLISECONDS.toSeconds(originalTimeInMilliSeconds) -
				TimeUnit.MINUTES.toSeconds(remainingMinutes);

		return String.format(getString(R.string.time_label_format), remainingMinutes, remainingSeconds);
	}

	public long getOriginalMilliSeconds() {
		return originalTimeInMilliSeconds;
	}

	public void resetFinish(){
		isFinish = false;
	}

	public void changeSpeedPercentage(int newSpeedPercentage){
		speedPercentage = newSpeedPercentage;
		setUpNotification(getTimeString(), CHANNEL_LOW_PRIORITY_ID);
	}

	public int getSpeedPercentage() {
		return speedPercentage;
	}

	/**
	 * https://developer.android.com/guide/components/bound-services#Binder
	 */
	public class LocalBinder extends Binder {
		TimerService getService() {
			return TimerService.this;
		}
	}

	/**
	 * https://developer.android.com/guide/components/bound-services#Binder
	 */
	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}
}