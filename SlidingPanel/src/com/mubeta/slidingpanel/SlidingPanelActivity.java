package com.mubeta.slidingpanel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.FrameLayout;

public class SlidingPanelActivity extends Activity implements OnGlobalLayoutListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      if (findViewById(android.R.id.home) != null) {
        ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
        View oldScreen = decorView.getChildAt(0);
        decorView.removeViewAt(0);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        SlidingPanel slidingPanel = (SlidingPanel) inflater.inflate(R.layout.screen_slider, null);
        ((ViewGroup) slidingPanel.findViewById(R.id.anterior)).addView(oldScreen);
        decorView.addView(slidingPanel, 0);
        setContentView(R.layout.anterior);
        findViewById(R.id.actuator).setVisibility(View.GONE);
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(this);
      }
      else
        setContentView(R.layout.activity_main);
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
    FrameLayout.LayoutParams parm = new FrameLayout.LayoutParams(-1, -1, 3);
    parm.setMargins(0, contentViewTop, 0, 0);
    posterior.setLayoutParams(parm);
    contentView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
	}

}
