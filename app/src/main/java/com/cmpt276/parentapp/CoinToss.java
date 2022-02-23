package com.cmpt276.parentapp;

import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageView;

/** This class uses matrix translations on ImageView to give the illusion of the coin flipping
 	Code inspired from StackOverflow answer: https://stackoverflow.com/questions/6135930/showing-both-sides-of-a-coin-being-flipped-using-android-standard-animation
*/
public class CoinToss extends Animation {
	private final float fromXDegrees;
	private final float toXDegrees;
	private final float fromYDegrees;
	private final float toYDegrees;
	private final float fromZDegrees;
	private final float toZDegrees;
	private Camera camera;
	private int width = 0;
	private int height = 0;
	private ImageView imageView;
	private int currentDrawable;
	private int nextDrawable;
	private int numOfRepetition = 0;


	public CoinToss(ImageView imageView, int currentDrawable, int nextDrawable, float fromXDegrees, float toXDegrees, float fromYDegrees, float toYDegrees, float fromZDegrees, float toZDegrees) {
		this.fromXDegrees = fromXDegrees;
		this.toXDegrees = toXDegrees;
		this.fromYDegrees = fromYDegrees;
		this.toYDegrees = toYDegrees;
		this.fromZDegrees = fromZDegrees;
		this.toZDegrees = toZDegrees;
		this.imageView = imageView;
		this.currentDrawable = currentDrawable;
		this.nextDrawable = nextDrawable;
	}

	@Override
	public void initialize(int width, int height, int parentWidth, int parentHeight) {
		super.initialize(width, height, parentWidth, parentHeight);
		this.width = width / 2;
		this.height = height / 2;
		camera = new Camera();
	}

	@Override
	protected void applyTransformation(float interpolatedTime, Transformation t) {
		float xDegrees = fromXDegrees + (toXDegrees * interpolatedTime);
		float yDegrees = fromYDegrees + (toYDegrees * interpolatedTime);
		float zDegrees = fromZDegrees + (toZDegrees * interpolatedTime);
		Matrix matrix = t.getMatrix();

		applyZoom(interpolatedTime);
		applyRotation(interpolatedTime);

		camera.save();
		camera.rotateX(-xDegrees);
		camera.rotateY(yDegrees);
		camera.rotateZ(zDegrees);
		camera.getMatrix(matrix);
		camera.restore();

		matrix.preTranslate(-this.width, -this.height);
		matrix.postTranslate(this.width, this.height);
	}

	private void applyZoom(float interpolatedTime) {
		int repeatCount = super.getRepeatCount();
		float scale = (numOfRepetition + interpolatedTime) / (repeatCount / 2.0f);

		if ((numOfRepetition + interpolatedTime) <= (repeatCount / 2.0f)) {
			imageView.setScaleX(0.25f + scale);
			imageView.setScaleY(0.25f + scale);
		}
		else if (numOfRepetition < repeatCount) {
			imageView.setScaleX(3 - scale);
			imageView.setScaleY(3 - scale);
		}
	}

	private void applyRotation(float interpolatedTime) {
		if (interpolatedTime >= 1.0f) {
			int temp = currentDrawable;
			currentDrawable = nextDrawable;
			nextDrawable = temp;
			numOfRepetition++;
		}
		else if (interpolatedTime <= 0.0f) {
			imageView.setImageResource(currentDrawable);
		}
		else {
			imageView.setImageResource(nextDrawable);
		}
	}
}