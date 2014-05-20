package com.videotx.testvideolib;


import com.videotx.vtxplayerlib.VTXViewVideoComp;

import android.app.Activity;
import android.os.Bundle;
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
}
