package com.mubeta.slidingpanel;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;

public class SlidingPanelActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      ViewGroup decorView = (ViewGroup) getWindow().getDecorView();
      View oldScreen = decorView.getChildAt(0);
      decorView.removeViewAt(0);
      LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      SlidingPanel slidingPanel = (SlidingPanel) inflater.inflate(R.layout.screen_slider, null);
      ((ViewGroup) slidingPanel.findViewById(R.id.anterior)).addView(oldScreen);
      decorView.addView(slidingPanel, 0);
      setContentView(R.layout.anterior);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.activity_main, menu);
      return true;
    }

}
