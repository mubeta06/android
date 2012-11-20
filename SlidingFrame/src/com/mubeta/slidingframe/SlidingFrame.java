package com.mubeta.slidingframe;

import com.mubeta.slidingframe.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout;


public class SlidingFrame extends FrameLayout implements OnGlobalLayoutListener, AnimationListener {
  
  private final int mActuatorId;
  private View mActuator;
  
  private Animation mSlideOpenAnimation;
  private Animation mSlideCloseAnimation;
  
  private boolean mLocked;
  private boolean mAnimateOnClick;
  
  private boolean mOpen = false;
  private int mSlideXOffset;
  private int mSlideYOffset;
  private int mSlideSpan;
  private int mSlideTime;
  
  public SlidingFrame(Context context) {
    this(context, null);
  }

  public SlidingFrame(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }
  
  public SlidingFrame(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingFrame, defStyle, 0);
    int actuatorId = a.getResourceId(R.styleable.SlidingFrame_actuator, 0);
    mActuatorId = actuatorId;
    mSlideSpan = (int) a.getDimension(R.styleable.SlidingFrame_slideSpan, 0.0f);
    mSlideTime = a.getInteger(R.styleable.SlidingFrame_slideTime, 500);
    mAnimateOnClick = a.getBoolean(R.styleable.SlidingFrame_animateOnClick, true);
    a.recycle();
    
    /* register as listener when global layout complete */
    getViewTreeObserver().addOnGlobalLayoutListener(this);
  }
  
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    if (getChildCount() > 2 || getChildCount() == 0)
      throw new IllegalArgumentException("SlidingFrame must contain at least 1 child and can only contain up to 2 children.");
  }
  
  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
  }
  
  @Override
  public void onGlobalLayout() {
    if (mActuatorId != 0) mActuator = findViewById(mActuatorId);
    if (mActuator != null) {
      mActuator.setOnClickListener(new PanelToggler());
    } 
    
    /* set the slide offsets */
    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams();
    if ((params.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.LEFT) {
      mSlideXOffset = mSlideSpan != 0 ? mSlideSpan : mActuator != null ? getRight() - mActuator.getRight() : getChildAt(getChildCount() - 1).getWidth();
      mSlideYOffset = 0;
    } else if ((params.gravity & Gravity.HORIZONTAL_GRAVITY_MASK) == Gravity.RIGHT) {
      mSlideXOffset = mSlideSpan != 0 ? mSlideSpan : mActuator != null ? getLeft() - mActuator.getLeft() : -getChildAt(getChildCount() - 1).getWidth();
      mSlideYOffset = 0;
    } else if ((params.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP) {
      mSlideXOffset = 0;
      mSlideYOffset = mSlideSpan != 0 ? mSlideSpan : mActuator != null ? getBottom() - mActuator.getBottom() : getChildAt(getChildCount() - 1).getHeight();
    } else if ((params.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.BOTTOM) {
      mSlideXOffset = 0;
      mSlideYOffset = mSlideSpan != 0 ? mSlideSpan : mActuator != null ? getTop() - mActuator.getTop() : -getChildAt(getChildCount() - 1).getHeight();
    }

    /*
     * These animations may seem a little backwards... reason being these animations
     * are based on the position of the view once it has been shifted into the end
     * animation position.
     */
    setSlideOpenAnimation(new TranslateAnimation(-mSlideXOffset, 0, -mSlideYOffset, 0));
    setSlideCloseAnimation(new TranslateAnimation(mSlideXOffset, 0, mSlideYOffset, 0));
    
    /* now lets unregister ourself */
    getViewTreeObserver().removeGlobalOnLayoutListener(this);
    
    /* and we start closed */
    setOpen(false);
  }

  /**
   * Toggles the drawer open and close. Takes effect immediately.
   *
   * @see #open()
   * @see #close()
   * @see #animateClose()
   * @see #animateOpen()
   * @see #animateToggle()
   */
  public void toggle() {
    if (!isOpened()) {
      open();
    } else {
      close();
    }
  }
  
  /**
   * Toggles the panel open and close with an animation.
   *
   * @see #open()
   * @see #close()
   * @see #animateClose()
   * @see #animateOpen()
   * @see #toggle()
   */
  public void animateToggle() {
    if (!isOpened()) {
      animateOpen();
    } else {
      animateClose();
    }
  }

  /**
   * Opens the panel immediately.
   *
   * @see #toggle()
   * @see #close()
   * @see #animateOpen()
   */
  public void open() {
    openPanel();
    setOpen(true);
  }

  /**
   * Closes the panel immediately.
   *
   * @see #toggle()
   * @see #open()Right
   * @see #animateClose()
   */
  public void close() {
    closePanel();
    setOpen(false);
  }

  /**
   * Closes the panel with an animation.
   *
   * @see #close()
   * @see #open()
   * @see #animateOpen()
   * @see #animateToggle()
   * @see #toggle()
   */
  public void animateClose() {
    closePanel();
    getChildAt(getChildCount() - 1).startAnimation(mSlideCloseAnimation);
  }

  /**
   * Opens the panel with an animation.
   *
   * @see #close()
   * @see #open()
   * @see #animateClose()
   * @see #animateToggle()
   * @see #toggle()
   */
  public void animateOpen() {
    openPanel();
    getChildAt(getChildCount() - 1).startAnimation(mSlideOpenAnimation);
  }
  
  protected void closePanel() {
    FrameLayout.LayoutParams params = (LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams();
    params.setMargins(0, 0, 0, 0);
    requestLayout();
  }

  protected void openPanel() {
    getChildAt(getChildCount() - 1).setVisibility(View.VISIBLE);
    FrameLayout.LayoutParams params = (LayoutParams) getChildAt(getChildCount() - 1).getLayoutParams();
    params.setMargins(mSlideXOffset, mSlideYOffset, -mSlideXOffset, -mSlideYOffset);
    requestLayout();
  }

  /**
   * Unlocks the SlidingPanel so that touch events are processed.
   *
   * @see #lock() 
   */
  public void unlock() {
      mLocked = false;
  }

  /**
   * Locks the SlidingPanel so that touch events are ignores.
   *
   * @see #unlock()
   */
  public void lock() {
    mLocked = true;
  }
  
  /**
   * Indicates whether the SlidingPanel is currently fully opened.
   *
   * @return True if the panel is opened, false otherwise.
   */
  public boolean isOpened() {
    return mOpen;
  }
  
  public void setOpen(boolean open) {
    mOpen = open;
    getChildAt(0).setVisibility(mOpen ? View.VISIBLE : View.GONE);
  }
  
  public void setSlideOpenAnimation(TranslateAnimation a) {
    mSlideOpenAnimation = a;
    mSlideOpenAnimation.setDuration(mSlideTime);
    mSlideOpenAnimation.setAnimationListener(this);
  }
  
  public void setSlideCloseAnimation(TranslateAnimation a) {
    mSlideCloseAnimation = a;
    mSlideCloseAnimation.setDuration(mSlideTime);
    mSlideCloseAnimation.setAnimationListener(this);
  }
  
  public int getSlideXOffset() {
    return mSlideXOffset;
  }

  public int getSlideYOffset() {
    return mSlideYOffset;
  }

  public int getSlideSpan() {
    return mSlideSpan;
  }

  public int getSlideTime() {
    return mSlideTime;
  }
  
  private class PanelToggler implements OnClickListener {
    public void onClick(View v) {
      if (mLocked) {
          return;
      }
      if (mAnimateOnClick) {
        animateToggle();
      } else {
        toggle();
      }
    }
  }

  @Override
  public void onAnimationEnd(Animation animation) {
    if (animation == mSlideCloseAnimation)
      setOpen(false);
  }

  @Override
  public void onAnimationRepeat(Animation animation) {
  }

  @Override
  public void onAnimationStart(Animation animation) {
    if (animation == mSlideOpenAnimation)
      setOpen(true);
  }
}
