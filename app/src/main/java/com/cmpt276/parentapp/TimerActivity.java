package com.cmpt276.parentapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.IBinder;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Contains the UI for the timer
 */
public class TimerActivity extends AppCompatActivity {

    private static final String ORIGINAL_TIME_IN_MILLI_SECONDS_TAG = "original_time_in_milli_seconds_tag";
    private static final String TIMER_SERVICE_BROADCAST = "timer_service_broadcast";
    private static final String STOP_ALARM_BROADCAST = "stop_alarm_broadcast";
    private static final String SERVICE_RUNNING_FLAG = "service_running_flag";
    private static final String SPEED_PERCENTAGE_TAG = "speed_percentage_tag";
    private static final long DEFAULT_MINUTES_IN_MILLI_SECONDS = 0L;
    private static final int DEFAULT_SPEED_PERCENTAGE = 100;
    private static final double SECONDS_CONVERSION = 1 / 1000.0;
    private static final int ZERO = 0;


    TextView remainingTime;
    private Intent serviceIntent;
    private long originalTimeInMilliSeconds;
    private boolean isServiceRunning;
    private Menu menu;
    private Dialog selectSpeedDialog;
    int speedPercentage;

    private BroadcastReceiver timerReceiver;
    private BroadcastReceiver stopAlarmReceiver;

    private TimerService timerService;
    private boolean timerServiceBound = false;


    public static Intent getIntent(Context context, long minutesInMilliSeconds) {
        return TimerActivity.getIntent(context, minutesInMilliSeconds, false, DEFAULT_SPEED_PERCENTAGE);
    }

    public static Intent getIntent(Context context,
                                   long originalTimeInMilliSeconds,
                                   boolean isServiceRunning,
                                   int speedPercentage) {

        Intent intent = new Intent(context, TimerActivity.class);
        intent.putExtra(ORIGINAL_TIME_IN_MILLI_SECONDS_TAG, originalTimeInMilliSeconds);
        intent.putExtra(SERVICE_RUNNING_FLAG, isServiceRunning);
        intent.putExtra(SPEED_PERCENTAGE_TAG, speedPercentage);

        return intent;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);

        originalTimeInMilliSeconds = this.getIntent().getLongExtra(ORIGINAL_TIME_IN_MILLI_SECONDS_TAG, DEFAULT_MINUTES_IN_MILLI_SECONDS);
        isServiceRunning = this.getIntent().getBooleanExtra(SERVICE_RUNNING_FLAG, false);
        speedPercentage = this.getIntent().getIntExtra(SPEED_PERCENTAGE_TAG, DEFAULT_SPEED_PERCENTAGE);
        setUpToolBar();
        setUpPieChart();
        setUpPausePlayButton();
        setUpResetButton();
        setUpNewTimerButton();
        setUpSpeedText();
    }


    @SuppressLint("StringFormatInvalid")
    private void setUpSpeedText() {
        TextView speedText = findViewById(R.id.speed_text);
        speedText.setText(String.format(getString(R.string.speed_text), speedPercentage));
    }

    private void setUpToolBar() {
        setSupportActionBar(findViewById(R.id.timer_toolbar));
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private void setUpPieChart() {
        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setMax((int) (originalTimeInMilliSeconds * SECONDS_CONVERSION));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.timer_menu, menu);
        updateSpeedButtonVisibility();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_speed: {
                displaySpeedSelectionDialog();
                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateSpeedPercentage() {
        if (!timerServiceBound) {
            setUpStartService();
        } else {
            if (timerService.isPaused()) {
                timerService.changeSpeedPercentage(speedPercentage);
            } else {
                timerService.pauseTimer();
                timerService.changeSpeedPercentage(speedPercentage);
                timerService.playTimer();
            }
        }
        updatePausePlayButtonText();
    }


    @Override
    protected void onStart() {
        super.onStart();
        setUpStartService();
        setupTimerBroadCastReceiver();
        setUpStopAlarmReceiver();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(timerReceiver);
        unregisterReceiver(stopAlarmReceiver);
        unbindService(connection);
        finish();
    }

    private void setUpStartService() {
        serviceIntent = TimerService.getIntent(this, originalTimeInMilliSeconds, speedPercentage);

        if (!isServiceRunning) {
            startService(serviceIntent);
            isServiceRunning = true;
        }

        bindService(serviceIntent, connection, 0);
    }

    private void setupTimerBroadCastReceiver() {
        remainingTime = findViewById(R.id.time_text);

        timerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateTimerLabelAndChart();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(TIMER_SERVICE_BROADCAST);
        registerReceiver(timerReceiver, filter);
    }

    private void displaySpeedSelectionDialog() {

        selectSpeedDialog = new Dialog(this);
        selectSpeedDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectSpeedDialog.setCancelable(false);
        selectSpeedDialog.setContentView(R.layout.change_speed_dialog);
        selectSpeedDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        AtomicInteger selected = new AtomicInteger(speedPercentage);

        RadioGroup speedGroup = selectSpeedDialog.findViewById(R.id.change_speed_radio_group);
        int[] speeds = getResources().getIntArray(R.array.timer_speeds_array);

        for (int speed_option : speeds) {
            RadioButton speedButton = new RadioButton(speedGroup.getContext());
            setButtonGraphics(speedButton, speed_option + getString(R.string.percent_symbol));
            speedGroup.addView(speedButton);

            if (speed_option == speedPercentage) {
                speedButton.setChecked(true);
            }

            speedButton.setOnClickListener(view -> selected.set(speed_option));
        }

        FloatingActionButton selectFab = selectSpeedDialog.findViewById(R.id.select_speed_fab);
        selectFab.setOnClickListener(view -> {
            speedPercentage = selected.get();
            updateSpeedPercentage();
            setUpSpeedText();
            selectSpeedDialog.dismiss();
        });

        FloatingActionButton confirmFab = selectSpeedDialog.findViewById(R.id.cancel_change_speed_fab);
        confirmFab.setOnClickListener(view -> selectSpeedDialog.dismiss());
        selectSpeedDialog.show();
    }

    private void setButtonGraphics(RadioButton button, String text) {
        Typeface font = getResources().getFont(R.font.moon_bold_font);

        button.setText(text);
        button.setTypeface(font);
        button.setTextColor(Color.BLACK);
        button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        button.setHighlightColor(getColor(R.color.mid_blue));
    }

    private void updateTimerLabelAndChart() {
        TextView timeText = findViewById(R.id.time_text);
        timeText.setText(timerService.getTimeString());

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress((int) timerService.getProgress());
    }

    private void resetTimer() {

        String originalTime = timerService.getOriginalTimeString();
        timerService.resetFinish();

        stopService(serviceIntent);
        isServiceRunning = false;

        TextView timeText = findViewById(R.id.time_text);
        timeText.setText(originalTime);

        ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setProgress((int)(originalTimeInMilliSeconds * SECONDS_CONVERSION));
    }

    private void setUpNewTimerButton() {

        Button editTimeButton = findViewById(R.id.new_timer_button);

        editTimeButton.setOnClickListener(view -> {

            if (isServiceRunning) {
                stopService(serviceIntent);
                isServiceRunning = false;
            }

            Intent intent = TimerOptions.getIntent(this);
            startActivity(intent);

            finish();

        });
    }

    private void setUpResetButton() {
        Button resetButton = findViewById(R.id.reset_button);
        resetButton.setOnClickListener(view -> resetTimer());
    }

    private void updatePausePlayButtonText() {
        Button pausePlayButton = findViewById(R.id.pause_play);

        if ((!timerServiceBound) || timerService.isPaused()) {
            pausePlayButton.setText(R.string.play_button_text);

        } else {
            pausePlayButton.setText(R.string.pause_button_text);
        }

    }

    private void setUpPausePlayButton() {
        Button pausePlayButton = findViewById(R.id.pause_play);

        pausePlayButton.setOnClickListener(view -> {
            if (!timerServiceBound) {
                setUpStartService();
            } else {
                if (timerService.isPaused()) {
                    timerService.playTimer();
                } else {
                    timerService.pauseTimer();
                    updateTimerLabelAndChart();
                }
            }
            updatePausePlayButtonText();
        });

    }

    private void setUpStopAlarmReceiver() {
        stopAlarmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                setUpStopAlarmButton();
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(STOP_ALARM_BROADCAST);
        registerReceiver(stopAlarmReceiver, filter);
    }

    private void updateSpeedButtonVisibility() {
        if (menu == null || timerService == null) {
            return;
        }

        MenuItem item = menu.findItem(R.id.action_speed);
        item.setVisible(!timerService.isFinish());
    }

    private void setUpStopAlarmButton() {

        if (timerService.isFinish()) {
            ProgressBar progressBar = findViewById(R.id.progressBar);
            progressBar.setProgress(ZERO);

            Button stopAlarmButton = findViewById(R.id.stop_alarm_button);
            Button pausePlayButton = findViewById(R.id.pause_play);
            Button resetButton = findViewById(R.id.reset_button);
            Button newTimerButton = findViewById(R.id.new_timer_button);
            TextView speedText = findViewById(R.id.speed_text);

            stopAlarmButton.setVisibility(View.VISIBLE);
            pausePlayButton.setVisibility(View.INVISIBLE);
            resetButton.setVisibility(View.INVISIBLE);
            newTimerButton.setVisibility(View.INVISIBLE);
            speedText.setVisibility(View.INVISIBLE);
            updateSpeedButtonVisibility();
            if(selectSpeedDialog != null){
                selectSpeedDialog.dismiss();
            }

            stopAlarmButton.setOnClickListener(view -> {
                timerService.stopSoundAndVibration();
                resetTimer();
                updatePausePlayButtonText();
                stopAlarmButton.setVisibility(View.INVISIBLE);
                pausePlayButton.setVisibility(View.VISIBLE);
                resetButton.setVisibility(View.VISIBLE);
                newTimerButton.setVisibility(View.VISIBLE);
                speedText.setVisibility(View.VISIBLE);
                updateSpeedButtonVisibility();
            });
        }

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

            updateTimerLabelAndChart();
            updatePausePlayButtonText();
            setUpStopAlarmButton();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            timerServiceBound = false;
            updatePausePlayButtonText();
        }
    };

}