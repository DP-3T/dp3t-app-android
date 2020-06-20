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
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class LockableScrollView extends ScrollView {

	private Rect scrollPreventRect = null;

	public LockableScrollView(Context context) {
		super(context);
	}

	public LockableScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setScrollPreventRect(Rect scrollPreventRect) {
		this.scrollPreventRect = scrollPreventRect;
	}

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		switch (e.getAction()) {
			case MotionEvent.ACTION_DOWN:
				// if we can scroll pass the event to the superclass
				return isScrollabe(e) && super.onTouchEvent(e);
			default:
				return super.onTouchEvent(e);
		}
	}

	private boolean isScrollabe(MotionEvent e) {
		if (scrollPreventRect == null) return true;

		int x = (int) e.getX();
		int y = (int) e.getY() + getScrollY();

		return !scrollPreventRect.contains(x, y);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		// Don't do anything with intercepted touch events if
		// we are not scrollable
		return isScrollabe(e) && super.onInterceptTouchEvent(e);
	}

}