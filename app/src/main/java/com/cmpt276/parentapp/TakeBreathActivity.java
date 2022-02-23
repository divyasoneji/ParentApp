package com.cmpt276.parentapp;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.cmpt276.parentapp.animations.InflateAnimation;
import com.cmpt276.parentapp.animations.StopAnimation;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Activity for the deep breathing exercise as described in User Stories.
 * */
public class TakeBreathActivity extends AppCompatActivity {

	//units are in milliseconds
	private static final long TIME_BREATHE_IN = 3000L;
	private static final long TIME_BREATHE_IN_HELP = 10000L;
	private static final long TIME_BREATHE_OUT = 3000L;
	private static final long TIME_BREATHE_OUT_ANIMATION = 10000L;
	private static final long TIME_BREATHING_DELAY = 500L; //subject to change depending on needs

	private static final float BREATHING_VOLUME = 200.0f;

	private static final float ORIGINAL_SCALE = 1.0f;
	private static final float INFLATE_SCALE = 2.0f;

	private Options options = Options.getInstance();
	private State currentState = new IdleState();
	private MediaPlayer musicPlayer, breatheInPlayer, breatheOutPlayer;

	//numberBreathSetting is zero indexed, while breathsRemaining is (usually) a positive integer
	//the slider must be zero indexed for it to look normal.
	private int breathsRemaining;
	private int numberBreathSetting;

	private float breatheStartScale;
	private float exhaleStartScale;

	private View circleLight;
	private View circleDark;

	private float musicVolume = 1.0f;

	public void setState(State newState) {
		currentState.handleExit();
		currentState = newState;
		currentState.handleEnter();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_take_breath);

		setup();
	}

	private void setup() {
		Button breatheNumBtn = findViewById(R.id.breathNumBtn);
		breatheNumBtn.setVisibility(View.VISIBLE);

		SeekBar seekBarNumBreaths = findViewById(R.id.seekbar_num_breaths);
		seekBarNumBreaths.setVisibility(View.GONE);

		TextView textViewHelp = findViewById(R.id.textViewBreatheHelp);
		textViewHelp.setVisibility(View.GONE);

		Button breatheButton = findViewById(R.id.button_breathe);
		breatheButton.setText(R.string.begin);

		numberBreathSetting = options.getNumberOfBreaths(TakeBreathActivity.this);
		breathsRemaining = numberBreathSetting + 1;

		musicPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.just_relax_11157);
		musicPlayer.setLooping(true);

		breatheInPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.breathe_in);
		breatheOutPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.breathe_out);

		circleLight = findViewById(R.id.circleViewLight);
		circleLight.clearAnimation();
		circleLight.animate().alpha(1.0f).setDuration(1).start();

		circleDark = findViewById(R.id.circleViewDark);
		circleDark.clearAnimation();
		circleDark.animate().alpha(0.0f).setDuration(1).start();

		breatheStartScale = ORIGINAL_SCALE;
		exhaleStartScale = INFLATE_SCALE;

		setUpBreatheNumBtn();
		setUpBackButton();
		setUpBreatheButton();
		setUpSeekBar();
		setUpHeading();

		TextView textViewBreathsRemaining = findViewById(R.id.textViewBreathsRemaining);
		textViewBreathsRemaining.setText(getString(R.string.breaths_remaining, breathsRemaining));
	}

	private void setUpBreatheNumBtn() {
		Button breatheNumBtn = findViewById(R.id.breathNumBtn);
		SeekBar seekBarNumBreaths = findViewById(R.id.seekbar_num_breaths);

		seekBarNumBreaths.setVisibility(View.GONE);
		breatheNumBtn.setOnClickListener(view -> {
			if (seekBarNumBreaths.getVisibility() == View.VISIBLE) {
				seekBarNumBreaths.setVisibility(View.GONE);
			}
			else {
				seekBarNumBreaths.setVisibility(View.VISIBLE);
			}
		});
	}

	private void setUpHeading() {
		TextView heading = findViewById(R.id.textViewTakeBreath);
		heading.setText(getString(R.string.heading_take_breath, breathsRemaining));
	}

	public static Intent getIntent(Context context) {
		return new Intent(context, TakeBreathActivity.class);
	}

	private void setUpBackButton() {
		Button backButton = findViewById(R.id.buttonBackTakeBreath);
		backButton.setOnClickListener(view -> {
			musicPlayer.stop();
			breatheInPlayer.stop();
			breatheOutPlayer.stop();
			TakeBreathActivity.this.finish();
		});
	}

	private void setUpBreatheButton() {
		BreatheButton breatheButton = findViewById(R.id.button_breathe);

		breatheButton.setOnClickListener((view) -> {
			if (currentState instanceof ReadyState) {
				State inhaleState = new InhaleState();
				setState(inhaleState);
			}
		});

		breatheButton.setOnTouchListener((view, motionEvent) -> {
			//OnTouchListener's onTouch method returns true if the event was handled and false if not.
			//ACTION_DOWN is when the user initially presses on the view, and ACTION_UP is when the user
			//releases the view.
			switch (motionEvent.getAction()){
				case MotionEvent.ACTION_DOWN:
					currentState.onHoldButton();
					return true;
				case MotionEvent.ACTION_UP:
					view.performClick();
					currentState.onReleaseButton();
					return false;
				default:
					return false;
			}
		});
	}

	private void setUpSeekBar() {
		SeekBar seekBarNumBreaths = findViewById(R.id.seekbar_num_breaths);
		seekBarNumBreaths.setProgress(numberBreathSetting);
		seekBarNumBreaths.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
				numberBreathSetting = i; //i is zero indexed, we want the number of breaths to be nonzero
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				//do nothing
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				breathsRemaining = numberBreathSetting + 1;
				options.setNumberOfBreaths(TakeBreathActivity.this, numberBreathSetting);

				setUpHeading();
				TextView textViewBreathsRemaining = findViewById(R.id.textViewBreathsRemaining);
				textViewBreathsRemaining.setText(getString(R.string.breaths_remaining, breathsRemaining));
			}
		});
	}

	@Override
	public void onPause() {
		super.onPause();
		if (musicPlayer.isPlaying()){
			musicPlayer.stop();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setState(new ReadyState());
	}

	@Override
	public void onStop() {
		super.onStop();
		if (musicPlayer != null){
			musicPlayer.release();
		}
	}

	private void playMusic() {
		musicVolume = 1.0f;

		if (musicPlayer != null){
			musicPlayer.stop();
		}

		musicPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.just_relax_11157);
		musicPlayer.setLooping(true);
		musicPlayer.start();
	}

	//https://stackoverflow.com/questions/38380495/android-studio-mediaplayer-how-to-fade-in-and-out
	private void startFadeOutMusic(){
		final long FADE_DURATION = 1500;
		final long FADE_INTERVAL = 50;
		final float START_VOLUME = 1.0f;
		final float TARGET_VOLUME = 0.0f;
		long numberOfSteps = FADE_DURATION / FADE_INTERVAL;
		final float deltaVolume = (TARGET_VOLUME - START_VOLUME) / (float) numberOfSteps;

		final Timer timer = new Timer(true);
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				fadeInStep(deltaVolume);
				if (musicVolume <= TARGET_VOLUME) {
					timer.cancel();
					timer.purge();
					musicPlayer.stop();
				}
			}
		};

		timer.schedule(timerTask,FADE_INTERVAL,FADE_INTERVAL);
	}

	private void fadeInStep(float deltaVolume){
		musicPlayer.setVolume(musicVolume, musicVolume);
		musicVolume += deltaVolume;
	}

	//////////////////////////////////
	//            STATES            //
	//////////////////////////////////

	private abstract class State {

		//methods meant to be overridden, default implementation is empty.
		void handleEnter() {}

		void handleExit() {}

		void onHoldButton() {}

		void onReleaseButton() {}

	}

	private class ReadyState extends State {

		@Override
		void handleEnter() {
			Button breatheNumBtn = findViewById(R.id.breathNumBtn);
			breatheNumBtn.setVisibility(View.VISIBLE);

			SeekBar seekBarNumBreaths = findViewById(R.id.seekbar_num_breaths);
			seekBarNumBreaths.setVisibility(View.GONE);

			setup();
		}
	}

	private class InhaleState extends State {

		@Override
		void handleEnter() {
			Button breatheButton = findViewById(R.id.button_breathe);
			breatheButton.setText(R.string.breathe_in);

			TextView textViewHelp = findViewById(R.id.textViewBreatheHelp);
			textViewHelp.setVisibility(View.VISIBLE);
			textViewHelp.setText(R.string.breathe_in_help);

			musicPlayer.stop();
			breatheInPlayer.stop();

			circleLight.clearAnimation();
			circleDark.clearAnimation();

			circleLight.animate().alpha(1.0f).setDuration(1).start();
			circleDark.animate().alpha(0.0f).setDuration(1).start();
		}

		@Override
		void handleExit() {
			TextView textViewHelp = findViewById(R.id.textViewBreatheHelp);
			textViewHelp.setVisibility(View.GONE);
		}

		@Override
		void onHoldButton() {
			State inhalingState = new InhalingState();
			setState(inhalingState);
		}
	}

	private class InhalingState extends State {

		long startTime;

		@Override
		void handleEnter() {
			//start animation and sound
			breatheInPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.breathe_in);
			breatheInPlayer.setVolume(BREATHING_VOLUME, BREATHING_VOLUME);
			breatheInPlayer.start();

			if (!musicPlayer.isPlaying()) {
				playMusic();
			}

			Animation inflateCircleAnimationLight = new InflateAnimation(INFLATE_SCALE, breatheStartScale);
			inflateCircleAnimationLight.setInterpolator(new LinearInterpolator());
			inflateCircleAnimationLight.setDuration(TIME_BREATHE_IN_HELP);
			inflateCircleAnimationLight.setFillAfter(true);
			inflateCircleAnimationLight.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					circleLight.animate().alpha(0.0f).setDuration(TIME_BREATHE_IN_HELP);
				}

				@Override
				public void onAnimationEnd(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});

			Animation inflateCircleAnimationDark = new InflateAnimation(INFLATE_SCALE, breatheStartScale);
			inflateCircleAnimationDark.setInterpolator(new LinearInterpolator());
			inflateCircleAnimationDark.setDuration(TIME_BREATHE_IN_HELP);
			inflateCircleAnimationDark.setFillAfter(true);
			inflateCircleAnimationDark.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					circleDark.animate().alpha(1.0f).setDuration(TIME_BREATHE_IN_HELP);
				}

				@Override
				public void onAnimationEnd(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});

			startTime = AnimationUtils.currentAnimationTimeMillis();

			circleLight.startAnimation(inflateCircleAnimationLight);
			circleDark.startAnimation(inflateCircleAnimationDark);

			Handler handler = new Handler();
			handler.postDelayed(() -> {
				//Check if we're still in the same instance of the state after the allotted time.
				//This prevents situations such as the user pressing the button, releasing after one
				//second, then holding it, which results in the state changing too early.
				if (currentState == InhalingState.this){
					breatheInPlayer.start();
					State inhaleReleaseState = new InhaleReleaseState(startTime);
					setState(inhaleReleaseState);
				}
			}, TIME_BREATHE_IN);
		}

		@Override
		void onReleaseButton() {
			State inhaleState = new InhaleState();
			setState(inhaleState);
		}
	}

	private class InhaleReleaseState extends State {

		long startTime;

		InhaleReleaseState(long startTime) {
			this.startTime = startTime;
		}

		@Override
		void handleEnter() {
			Button breatheButton = findViewById(R.id.button_breathe);
			breatheButton.setText(R.string.breathe_out);

			Handler handler = new Handler();
			handler.postDelayed(() -> {
				//Check if we're still in the same instance of the state after the allotted time.
				//This prevents situations such as the user pressing the button, releasing after one
				//second, then holding it, which results in the state changing too early.
				if (currentState == InhaleReleaseState.this){
					State inhaleReleaseHelpState = new InhaleReleaseHelpState(startTime);
					setState(inhaleReleaseHelpState);
				}
			}, TIME_BREATHE_IN_HELP - TIME_BREATHE_IN);
		}

		@Override
		void onReleaseButton() {
			State inhaleDoneState = new InhaleDoneState(startTime);
			setState(inhaleDoneState);
		}
	}

	private class InhaleReleaseHelpState extends State {

		long startTime;

		InhaleReleaseHelpState(long startTime) {
			this.startTime = startTime;
		}

		@Override
		void handleEnter() {
			TextView textViewPrompt = findViewById(R.id.textViewBreatheHelp);
			textViewPrompt.setText(R.string.release_breath_help);
			textViewPrompt.setVisibility(View.VISIBLE);
		}

		@Override
		void handleExit() {
			TextView textViewPrompt = findViewById(R.id.textViewBreatheHelp);
			textViewPrompt.setVisibility(View.GONE);
		}

		@Override
		void onHoldButton() {
			//do nothing
		}

		@Override
		void onReleaseButton() {
			State inhaleDoneState = new InhaleDoneState(startTime);
			setState(inhaleDoneState);
		}
	}

	private class InhaleDoneState extends State {

		long currentTime;
		long startTime;

		InhaleDoneState(long startTime) {
			this.startTime = startTime;
			this.currentTime = AnimationUtils.currentAnimationTimeMillis();
		}

		@Override
		void handleEnter() {

			long deltaTime = currentTime - startTime;
			if (deltaTime > TIME_BREATHE_IN_HELP) {
				deltaTime = TIME_BREATHE_IN_HELP;
			}

			//stop animation and sound
			breatheInPlayer.stop();
			startFadeOutMusic();

			float currentScale = breatheStartScale + ((INFLATE_SCALE - breatheStartScale) * deltaTime / TIME_BREATHE_IN_HELP);
			float currentAlpha = (float) deltaTime / TIME_BREATHE_IN_HELP;

			exhaleStartScale = currentScale;

			Animation stopAnimation = new StopAnimation(currentScale);
			stopAnimation.setDuration(TIME_BREATHING_DELAY);
			stopAnimation.setFillAfter(true);

			circleLight.setAlpha(1.0f - currentAlpha);
			circleLight.startAnimation(stopAnimation);

			circleDark.setAlpha(currentAlpha);
			circleDark.startAnimation(stopAnimation);

			final long time = deltaTime;
			Handler handler = new Handler();
			handler.postDelayed(() -> {
				State exhaleState = new ExhaleState(time);
				setState(exhaleState);
			}, TIME_BREATHING_DELAY);
		}
	}

	private class ExhaleState extends State {

		long deltaTime;
		long startExhaleTime;

		ExhaleState(long deltaTime) {
			this.deltaTime = deltaTime;
			startExhaleTime = AnimationUtils.currentAnimationTimeMillis();
		}

		@Override
		void handleEnter() {
			//play exhaling animation/sound

			breatheOutPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.breathe_out);
			breatheOutPlayer.setVolume(BREATHING_VOLUME, BREATHING_VOLUME);
			breatheOutPlayer.start();

			float exhaleScale = breatheStartScale + ((INFLATE_SCALE - breatheStartScale) / TIME_BREATHE_IN_HELP) * deltaTime;

			Animation deflateCircleAnimationLight = new InflateAnimation(ORIGINAL_SCALE, exhaleScale);
			deflateCircleAnimationLight.setDuration(TIME_BREATHE_OUT_ANIMATION);
			deflateCircleAnimationLight.setFillAfter(true);
			deflateCircleAnimationLight.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					circleLight.animate().alpha(1.0f).setDuration(TIME_BREATHE_OUT_ANIMATION);
				}

				@Override
				public void onAnimationEnd(Animation animation) {
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});

			Animation deflateCircleAnimationDark = new InflateAnimation(ORIGINAL_SCALE, exhaleScale);
			deflateCircleAnimationDark.setDuration(TIME_BREATHE_OUT_ANIMATION);
			deflateCircleAnimationDark.setFillAfter(true);
			deflateCircleAnimationDark.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					circleDark.animate().alpha(0.0f).setDuration(TIME_BREATHE_OUT_ANIMATION);
				}

				@Override
				public void onAnimationEnd(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});

			circleLight.startAnimation(deflateCircleAnimationLight);
			circleDark.startAnimation(deflateCircleAnimationDark);

			Handler handler = new Handler();
			handler.postDelayed(() -> {
				State exhaleReleaseState = new ExhaleReleaseState(startExhaleTime);
				setState(exhaleReleaseState);
			}, TIME_BREATHE_OUT);
		}
	}

	private class ExhaleReleaseState extends State {

		long startExhaleTime;

		ExhaleReleaseState(long startExhaleTime) {
			this.startExhaleTime = startExhaleTime;
		}

		@Override
		void handleEnter() {
			breathsRemaining--;

			TextView textViewBreathsRemaining = findViewById(R.id.textViewBreathsRemaining);
			textViewBreathsRemaining.setText(getString(R.string.breaths_remaining, breathsRemaining));

			Button breatheButton = findViewById(R.id.button_breathe);
			if (breathsRemaining > 0){
				breatheButton.setText(R.string.breathe_in);
			}
			else {
				breatheButton.setText(R.string.good_job);
			}

			Handler handler = new Handler();
			handler.postDelayed(() -> {
				if (currentState == ExhaleReleaseState.this){
					State exhaleDoneState = new ExhaleDoneState(ExhaleDoneState.WAITED, startExhaleTime);
					setState(exhaleDoneState);
				}
			}, TIME_BREATHE_OUT_ANIMATION - TIME_BREATHE_OUT);
		}

		@Override
		void handleExit() {

		}

		@Override
		void onHoldButton() {
			State exhaleDoneState = new ExhaleDoneState(ExhaleDoneState.BUTTON_PRESSED, startExhaleTime);
			setState(exhaleDoneState);
		}

		@Override
		void onReleaseButton() {
			//do nothing
		}
	}

	private class ExhaleDoneState extends State {

		public static final int WAITED = 0;
		public static final int BUTTON_PRESSED = 1;

		private final int transition;
		long startExhaleTime;

		public ExhaleDoneState(int transition, long startExhaleTime) {
			this.transition = transition;
			this.startExhaleTime = startExhaleTime;
		}

		@Override
		void handleEnter() {
			breatheOutPlayer.stop();

			circleLight.clearAnimation();
			circleDark.clearAnimation();

			long currentTime = AnimationUtils.currentAnimationTimeMillis();
			long deltaTime = currentTime - startExhaleTime;
			if (deltaTime > TIME_BREATHE_OUT_ANIMATION) {
				deltaTime = TIME_BREATHE_OUT_ANIMATION;
			}

			breatheStartScale = exhaleStartScale + ((ORIGINAL_SCALE - exhaleStartScale) / TIME_BREATHE_OUT_ANIMATION) * deltaTime;

			if (breathsRemaining <= 0){
				State promptMoreState = new PromptMoreState();
				setState(promptMoreState);
				return;
			}

			//change either to InhaleState or InhalingState depending on whether the button is currently being held or not.
			State buttonHeldState;
			switch (transition){
				case WAITED:
					buttonHeldState = new InhaleState();
					break;
				case BUTTON_PRESSED:
					buttonHeldState = new InhalingState();
					break;
				default:
					throw new IllegalStateException("Cannot have transition outside defined boundaries.");
			}
			setState(buttonHeldState);
		}
	}

	private class PromptMoreState extends State {

		@Override
		void handleEnter() {
			Button breatheNumBtn = findViewById(R.id.breathNumBtn);
			breatheNumBtn.setVisibility(View.VISIBLE);

			Button breatheButton = findViewById(R.id.button_breathe);
			breatheButton.setText(R.string.begin);

			TextView heading = findViewById(R.id.textViewTakeBreath);
			heading.setText(getString(R.string.heading_take_breath, numberBreathSetting + 1));

			TextView breathCount = findViewById(R.id.textViewBreathsRemaining);
			breathCount.setText(getString(R.string.breaths_remaining, numberBreathSetting + 1));

			TextView textViewHelp = findViewById(R.id.textViewBreatheHelp);
			textViewHelp.setVisibility(View.VISIBLE);
			textViewHelp.setText(R.string.set_more_breaths);

			musicPlayer.stop();
		}

		@Override
		void handleExit() {
			Button breatheNumBtn = findViewById(R.id.breathNumBtn);
			breatheNumBtn.setVisibility(View.INVISIBLE);

			SeekBar seekBarNumBreaths = findViewById(R.id.seekbar_num_breaths);
			seekBarNumBreaths.setVisibility(View.GONE);

			musicPlayer = MediaPlayer.create(TakeBreathActivity.this, R.raw.just_relax_11157);
			musicPlayer.setLooping(true);
			musicPlayer.start();
		}

		@Override
		void onHoldButton() {
			breathsRemaining = numberBreathSetting + 1;
			State inhaleState = new InhaleState();
			setState(inhaleState);
		}

		@Override
		void onReleaseButton() {

		}
	}

	private class IdleState extends State {}
}