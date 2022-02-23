package com.cmpt276.parentapp.animations;

import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Animation to hold a view to a specific scale for a period of time.
 * */
public class StopAnimation extends Animation {
	int width, height;

	float scale;

	public StopAnimation(float scale) {
		this.scale = scale;
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

		matrix.postScale(scale, scale);

		matrix.preTranslate(-this.width, -this.height);
		matrix.postTranslate(this.width, this.height);
	}
}
