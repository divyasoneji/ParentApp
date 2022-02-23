package com.cmpt276.parentapp;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cmpt276.parentapp.databinding.ActivityMainBinding;

import java.util.Calendar;

/**
 * Main menu for the application, hub for accessing other functions of the app.
 */
public class MainActivity extends AppCompatActivity {

    private static final long SLIDE_ENTRY_DELAY = 200;

	private ActivityMainBinding binding;
	private TimerService timerService;
	private boolean timerServiceBound = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		binding = ActivityMainBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		setUpWelcomeText();
		setUpAnimation();
		setUpMyChildrenButton();
		setUpCoinFlipButton();
		setUpTimerButton();
		setUpTaskListButton();
		setUpTakeBreathButton();
		setUpHelpBtn();
		setUpExitBtn();
	}

    private void setUpTakeBreathButton() {
        Button takeBreathButton = findViewById(R.id.take_breath_button);
        takeBreathButton.setOnClickListener(view -> {
            Intent i = TakeBreathActivity.getIntent(MainActivity.this);
            startActivity(i);
        });
    }

	private void setUpTaskListButton() {
		Button taskListButton = findViewById(R.id.task_list_button);
		taskListButton.setOnClickListener(view -> {
			Intent i = TaskActivity.getIntent(MainActivity.this);
			startActivity(i);
		});
	}

	private void setUpHelpBtn() {
		Button helpBtn = findViewById(R.id.helpBtn);
		helpBtn.setOnClickListener(view -> {
			Intent i = HelpActivity.getIntent(MainActivity.this);
			startActivity(i);
		});
	}

	private void setUpExitBtn() {
		Button backBtn = findViewById(R.id.exitBtn);
		backBtn.setText(R.string.exit);
		backBtn.setOnClickListener((view) -> finishAffinity());
	}

	@Override
	protected void onStart() {
		super.onStart();
		Intent intent = TimerService.getIntent(this);
		bindService(intent, connection, 0);
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unbindService(connection);
		timerServiceBound = false;
	}

	private void setUpTimerButton() {
		Button timerButton = findViewById(R.id.timer_button);
		timerButton.setOnClickListener(view -> {
			Intent intent;
			if (timerServiceBound) {
				intent = TimerActivity.getIntent(MainActivity.this, timerService.getOriginalMilliSeconds(), true, timerService.getSpeedPercentage());
			}
			else {
				intent = TimerOptions.getIntent(MainActivity.this);
			}
			startActivity(intent);
		});
	}

	private void setUpCoinFlipButton() {
		Button coinFlipButton = findViewById(R.id.flip_a_coin_button);
		coinFlipButton.setOnClickListener(view -> {
			Intent intent = CoinFlipActivity.getIntent(MainActivity.this);
			startActivity(intent);
		});
	}

	private void setUpMyChildrenButton() {
		Button myChildrenButton = findViewById(R.id.my_children_button);
		myChildrenButton.setOnClickListener(view -> {
			Intent intent = ChildrenActivity.getIntent(MainActivity.this);
			startActivity(intent);
		});
	}

	private void setUpWelcomeText() {
		TextView welcomeText = findViewById(R.id.main_menu_title);
		Calendar currentTime = Calendar.getInstance();
		int hour = currentTime.get(Calendar.HOUR_OF_DAY); //24 hour time
		if (hour < 12) {
			welcomeText.setText(R.string.good_morning);
		}
		else if (hour < 18) {
			welcomeText.setText(R.string.good_afternoon);
		}
		else {
			welcomeText.setText(R.string.good_evening);
		}

	}

	private void setUpAnimation() {

        Animation slideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideIn2 = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideIn3 = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideIn4 = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        Animation slideIn5 = AnimationUtils.loadAnimation(this, R.anim.slide_in);

        Button timerButton = findViewById(R.id.timer_button);
        Button coinFlipButton = findViewById(R.id.flip_a_coin_button);
        Button myChildrenButton = findViewById(R.id.my_children_button);
        Button taskButton = findViewById(R.id.task_list_button);
        Button takeBreathButton = findViewById(R.id.take_breath_button);

        coinFlipButton.setVisibility(View.INVISIBLE);
        timerButton.setVisibility(View.INVISIBLE);
        taskButton.setVisibility(View.INVISIBLE);
        takeBreathButton.setVisibility(View.INVISIBLE);

		myChildrenButton.startAnimation(slideIn);

        Handler handler = new Handler();
        handler.postDelayed(() -> {
            coinFlipButton.setVisibility(View.VISIBLE);
            coinFlipButton.startAnimation(slideIn2);
        }, SLIDE_ENTRY_DELAY);
        handler.postDelayed(() -> {
            timerButton.setVisibility(View.VISIBLE);
            timerButton.startAnimation(slideIn3);
        }, 2 * SLIDE_ENTRY_DELAY);
        handler.postDelayed(() -> {
            taskButton.setVisibility(View.VISIBLE);
            taskButton.startAnimation(slideIn4);
        }, 3 * SLIDE_ENTRY_DELAY);
        handler.postDelayed(() -> {
            takeBreathButton.setVisibility(View.VISIBLE);
            takeBreathButton.startAnimation(slideIn5);
        }, 4 * SLIDE_ENTRY_DELAY);

	}

	/**
	 * https://developer.android.com/guide/components/bound-services#Binder
	 */
	private ServiceConnection connection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName className,
									   IBinder service) {

			TimerService.LocalBinder binder = (TimerService.LocalBinder) service;
			timerService = binder.getService();
			timerServiceBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			timerServiceBound = false;
		}
	};
}