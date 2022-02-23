package com.cmpt276.parentapp.animations;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Animation for scaling to and from specified scales
 * */
public class InflateAnimation extends Animation {
	int width, height;

	float toScale, fromScale;

	public InflateAnimation(float toScale, float fromScale) {
		this.toScale = toScale;
		this.fromScale = fromScale;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		this.width = width / 2;
		this.height = height / 2;
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		Matrix matrix = t.getMatrix();

		float scaleFactor = toScale * interpolatedTime + fromScale * (1 - interpolatedTime);
		matrix.postScale(scaleFactor, scaleFactor);

		matrix.preTranslate(-this.width, -this.height);
		matrix.postTranslate(this.width, this.height);
	}

	@Override
	public void setDuration(long durationMillis) {
		super.setDuration(durationMillis);
	}
}
