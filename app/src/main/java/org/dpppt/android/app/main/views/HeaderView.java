/*
 * Created by Ubique Innovation AG
 * https://www.ubique.ch
 * Copyright (c) 2020. All rights reserved.
 */

package org.dpppt.android.app.main.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.*;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.dpppt.android.app.R;
import org.dpppt.android.app.main.model.AppState;

public class HeaderView extends FrameLayout {

	private static final int MAX_NUM_ARCS = 20;

	private static final long COLOR_ANIM_DURATION = 500;
	private static final long ICON_ANIM_DURATION = 500;
	private static final long ICON_ANIM_DELAY = 200;
	private static final float ICON_BG_BUMP_FACTOR = 0.95f;
	private static final float ANIM_OVERSHOOT_TENSION = 2;
	private static final float ARC_RADIUS_START_FRAC = 0.25f;
	private static final float ARC_RADIUS_DELTA = 0.4f;
	private static final float ARC_SIZE_DELTA = 1.25f;
	private static final float ARC_HALF_ANGLE_DEG = 30;
	private static final long ARC_MAX_AGE = 2000;
	private static final long ARC_PAUSE_DELAY = 5000;
	private static final long ARC_CONSEC_DELAY = ARC_MAX_AGE - 500;
	private static final float ARC_FADE_IN_FRAC = 0.1f;
	private static final long INITIAL_DELAY = 500;
	private static final long INITIAL_DELAY_ARC = 2 * INITIAL_DELAY + ICON_ANIM_DURATION + ICON_ANIM_DELAY;

	private ImageView icon;
	private ImageView iconBackground;

	private AppState currentState;
	private AnimatorSet iconAnimatorSet;
	private ValueAnimator colorAnimator;
	private Handler arcHandler;
	private ArcRunnable arcRunnable;
	private ConcurrentLinkedQueue<ArcObject> arcs = new ConcurrentLinkedQueue<>();
	private Paint paintArc;
	private int arcStrokeWidth;
	private int arcAlpha;

	public HeaderView(Context context) {
		super(context);
		init(context, null, 0, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs, 0, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs, defStyleAttr, 0);
	}

	public HeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs, defStyleAttr, defStyleRes);
	}

	private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		setForegroundGravity(Gravity.CENTER);
		setBackgroundColor(Color.TRANSPARENT);
		View headerContent = LayoutInflater.from(context).inflate(R.layout.view_header, this, true);
		icon = headerContent.findViewById(R.id.main_header_icon);
		icon.setScaleX(0);
		icon.setScaleY(0);
		iconBackground = headerContent.findViewById(R.id.main_header_icon_bg);
		iconBackground.setScaleX(0);
		iconBackground.setScaleY(0);

		paintArc = new Paint();
		paintArc.setStyle(Paint.Style.STROKE);
		paintArc.setAntiAlias(true);
		int arcColor = getResources().getColor(R.color.status_arc, null);
		paintArc.setColor(arcColor);
		arcStrokeWidth = getResources().getDimensionPixelSize(R.dimen.header_stroke_width_arc);
		arcAlpha = Color.alpha(arcColor);

		arcHandler = new Handler();
	}

	public void stopArcAnimation() {
		if (arcRunnable != null) {
			arcRunnable.stop();
			arcHandler.removeCallbacksAndMessages(null);
		}
	}

	public void setState(AppState state) {
		if (currentState == state) return;
		boolean initialUpdate = currentState == null;

		int backgroundColor = Color.TRANSPARENT;
		int iconRes = 0;
		String contentDescription = null;
		switch (state) {
			case TRACING_ACTIVATED:
				iconRes = (R.drawable.ic_header_check);
				backgroundColor = getResources().getColor(R.color.status_green, null);
				contentDescription = getResources().getString(R.string.state_tracing);
				break;
			case TRACING_DEACTIVATED:
				iconRes = (R.drawable.ic_header_error);
				backgroundColor = getResources().getColor(R.color.status_red, null);
				contentDescription = getResources().getString(R.string.state_tracing_deactivated);
				break;
			case ERROR:
				iconRes = (R.drawable.ic_header_error);
				backgroundColor = getResources().getColor(R.color.status_red, null);
				contentDescription = getResources().getString(R.string.state_error);
				break;
			case EXPOSED_ERROR:
			case EXPOSED:
				iconRes = (R.drawable.ic_header_info);
				backgroundColor = getResources().getColor(R.color.status_blue, null);
				contentDescription = getResources().getString(R.string.state_exposed);
				break;
		}
		iconBackground.setImageResource(R.drawable.ic_header_background);

		setContentDescription(contentDescription);

		if (colorAnimator != null && colorAnimator.isRunning()) colorAnimator.cancel();
		int startColor = ((ColorDrawable) getBackground()).getColor();
		int endColor = backgroundColor;
		colorAnimator = ValueAnimator.ofArgb(startColor, endColor);
		colorAnimator.setDuration(COLOR_ANIM_DURATION);
		colorAnimator.addUpdateListener(animation -> setBackgroundColor((int) animation.getAnimatedValue()));
		colorAnimator.start();

		if (iconAnimatorSet != null && iconAnimatorSet.isRunning()) iconAnimatorSet.cancel();
		if (initialUpdate) {
			Animator iconAnimator =
					createSizeAnimation(icon, icon.getScaleX(), 1, ICON_ANIM_DURATION, ICON_ANIM_DELAY + INITIAL_DELAY);
			Animator iconBgAnimator =
					createSizeAnimation(iconBackground, iconBackground.getScaleX(), 1, ICON_ANIM_DURATION, INITIAL_DELAY);
			icon.setImageResource(iconRes);
			iconAnimatorSet = new AnimatorSet();
			iconAnimatorSet.playTogether(iconAnimator, iconBgAnimator);
			iconAnimatorSet.start();
		} else {
			iconAnimatorSet = createIconSwitchAnimation(icon, iconBackground, iconRes, ICON_ANIM_DURATION);
			iconAnimatorSet.start();
		}

		stopArcAnimation();
		if (state != AppState.ERROR && state != AppState.EXPOSED_ERROR && state != AppState.TRACING_DEACTIVATED) {
			arcRunnable = new ArcRunnable(3);
			arcHandler.postDelayed(arcRunnable, initialUpdate ? INITIAL_DELAY_ARC : 0);
		}

		currentState = state;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (arcs.size() == 0) return;

		long now = System.currentTimeMillis();
		int halfW = Math.round(getWidth() * 0.5f);
		int halfH = Math.round(getHeight() * 0.5f);

		Iterator<ArcObject> iter = arcs.iterator();
		while (iter.hasNext()) {
			ArcObject arc = iter.next();
			float progress = (now - arc.getBirthMs()) / (float) ARC_MAX_AGE;

			if (progress >= 1) {
				iter.remove();
				continue;
			}

			int radius = Math.round((ARC_RADIUS_START_FRAC + progress * ARC_RADIUS_DELTA) * halfW);
			float scaleFactor = 1 + progress * ARC_SIZE_DELTA;
			paintArc.setStrokeWidth(scaleFactor * arcStrokeWidth);
			int alpha = Math.round(
					(progress > ARC_FADE_IN_FRAC ? Math.max(1 - progress, 0) : progress * 1 / ARC_FADE_IN_FRAC) * arcAlpha);
			paintArc.setAlpha(alpha);

			int left = halfW - radius;
			int top = halfH - radius;
			int right = halfW + radius;
			int bottom = halfH + radius;
			canvas.drawArc(left, top, right, bottom, -ARC_HALF_ANGLE_DEG, 2 * ARC_HALF_ANGLE_DEG, false, paintArc);
			canvas.drawArc(left, top, right, bottom, 180 - ARC_HALF_ANGLE_DEG, 2 * ARC_HALF_ANGLE_DEG, false, paintArc);
		}

		if (arcs.size() > 0) invalidate();
	}

	private ValueAnimator createSizeAnimation(View view, float from, float to, long duration, long delay) {
		ValueAnimator animator = ValueAnimator.ofFloat(from, to);
		animator.setInterpolator(new OvershootInterpolator(ANIM_OVERSHOOT_TENSION));
		animator.setDuration(duration);
		animator.setStartDelay(delay);
		animator.addUpdateListener(animation -> {
			float scale = (float) animation.getAnimatedValue();
			view.setScaleX(scale);
			view.setScaleY(scale);
		});
		return animator;
	}

	private AnimatorSet createSizeBumpAnimation(View view, float to, long duration, Runnable onBumpPeak) {
		long halfDur = duration / 2;
		AnimatorSet animatorSet = new AnimatorSet();

		ValueAnimator bumpStart = createSizeAnimation(view, 1f, to, halfDur, 0);
		bumpStart.setInterpolator(new LinearInterpolator());
		bumpStart.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				super.onAnimationEnd(animation);
				if (onBumpPeak != null) onBumpPeak.run();
			}
		});
		ValueAnimator bumpEnd = createSizeAnimation(view, to, 1f, halfDur, 0);

		animatorSet.playSequentially(bumpStart, bumpEnd);
		return animatorSet;
	}

	private AnimatorSet createIconSwitchAnimation(ImageView iconView, ImageView iconBg, @DrawableRes int iconRes, long duration) {
		long halfDur = duration / 2;
		AnimatorSet animatorSet = new AnimatorSet();
		AnimatorSet bump = createSizeBumpAnimation(iconBg, ICON_BG_BUMP_FACTOR, duration, null);
		ValueAnimator disappear = createSizeAnimation(iconView, 1f, 0f, halfDur, 0);
		disappear.setInterpolator(new AnticipateInterpolator(ANIM_OVERSHOOT_TENSION));
		disappear.addListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				icon.setImageResource(iconRes);
			}
		});
		ValueAnimator appear = createSizeAnimation(iconView, 0f, 1f, halfDur, 0);
		animatorSet.playTogether(disappear, bump);
		animatorSet.play(appear).after(disappear);
		return animatorSet;
	}

	public void spawnArc() {
		if (arcs.size() < MAX_NUM_ARCS) {
			arcs.add(new ArcObject(System.currentTimeMillis()));
			invalidate();
		}
	}

	private class ArcObject {
		private final long birthMs;

		private ArcObject(long birthMs) {this.birthMs = birthMs;}

		public long getBirthMs() {
			return birthMs;
		}

	}


	private class ArcRunnable implements Runnable {
		private boolean run = true;
		private final int newConsecutives;

		private ArcRunnable(int numConsecutives) {
			newConsecutives = Math.max(numConsecutives - 1, 0);
		}

		public void stop() {
			run = false;
		}

		@Override
		public void run() {
			if (!run) return;

			arcRunnable = new ArcRunnable(newConsecutives);
			long arcDelay = newConsecutives > 0 ? ARC_CONSEC_DELAY : ARC_PAUSE_DELAY;

			if (iconAnimatorSet != null && iconAnimatorSet.isRunning()) {
				spawnArc();
				arcHandler.postDelayed(arcRunnable, arcDelay);
			} else {
				iconAnimatorSet = createSizeBumpAnimation(iconBackground, ICON_BG_BUMP_FACTOR, ICON_ANIM_DURATION, () -> {
					spawnArc();
					arcHandler.postDelayed(arcRunnable, arcDelay);
				});
				iconAnimatorSet.start();
			}
		}

	}

}
