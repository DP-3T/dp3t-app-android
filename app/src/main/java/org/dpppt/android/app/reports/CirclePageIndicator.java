/*
 * Copyright (c) 2020 Ubique Innovation AG <https://www.ubique.ch>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * SPDX-License-Identifier: MPL-2.0
 */

package org.dpppt.android.app.reports;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;
import org.dpppt.android.app.R;
import org.dpppt.android.app.util.UiUtils;

import static android.graphics.Paint.ANTI_ALIAS_FLAG;
import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

/**
 * IMPORTANT: based on "com.github.JakeWharton:ViewPagerIndicator:2.4.1" but custom modified/updated
 * <p>
 * Draws circles (one for each view). The current view position is filled and
 * others are only stroked.
 */
public class CirclePageIndicator extends View {
	private static final int INVALID_POINTER = -1;

	private float mRadius;
	private final Paint mPaintPageFill = new Paint(ANTI_ALIAS_FLAG);
	private final Paint mPaintStroke = new Paint(ANTI_ALIAS_FLAG);
	private final Paint mPaintFill = new Paint(ANTI_ALIAS_FLAG);
	private ViewPager2 mViewPager;
	private ViewPager2.OnPageChangeCallback mCallback = new CirclePageChangeCallback();
	private int mCurrentPage;
	private int mSnapPage;
	private float mPageOffset;
	private int mScrollState;
	private int mOrientation;
	private boolean mCentered;
	private boolean mSnap;

	private int mTouchSlop;
	private float mLastMotionX = -1;
	private int mActivePointerId = INVALID_POINTER;
	private boolean mIsDragging;


	public CirclePageIndicator(Context context) {
		this(context, null);
	}

	public CirclePageIndicator(Context context, AttributeSet attrs) {
		super(context, attrs);

		final Resources res = getResources();

		mCentered = true;
		mOrientation = 0;
		mPaintPageFill.setStyle(Paint.Style.FILL);
		mPaintPageFill.setColor(ContextCompat.getColor(context, R.color.dark_main_transparent));
		mPaintStroke.setStyle(Paint.Style.STROKE);
		mPaintStroke.setColor(ContextCompat.getColor(context, android.R.color.transparent));
		mPaintStroke.setStrokeWidth(UiUtils.dpToPx(res, 1));
		mPaintFill.setStyle(Paint.Style.FILL);
		mPaintFill.setColor(ContextCompat.getColor(context, R.color.white));
		mRadius = UiUtils.dpToPx(res, 4);
		mSnap = false;

		mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();
	}

	public void setCentered(boolean centered) {
		mCentered = centered;
		invalidate();
	}

	public boolean isCentered() {
		return mCentered;
	}

	public void setPageColor(int pageColor) {
		mPaintPageFill.setColor(pageColor);
		invalidate();
	}

	public int getPageColor() {
		return mPaintPageFill.getColor();
	}

	public void setFillColor(int fillColor) {
		mPaintFill.setColor(fillColor);
		invalidate();
	}

	public int getFillColor() {
		return mPaintFill.getColor();
	}

	public void setOrientation(int orientation) {
		switch (orientation) {
			case HORIZONTAL:
			case VERTICAL:
				mOrientation = orientation;
				requestLayout();
				break;

			default:
				throw new IllegalArgumentException("Orientation must be either HORIZONTAL or VERTICAL.");
		}
	}

	public int getOrientation() {
		return mOrientation;
	}

	public void setStrokeColor(int strokeColor) {
		mPaintStroke.setColor(strokeColor);
		invalidate();
	}

	public int getStrokeColor() {
		return mPaintStroke.getColor();
	}

	public void setStrokeWidth(float strokeWidth) {
		mPaintStroke.setStrokeWidth(strokeWidth);
		invalidate();
	}

	public float getStrokeWidth() {
		return mPaintStroke.getStrokeWidth();
	}

	public void setRadius(float radius) {
		mRadius = radius;
		invalidate();
	}

	public float getRadius() {
		return mRadius;
	}

	public void setSnap(boolean snap) {
		mSnap = snap;
		invalidate();
	}

	public boolean isSnap() {
		return mSnap;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		if (mViewPager == null) {
			return;
		}
		final int count = mViewPager.getAdapter().getItemCount();
		if (count == 0) {
			return;
		}

		if (mCurrentPage >= count) {
			setCurrentItem(count - 1);
			return;
		}

		int longSize;
		int longPaddingBefore;
		int longPaddingAfter;
		int shortPaddingBefore;
		if (mOrientation == HORIZONTAL) {
			longSize = getWidth();
			longPaddingBefore = getPaddingLeft();
			longPaddingAfter = getPaddingRight();
			shortPaddingBefore = getPaddingTop();
		} else {
			longSize = getHeight();
			longPaddingBefore = getPaddingTop();
			longPaddingAfter = getPaddingBottom();
			shortPaddingBefore = getPaddingLeft();
		}

		final float spreadDistance = mRadius * 4.5f;
		final float shortOffset = shortPaddingBefore + mRadius;
		float longOffset = longPaddingBefore + mRadius;
		if (mCentered) {
			longOffset += ((longSize - longPaddingBefore - longPaddingAfter) / 2.0f) - (((count - 1) * spreadDistance) / 2.0f + mRadius);
		}

		// case: indicators need more space than available
		final float neededWidth = longPaddingBefore + mRadius + (count - 1) * spreadDistance + mRadius + longPaddingAfter;
		boolean indicatorsExceedAvailableSpace = neededWidth > longSize;
		if (indicatorsExceedAvailableSpace) {
			longOffset = calculateShiftingOffset(neededWidth, longSize, longPaddingBefore + mRadius, spreadDistance);
		}

		float pageFillRadius = mRadius;
		if (mPaintStroke.getStrokeWidth() > 0) {
			pageFillRadius -= mPaintStroke.getStrokeWidth() / 2.0f;
		}

		//Draw stroked circles
		for (int iLoop = 0; iLoop < count; iLoop++) {
			float drawLong = longOffset + (iLoop * spreadDistance);
			PointF center = getPointXySwappedIfVerticalOrientation(drawLong, shortOffset, mOrientation);
			// Only paint fill if not completely transparent
			if (mPaintPageFill.getAlpha() > 0) {
				canvas.drawCircle(center.x, center.y, pageFillRadius, mPaintPageFill);
			}

			// Only paint stroke if a stroke width was non-zero
			if (pageFillRadius != mRadius) {
				canvas.drawCircle(center.x, center.y, mRadius, mPaintStroke);
			}
		}

		//Draw the filled circle according to the current scroll
		float cx = getXCoordinateOfFilledCircleCenter(longOffset, spreadDistance);
		PointF center = getPointXySwappedIfVerticalOrientation(cx, shortOffset, mOrientation);
		canvas.drawCircle(center.x, center.y, mRadius, mPaintFill);
	}

	private float calculateShiftingOffset(float neededWidth, float actualWidth, float initialOffset, float distanceBetweenTwoCircleCenterX) {
		float missingWidth = neededWidth - actualWidth;
		float currentX = getXCoordinateOfFilledCircleCenter(initialOffset, distanceBetweenTwoCircleCenterX);
		float progress = currentX / neededWidth;
		float relativeMissingWidth = missingWidth * progress;
		return initialOffset - relativeMissingWidth;
	}

	private PointF getPointXySwappedIfVerticalOrientation(float x, float y, int orientation) {
		return (orientation == HORIZONTAL) ? new PointF(x, y) : new PointF(y, x);
	}

	private float getXCoordinateOfFilledCircleCenter(float initialOffset, float distanceBetweenTwoCircleCenterX) {
		float currentX = (mSnap ? mSnapPage : mCurrentPage) * distanceBetweenTwoCircleCenterX;
		if (!mSnap) {
			currentX += mPageOffset * distanceBetweenTwoCircleCenterX;
		}
		return initialOffset + currentX;
	}

	public boolean onTouchEvent(MotionEvent ev) {
		if (super.onTouchEvent(ev)) {
			return true;
		}
		if ((mViewPager == null) || (mViewPager.getAdapter().getItemCount() == 0)) {
			return false;
		}

		final int action = ev.getActionMasked();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				mActivePointerId = ev.getPointerId(0);
				mLastMotionX = ev.getX();
				break;

			case MotionEvent.ACTION_MOVE: {
				final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
				final float x = ev.getX(activePointerIndex);
				final float deltaX = x - mLastMotionX;

				if (!mIsDragging) {
					if (Math.abs(deltaX) > mTouchSlop) {
						mIsDragging = true;
					}
				}

				if (mIsDragging) {
					mLastMotionX = x;
					if (mViewPager.isFakeDragging() || mViewPager.beginFakeDrag()) {
						mViewPager.fakeDragBy(deltaX);
					}
				}

				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				if (!mIsDragging) {
					final int count = mViewPager.getAdapter().getItemCount();
					final int width = getWidth();
					final float halfWidth = width / 2f;
					final float sixthWidth = width / 6f;

					if ((mCurrentPage > 0) && (ev.getX() < halfWidth - sixthWidth)) {
						if (action != MotionEvent.ACTION_CANCEL) {
							mViewPager.setCurrentItem(mCurrentPage - 1);
						}
						return true;
					} else if ((mCurrentPage < count - 1) && (ev.getX() > halfWidth + sixthWidth)) {
						if (action != MotionEvent.ACTION_CANCEL) {
							mViewPager.setCurrentItem(mCurrentPage + 1);
						}
						return true;
					}
				}

				mIsDragging = false;
				mActivePointerId = INVALID_POINTER;
				if (mViewPager.isFakeDragging()) mViewPager.endFakeDrag();
				break;

			case MotionEvent.ACTION_POINTER_DOWN: {
				final int index = ev.getActionIndex();
				mLastMotionX = ev.getX(index);
				mActivePointerId = ev.getPointerId(index);
				break;
			}

			case MotionEvent.ACTION_POINTER_UP:
				final int pointerIndex = ev.getActionIndex();
				final int pointerId = ev.getPointerId(pointerIndex);
				if (pointerId == mActivePointerId) {
					final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
					mActivePointerId = ev.getPointerId(newPointerIndex);
				}
				mLastMotionX = ev.getX(ev.findPointerIndex(mActivePointerId));
				break;
		}

		return true;
	}

	public void setViewPager(ViewPager2 view) {
		if (mViewPager == view) {
			return;
		}
		if (mViewPager != null) {
			mViewPager.unregisterOnPageChangeCallback(mCallback);
		}
		if (view.getAdapter() == null) {
			throw new IllegalStateException("ViewPager does not have adapter instance.");
		}
		mViewPager = view;
		mViewPager.registerOnPageChangeCallback(mCallback);
		invalidate();
	}

	public void setViewPager(ViewPager2 view, int initialPosition) {
		setViewPager(view);
		setCurrentItem(initialPosition);
	}

	public void setCurrentItem(int position) {
		if (mViewPager == null) {
			throw new IllegalStateException("ViewPager has not been bound.");
		}
		mViewPager.setCurrentItem(position);
		mCurrentPage = position;
		invalidate();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		if (mOrientation == HORIZONTAL) {
			setMeasuredDimension(measureLong(widthMeasureSpec), measureShort(heightMeasureSpec));
		} else {
			setMeasuredDimension(measureShort(widthMeasureSpec), measureLong(heightMeasureSpec));
		}
	}

	/**
	 * Determines the width of this view
	 *
	 * @param measureSpec A measureSpec packed into an int
	 * @return The width of the view, honoring constraints from measureSpec
	 */
	private int measureLong(int measureSpec) {
		int result;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if ((specMode == MeasureSpec.EXACTLY) || (mViewPager == null)) {
			//We were told how big to be
			result = specSize;
		} else {
			//Calculate the width according the views count
			final int count = mViewPager.getAdapter().getItemCount();
			result = (int) (getPaddingLeft() + getPaddingRight()
					+ (count * 2 * mRadius) + (count - 1) * mRadius + 1);
			//Respect AT_MOST value if that was what is called for by measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Determines the height of this view
	 *
	 * @param measureSpec A measureSpec packed into an int
	 * @return The height of the view, honoring constraints from measureSpec
	 */
	private int measureShort(int measureSpec) {
		int result;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			//We were told how big to be
			result = specSize;
		} else {
			//Measure the height
			result = (int) (2 * mRadius + getPaddingTop() + getPaddingBottom() + 1);
			//Respect AT_MOST value if that was what is called for by measureSpec
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	private class CirclePageChangeCallback extends ViewPager2.OnPageChangeCallback {

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			mCurrentPage = position;
			mPageOffset = positionOffset;
			invalidate();
		}

		@Override
		public void onPageSelected(int position) {
			if (mSnap || mScrollState == ViewPager.SCROLL_STATE_IDLE) {
				mCurrentPage = position;
				mSnapPage = position;
				invalidate();
			}
		}

		@Override
		public void onPageScrollStateChanged(int state) {
			mScrollState = state;
		}

	}

	@Override
	public void onRestoreInstanceState(Parcelable state) {
		SavedState savedState = (SavedState) state;
		super.onRestoreInstanceState(savedState.getSuperState());
		mCurrentPage = savedState.currentPage;
		mSnapPage = savedState.currentPage;
		requestLayout();
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		SavedState savedState = new SavedState(superState);
		savedState.currentPage = mCurrentPage;
		return savedState;
	}

	static class SavedState extends BaseSavedState {
		int currentPage;

		public SavedState(Parcelable superState) {
			super(superState);
		}

		private SavedState(Parcel in) {
			super(in);
			currentPage = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			dest.writeInt(currentPage);
		}

		@SuppressWarnings("UnusedDeclaration")
		public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
			@Override
			public SavedState createFromParcel(Parcel in) {
				return new SavedState(in);
			}

			@Override
			public SavedState[] newArray(int size) {
				return new SavedState[size];
			}
		};
	}
}
