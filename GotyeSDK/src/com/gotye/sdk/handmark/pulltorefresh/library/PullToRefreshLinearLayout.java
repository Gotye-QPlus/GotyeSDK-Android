package com.gotye.sdk.handmark.pulltorefresh.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.gotye.sdk.R;
import com.gotye.sdk.handmark.pulltorefresh.library.internal.LoadingLayout;

public class PullToRefreshLinearLayout extends PullToRefreshBase<LinearLayout> {

	private LoadingLayout mHeaderLoadingView;
	private LoadingLayout mFooterLoadingView;

	private FrameLayout mLvFooterLoadingFrame;

	private boolean mListViewExtrasEnabled;
	
	public PullToRefreshLinearLayout(Context context) {
		super(context);
	}

	public PullToRefreshLinearLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshLinearLayout(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshLinearLayout(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}
	
	@Override
	public com.gotye.sdk.handmark.pulltorefresh.library.PullToRefreshBase.Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected LinearLayout createRefreshableView(Context context,
			AttributeSet attrs) {
		LinearLayout lv = createLinearLayout(context, attrs);

		// Set it to this so it can be used in ListActivity/ListFragment
		lv.setId(android.R.id.list);
		return null;
	}
	
	protected LinearLayout createLinearLayout(Context context, AttributeSet attrs) {
		final LinearLayout lv;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			lv = new InternalLinearLayoutSDK9(context, attrs);
		} else {
			lv = new LinearLayout(context, attrs);
		}
		return lv;
	}

	@Override
	protected boolean isReadyForPullEnd() {
		return false;
	}

	@Override
	protected boolean isReadyForPullStart() {
		return true;
	}

	
	@Override
	protected void handleStyledAttributes(TypedArray a) {
		super.handleStyledAttributes(a);

		mListViewExtrasEnabled = a.getBoolean(R.styleable.PullToRefresh_ptrListViewExtrasEnabled, true);

		if (mListViewExtrasEnabled) {
			final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
					FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

			// Create Loading Views ready for use later
			FrameLayout frame = new FrameLayout(getContext());
			mHeaderLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_START, a);
			mHeaderLoadingView.setVisibility(View.GONE);
			frame.addView(mHeaderLoadingView, lp);
//			mRefreshableView.addHeaderView(frame, null, false);

			mLvFooterLoadingFrame = new FrameLayout(getContext());
			mFooterLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_END, a);
			mFooterLoadingView.setVisibility(View.GONE);
			mLvFooterLoadingFrame.addView(mFooterLoadingView, lp);

			/**
			 * If the value for Scrolling While Refreshing hasn't been
			 * explicitly set via XML, enable Scrolling While Refreshing.
			 */
			if (!a.hasValue(R.styleable.PullToRefresh_ptrScrollingWhileRefreshingEnabled)) {
				setScrollingWhileRefreshingEnabled(true);
			}
		}
	}
	
	@TargetApi(9)
	final class InternalLinearLayoutSDK9 extends LinearLayout {

		public InternalLinearLayoutSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
				int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
					scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(PullToRefreshLinearLayout.this, deltaX, scrollX, deltaY, scrollY, isTouchEvent);

			return returnValue;
		}
	}

	
}
