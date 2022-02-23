package com.cmpt276.parentapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

/**
 * Contains options for the timer and starts the TimerActivity based on the option selected
 */
public class TimerOptions extends AppCompatActivity {

	private final int EMPTY_STRING = -1;
	private final int NO_SELECTION = -2;
	private final int ZERO = 0;
	private int selected = NO_SELECTION;

	public static Intent getIntent(Context context) {
		return new Intent(context, TimerOptions.class);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_timer_options);

		setUpRadioButtons();
		setUpStartButton();
		setUpBackBtn();

	}

	private void setUpBackBtn() {
		Button backBtn = findViewById(R.id.backBtn_timerOptions);
		backBtn.setText(R.string.back);
		backBtn.setOnClickListener((view) -> finish());
	}

	private void setUpStartButton() {
		Button startButton = findViewById(R.id.timer_start_button);
		startButton.setOnClickListener(view -> {
			if (selected > ZERO) {
				Intent i = TimerActivity.getIntent(this, selected * 60000L);
				startActivity(i);
				finish();
			}
			else {
			    switch (selected){
                    case NO_SELECTION:
                        Toast.makeText(this, R.string.error_select_time, Toast.LENGTH_SHORT).show();
                        break;
                    case EMPTY_STRING:
                        Toast.makeText(this, R.string.error_enter_time, Toast.LENGTH_SHORT).show();
                        break;
                    case ZERO:
                        Toast.makeText(this, R.string.error_time_negative, Toast.LENGTH_SHORT).show();
                    default:
                        throw new IllegalStateException("Selection for timer is invalid.");
                }
			}
		});
	}

	private void setUpRadioButtons() {

		RadioGroup timerOptions = findViewById(R.id.timer_options_radio_group);

		EditText customMinutes = findViewById(R.id.custom_minutes_edit);

		int[] minutes = getResources().getIntArray(R.array.minutes_array);

		for (int minute_option : minutes) {
			RadioButton minuteButton = new RadioButton(this);
			setButtonGraphics(minuteButton, minute_option + " " + ((minute_option == 1) ? getString(R.string.minute) : getString(R.string.minutes)));
			timerOptions.addView(minuteButton);

			minuteButton.setOnClickListener(view -> {
				customMinutes.setVisibility(View.INVISIBLE);
				selected = minute_option;
			});

		}

		RadioButton customButton = new RadioButton(this);

		timerOptions.addView(customButton);
		setButtonGraphics(customButton, getString(R.string.custom_radio_option_string));

		customButton.setOnClickListener(view -> {
			selected = EMPTY_STRING;
			customMinutes.setVisibility(View.VISIBLE);
		});
		setUpTextWatcher(customMinutes);

	}

	private void setUpTextWatcher(EditText customMinutes) {
		TextWatcher textWatcher = new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
			}

			@Override
			public void afterTextChanged(Editable editable) {
				try {
					selected = Integer.parseInt(customMinutes.getText().toString());
				} catch (Exception e) {
					selected = EMPTY_STRING;
				}
			}
		};
		customMinutes.addTextChangedListener(textWatcher);
	}

	private void setButtonGraphics(RadioButton button, String text) {
		Typeface font = getResources().getFont(R.font.moon_bold_font);

		button.setText(text);
		button.setTypeface(font);
		button.setTextColor(Color.BLACK);
		button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
		button.setHighlightColor(getColor(R.color.mid_blue));
	}

}