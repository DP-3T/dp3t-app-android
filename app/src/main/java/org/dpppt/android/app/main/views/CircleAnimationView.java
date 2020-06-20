/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */
package org.dpppt.android.app.main.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.dpppt.android.app.R;

public class CircleAnimationView extends View {

	private static final int MAX_NUM_CIRCLE = 20;

	private static final float CIRCLE_RADIUS_START_FRAC = 0.15f;
	private static final float CIRCLE_RADIUS_DELTA = 0.4f;
	private static final float CIRCLE_SIZE_DELTA = 2f;
	private static final long CIRCLE_MAX_AGE = 2000;
	private static final long CIRCLE_PAUSE_DELAY = 5000;
	private static final long CIRCLE_CONSEC_DELAY = CIRCLE_MAX_AGE - 500;
	private static final float CIRCLE_FADE_IN_FRAC = 0.1f;
	private static final long INITIAL_DELAY_CIRCLE =
			2 * HeaderView.INITIAL_DELAY + HeaderView.ICON_ANIM_DURATION + HeaderView.ICON_ANIM_DELAY;

	private Handler circleHandler;
	private CircleRunnable circleRunnable;
	private ConcurrentLinkedQueue<CircleObject> circles = new ConcurrentLinkedQueue<>();

	private Paint paintCircle;
	private int circleStrokeWidth;
	private int circleAlpha;
	private int centerX;
	private int centerY;

	public CircleAnimationView(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public CircleAnimationView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public CircleAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public CircleAnimationView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		paintCircle = new Paint();
		paintCircle.setStyle(Paint.Style.STROKE);
		paintCircle.setAntiAlias(true);
		int circleColor = getResources().getColor(R.color.white_10, null);
		paintCircle.setColor(circleColor);
		circleStrokeWidth = getResources().getDimensionPixelSize(R.dimen.header_stroke_width_circle);
		circleAlpha = Color.alpha(circleColor);

		circleHandler = new Handler();
	}

	public void setCenter(int x, int y) {
		centerX = x;
		centerY = y;
	}

	public void stopAnimation() {
		if (circleRunnable != null) {
			circleRunnable.stop();
			circleHandler.removeCallbacksAndMessages(null);
		}
	}

	public void setState(boolean isActive, boolean initialUpdate) {
		stopAnimation();
		if (isActive) {
			circleRunnable = new CircleRunnable(3);
			circleHandler.postDelayed(circleRunnable, initialUpdate ? INITIAL_DELAY_CIRCLE : 0);
		}
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (circles.size() == 0) return;

		long now = System.currentTimeMillis();
		int halfW = Math.round(getWidth() * 0.5f);

		Iterator<CircleObject> iter = circles.iterator();
		while (iter.hasNext()) {
			CircleObject circle = iter.next();
			float progress = (now - circle.getBirthMs()) / (float) CIRCLE_MAX_AGE;

			if (progress >= 1) {
				iter.remove();
				continue;
			}

			int radius = Math.round((CIRCLE_RADIUS_START_FRAC + progress * CIRCLE_RADIUS_DELTA) * halfW);
			float scaleFactor = 1 + progress * CIRCLE_SIZE_DELTA;
			paintCircle.setStrokeWidth(scaleFactor * circleStrokeWidth);
			int alpha = Math.round(
					(progress > CIRCLE_FADE_IN_FRAC ? Math.max(1 - progress, 0) : progress * 1 / CIRCLE_FADE_IN_FRAC) *
							circleAlpha);
			paintCircle.setAlpha(alpha);

			canvas.drawCircle(centerX, centerY, radius, paintCircle);
		}

		if (circles.size() > 0) invalidate();
	}

	public void spawnCircle() {
		if (circles.size() < MAX_NUM_CIRCLE) {
			circles.add(new CircleObject(System.currentTimeMillis()));
			invalidate();
		}
	}

	private class CircleObject {
		private final long birthMs;

		private CircleObject(long birthMs) {this.birthMs = birthMs;}

		public long getBirthMs() {
			return birthMs;
		}

	}


	private class CircleRunnable implements Runnable {
		private boolean run = true;
		private final int newConsecutives;

		private CircleRunnable(int numConsecutives) {
			newConsecutives = Math.max(numConsecutives - 1, 0);
		}

		public void stop() {
			run = false;
		}

		@Override
		public void run() {
			if (!run) return;

			circleRunnable = new CircleRunnable(newConsecutives);
			long circleDelay = newConsecutives > 0 ? CIRCLE_CONSEC_DELAY : CIRCLE_PAUSE_DELAY;

			spawnCircle();
			circleHandler.postDelayed(circleRunnable, circleDelay);
		}

	}

}
