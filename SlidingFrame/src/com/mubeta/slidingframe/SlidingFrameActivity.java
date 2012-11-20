package com.mubeta.slidingframe;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.FrameLayout;

public class SlidingFrameActivity extends Activity implements OnGlobalLayoutListener, OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (findViewById(android.R.id.home) != null) {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        View oldScreen = decorView.getChildAt(0);
        decorView.removeViewAt(0);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SlidingFrame slidingFrame = (SlidingFrame) inflater.inflate(R.layout.screen_slider, null);
        ((ViewGroup) slidingFrame.findViewById(R.id.anterior)).addView(oldScreen);
        decorView.addView(slidingFrame, 0);
        setContentView(R.layout.anterior);
        findViewById(R.id.actuator).setVisibility(View.GONE);
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(this);
      }
      else
        setContentView(R.layout.activity_main);
      findViewById(R.id.posterior).setOnClickListener(this);
      findViewById(R.id.anterior).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
    }

	@Override
	public void onGlobalLayout() {
	  /* adjust posterior view's offset so content is not obscured by title bar */
	  View contentView = getWindow().findViewById(Window.ID_ANDROID_CONTENT);
    View posterior = getWindow().findViewById(R.id.posterior);
    int contentViewTop = contentView.getTop() + contentView.getPaddingTop();
    FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 
                                                                  ViewGroup.LayoutParams.MATCH_PARENT, 
                                                                  Gravity.LEFT);
    parm.setMargins(0, contentViewTop, 0, 0);
    posterior.setLayoutParams(parm);
    contentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.posterior) {
      ((SlidingFrame) findViewById(R.id.slider)).animateToggle();
    }
    else if (v.getId() == R.id.anterior)
      ((SlidingFrame) findViewById(R.id.slidingframe)).animateToggle();
  }

}
