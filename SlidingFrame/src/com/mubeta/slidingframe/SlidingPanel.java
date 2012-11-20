package com.mubeta.slidingframe;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

public class SlidingPanel extends SlidingFrame {
  
  public SlidingPanel(Context context) {
    this(context, null);
  }

  public SlidingPanel(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }
  
  public SlidingPanel(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
  }
  
  @Override
  protected void openPanel() {
    getChildAt(getChildCount() - 1).setVisibility(View.VISIBLE);
    FrameLayout.LayoutParams params = (LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams();
    params.setMargins(0, 0, 0, 0);
    requestLayout();
  }
  
  @Override
  protected void closePanel() {
    FrameLayout.LayoutParams params = (LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams();
    params.setMargins(-getSlideXOffset(), -getSlideYOffset(), getSlideXOffset(), getSlideYOffset());
    requestLayout();
  }
  
  @Override
  public void setOpen(boolean open) {
    super.setOpen(open);
    getChildAt(getChildCount() - 1).setVisibility(open ? View.VISIBLE : View.GONE);
    getChildAt(0).setVisibility(View.VISIBLE);
  }

}
