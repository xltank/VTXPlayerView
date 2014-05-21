package com.videotx.testvideolib;


import com.videotx.vtxplayerlib.VTXViewVideoComp;
import com.videotx.vtxplayerlib.utils.GlobalData;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;

public class CompActivity extends Activity {

	public CompActivity() {
	}

	private Button btn;
	private RelativeLayout container;
	private VTXViewVideoComp videoView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_comp);
		
		btn = (Button) findViewById(R.id.viewVideo);
		container = (RelativeLayout) findViewById(R.id.container);
		
		videoView = new VTXViewVideoComp(this, this);
		ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		container.addView(videoView, lp);
//		videoView.playSingleVideo("94986174405279744", "137687822667612161");
		videoView.playPlaylist("94986174405279744", "94990205768957953");
	}
	
	
	@Override
	protected void onStart()
	{
		super.onStart();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onStart");
	}
	
	/**
	 * Called as part of the activity lifecycle when an activity is going into
     * the background, but has not (yet) been killed.
	 */
	@Override
	protected void onPause()
	{
		super.onPause();
		if(videoView != null)
		{
			videoView.pause();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onResume");
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onStop");
		if(videoView != null)
		{
			videoView.dispose();
			videoView = null;
		}
	}
	
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onDestroy");
	}
	
	@Override
	protected void onRestart()
	{
		super.onRestart();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onRestart");
	}
}
