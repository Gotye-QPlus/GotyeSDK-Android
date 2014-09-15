/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.gotye.sdk.handmark.pulltorefresh.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

import com.gotye.sdk.R;
import com.gotye.sdk.handmark.pulltorefresh.library.internal.LoadingLayout;
import com.gotye.sdk.utils.GraphicsUtil;

public class GotyePullToRefreshScrollView extends PullToRefreshBase<ScrollView> {

	private LoadingLayout mHeaderLoadingView;
	private LinearLayout mContentLayout;
	private LinearLayout mContentListLayout;
	private ListView listView;
	private View footView;
	private OnTouchBottomListener liststener;
	
	public GotyePullToRefreshScrollView(Context context) {
		super(context);
	}

	public GotyePullToRefreshScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public GotyePullToRefreshScrollView(Context context, Mode mode) {
		super(context, mode);
	}

	public GotyePullToRefreshScrollView(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public final Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}
	
	@Override
	protected void onScrollChanged(int l, int t, int oldl, int oldt) {
//		super.onScrollChanged(l, t, oldl, oldt);
		
		if (getRefreshableView().getScrollY()
				+ getRefreshableView().getHeight() >= getRefreshableView()
				.getChildAt(0).getMeasuredHeight()) {

			Log.d(VIEW_LOG_TAG, "------滚动到最下方------");
			if(liststener != null){
				liststener.onTouchBottom();
			}
		} else {
		}
	}

	@Override
	protected ScrollView createRefreshableView(Context context, AttributeSet attrs) {
		ScrollView scrollView;
		if (VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			scrollView = new InternalScrollViewSDK9(context, attrs);
		} else {
			scrollView = new ScrollView(context, attrs){
				@Override
				protected void onScrollChanged(int l, int t, int oldl, int oldt) {
					super.onScrollChanged(l, t, oldl, oldt);
					GotyePullToRefreshScrollView.this.onScrollChanged(l, t, oldl, oldt);
				}
			};
		}
		LinearLayout linearLayout = mContentLayout = new LinearLayout(context);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		scrollView.setFillViewport(true);
		scrollView.addView(linearLayout, -1, -1);
		scrollView.setId(R.id.gotye_scrollview);
		
		return scrollView;
	}

	@Override
	protected boolean isReadyForPullStart() {
		return mRefreshableView.getScrollY() == 0;
	}

	@Override
	protected boolean isReadyForPullEnd() {
		View scrollViewChild = mRefreshableView.getChildAt(0);
		if (null != scrollViewChild) {
			return mRefreshableView.getScrollY() >= (scrollViewChild.getHeight() - getHeight());
		}
		return false;
	}
	
	@Override
	protected void handleStyledAttributes(TypedArray a) {
		super.handleStyledAttributes(a);
		
		final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT,
				FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);

		// Create Loading Views ready for use later
		FrameLayout frame = new FrameLayout(getContext());
		mHeaderLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_START, a);
		mHeaderLoadingView.setVisibility(View.GONE);
		frame.addView(mHeaderLoadingView, lp);
		((ViewGroup) mRefreshableView.getChildAt(0)).addView(frame, -1, -2);

		LinearLayout linearLayout = mContentListLayout = new LinearLayout(getContext());
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		mContentLayout.addView(linearLayout, -1, -1);
		
		
//		TextView tv = new TextView(getContext());
//		tv.setTextColor(Color.WHITE);
//		tv.setTextSize(20);
//		tv.setText("lasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxxlasjxxxxxxxxx");
//		((ViewGroup) mRefreshableView.getChildAt(0)).addView(tv, -1, -2);
//		mLvFooterLoadingFrame = new FrameLayout(getContext());
//		mFooterLoadingView = createLoadingLayout(getContext(), Mode.PULL_FROM_END, a);
//		mFooterLoadingView.setVisibility(View.GONE);
//		mLvFooterLoadingFrame.addView(mFooterLoadingView, lp);

		setScrollingWhileRefreshingEnabled(true);
	}
	
	@Override
	protected LoadingLayoutProxy createLoadingLayoutProxy(final boolean includeStart, final boolean includeEnd) {
		LoadingLayoutProxy proxy = super.createLoadingLayoutProxy(includeStart, includeEnd);

		final Mode mode = getMode();

		if (includeStart && mode.showHeaderLoadingLayout()) {
			proxy.addLayout(mHeaderLoadingView);
		}
//		if (includeEnd && mode.showFooterLoadingLayout()) {
//			proxy.addLayout(mFooterLoadingView);
//		}
		return proxy;
	}
	
	@Override
	protected void onRefreshing(final boolean doScroll) {

		super.onRefreshing(false);

		final LoadingLayout origLoadingView, listViewLoadingView;
		final int selection, scrollToY;

		switch (getCurrentMode()) {
			case PULL_FROM_START:
			default:
				origLoadingView = getHeaderLayout();
				listViewLoadingView = mHeaderLoadingView;
//				oppositeListViewLoadingView = mFooterLoadingView;
				selection = 0;
				scrollToY = getScrollY() + getHeaderSize();
				break;
		}

		// Hide our original Loading View
		origLoadingView.reset();
		origLoadingView.hideAllViews();

		// Make sure the opposite end is hidden too
//		oppositeListViewLoadingView.setVisibility(View.GONE);

		// Show the ListView Loading View and set it to refresh.
		listViewLoadingView.setVisibility(View.VISIBLE);
		listViewLoadingView.refreshing();

		if (doScroll) {
			// We need to disable the automatic visibility changes for now
			disableLoadingLayoutVisibilityChanges();

			// We scroll slightly so that the ListView's header/footer is at the
			// same Y position as our normal header/footer
			setHeaderScroll(scrollToY);

			// Make sure the ListView is scrolled to show the loading
			// header/footer

			// Smooth scroll as normal
			smoothScrollTo(0);
		}
	}

	@Override
	protected void onReset() {

		final LoadingLayout originalLoadingLayout, listViewLoadingLayout;
		final int scrollToHeight, selection;
		final boolean scrollLvToEdge;

		switch (getCurrentMode()) {
			case PULL_FROM_START:
			default:
				originalLoadingLayout = getHeaderLayout();
				listViewLoadingLayout = mHeaderLoadingView;
				scrollToHeight = -getHeaderSize();
				selection = 0;
//				scrollLvToEdge = Math.abs(mRefreshableView.getFirstVisiblePosition() - selection) <= 1;
				break;
		}

		// If the ListView header loading layout is showing, then we need to
		// flip so that the original one is showing instead
		if (listViewLoadingLayout.getVisibility() == View.VISIBLE) {

			// Set our Original View to Visible
			originalLoadingLayout.showInvisibleViews();

			// Hide the ListView Header/Footer
			listViewLoadingLayout.setVisibility(View.GONE);

			/**
			 * Scroll so the View is at the same Y as the ListView
			 * header/footer, but only scroll if: we've pulled to refresh, it's
			 * positioned correctly
			 */
			if (getState() != State.MANUAL_REFRESHING) {
//				mRefreshableView.setSelection(selection);
//				setHeaderScroll(scrollToHeight);
			}
		}

		// Finally, call up to super
		super.onReset();
//		if(listView != null){
//			listView.setAdapter(null);
//		}
//		try{
//			mContentLayout.removeViewAt(1);
//		}catch(Exception e){
//			
//		}
//		
	}


	@TargetApi(9)
	final class InternalScrollViewSDK9 extends ScrollView {

		@Override
		protected void onScrollChanged(int l, int t, int oldl, int oldt) {
			super.onScrollChanged(l, t, oldl, oldt);
			GotyePullToRefreshScrollView.this.onScrollChanged(l, t, oldl, oldt);
		}
		public InternalScrollViewSDK9(Context context, AttributeSet attrs) {
			super(context, attrs);
		}

		@Override
		protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX,
				int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent) {

			final boolean returnValue = super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX,
					scrollRangeY, maxOverScrollX, maxOverScrollY, isTouchEvent);

			// Does all of the hard work...
			OverscrollHelper.overScrollBy(GotyePullToRefreshScrollView.this, deltaX, scrollX, deltaY, scrollY,
					getScrollRange(), isTouchEvent);

			return returnValue;
		}

		/**
		 * Taken from the AOSP ScrollView source
		 */
		private int getScrollRange() {
			int scrollRange = 0;
			if (getChildCount() > 0) {
				View child = getChildAt(0);
				scrollRange = Math.max(0, child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
			}
			return scrollRange;
		}
	}
	
	
	public void addListView(ListView view){
		listView = view;
		
		LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		p.leftMargin = GraphicsUtil.dipToPixel(12);
		p.rightMargin = GraphicsUtil.dipToPixel(12);
		p.topMargin = GraphicsUtil.dipToPixel(15);
		p.bottomMargin = GraphicsUtil.dipToPixel(15);
		
		mContentListLayout.addView(view, p);
	}
	
	public void removeListView(){
		if(listView != null){
			mContentListLayout.removeAllViews();
			listView = null;
		}
		removeFootView();
	}
	
	public void addFootView(){
		if(footView != null){
			return;
		}
		footView = LayoutInflater.from(getContext()).inflate(
				R.layout.gotye_load_more, null);
		AbsListView.LayoutParams p = new AbsListView.LayoutParams(-1, 100);
		p.height = 100;
		footView.setLayoutParams(p);
		mContentLayout.addView(footView, -1, 100);
//		getRefreshableView().smoothScrollTo(0, getRefreshableView()
//				.getChildAt(0).getMeasuredHeight() + 100);
	}
	
	
	public void removeFootView(){
		if(footView == null){
			return;
		}
		mContentLayout.removeView(footView);
		footView = null;
	}
	
	public OnTouchBottomListener getListstener() {
		return liststener;
	}

	public void setListstener(OnTouchBottomListener liststener) {
		this.liststener = liststener;
	}


	public ListView getListView() {
		return listView;
	}


	public interface OnTouchBottomListener {
		public boolean onTouchBottom();
	}
}
