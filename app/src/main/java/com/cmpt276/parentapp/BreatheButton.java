package com.cmpt276.parentapp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Button specifically for breathing in
 * */
public class BreatheButton extends androidx.appcompat.widget.AppCompatButton {

	public BreatheButton(@NonNull Context context) {
		super(context);
	}

	public BreatheButton(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public BreatheButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				performClick();
				return true;

			case MotionEvent.ACTION_UP:
				performClick();
				return false;
		}
		return false;
	}

	@Override
	public boolean performClick() {
		super.performClick();
		return false;
	}
}
