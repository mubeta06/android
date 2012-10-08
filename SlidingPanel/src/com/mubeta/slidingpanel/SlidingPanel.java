package com.mubeta.slidingpanel;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;


/**
 * This widget makes possible building apps that mimic the facebook-style sliding menu.
 * 
 * @author gloo
 *
 */
public class SlidingPanel extends FrameLayout implements OnGlobalLayoutListener {
  
  private final int mActuatorId;

  private View mActuator;
  
  private Animation mSlideOpen;
  private Animation mSlideClose;
  
  private boolean mLocked;
  private boolean mAnimateOnClick;
  
  private boolean mOpen = false;
  private int mXOffset;
  private int mSlideSpan;
  
  public SlidingPanel(Context context) {
    this(context, null);
  }

  public SlidingPanel(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }
  
  public SlidingPanel(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SlidingPanel, defStyle, 0);
    int actuatorId = a.getResourceId(R.styleable.SlidingPanel_actuator, 0);
    if (actuatorId == 0) {
        throw new IllegalArgumentException("The actuator attribute is required and must refer "
                + "to a valid child.");
    }
    
    mActuatorId = actuatorId;
        
    mSlideSpan = (int) a.getDimension(R.styleable.SlidingPanel_slideSpan, 0.0f);
    mAnimateOnClick = a.getBoolean(R.styleable.SlidingPanel_animateOnClick, true);
    a.recycle();
    
    /* register as listener when global layout complete */
    getViewTreeObserver().addOnGlobalLayoutListener(this);
  }
  
  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    if (!(getChildCount() <= 2))
      throw new IllegalArgumentException("SlidingPanel can only contain 2 children.");
  }
  
  @Override
  public void onGlobalLayout() {
    mActuator = findViewById(mActuatorId);
    if (mActuator == null) {
        throw new IllegalArgumentException("The actuator attribute is must refer to an"
                + " existing child.");
    }
    mActuator.setOnClickListener(new PanelToggler());
    mXOffset = mSlideSpan != 0 ? mSlideSpan : getRight() - mActuator.getRight();
    initialiseAnimations();
    
    /* now lets unregister ourself */
    getViewTreeObserver().removeGlobalOnLayoutListener(this);
  }
  
  private void initialiseAnimations() {
    mSlideOpen = new TranslateAnimation(-mXOffset, 0, 0, 0);
    mSlideOpen.setDuration(750);
    mSlideOpen.setFillAfter(true);

    mSlideClose = new TranslateAnimation(mXOffset, 0, 0, 0);
    mSlideClose.setDuration(750);
    mSlideClose.setFillAfter(true);
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
  }

  /**
   * Closes the panel immediately.
   *
   * @see #toggle()
   * @see #open()
   * @see #animateClose()
   */
  public void close() {
    closePanel();
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
    if (getChildCount() == 2){
      FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(-1, -1, 3);
      parm.setMargins(0, 0, 0, 0);
      getChildAt(1).setLayoutParams(parm);
      getChildAt(1).startAnimation(mSlideClose);
      setOpen(false);
    }
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
    if (getChildCount() == 2){
      FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(-1, -1, 3);
      parm.setMargins(mXOffset, 0, -mXOffset, 0);
      getChildAt(1).setLayoutParams(parm);
      getChildAt(1).startAnimation(mSlideOpen);
      setOpen(true);
    }
  }
  
  private void closePanel() {
  }

  private void openPanel() {
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

}
