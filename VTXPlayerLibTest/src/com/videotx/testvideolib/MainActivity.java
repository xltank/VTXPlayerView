package com.videotx.testvideolib;

import com.videotx.vtxplayerlib.VTXViewVideoActivity;
import com.videotx.vtxplayerlib.VTXViewVideoActivityOriginal;
import com.videotx.vtxplayerlib.constants.MessageConstant;
import com.videotx.vtxplayerlib.utils.GlobalData;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
		
//		playVideo();
		
		Button btn = (Button) findViewById(R.id.viewVideo);
		btn.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				playVideo(false);
			}
		});
		
		Button btn2 = (Button) findViewById(R.id.viewVideoOriginal);
		btn2.setOnClickListener(new View.OnClickListener() 
		{
			@Override
			public void onClick(View v) {
				playVideo(true);
			}
		});
	}
	
	private void playVideo(Boolean originally)
	{
		Intent intent;
		if(originally == false)
			intent = new Intent(this, VTXViewVideoActivity.class);
		else
			intent = new Intent(this, VTXViewVideoActivityOriginal.class);
		
//		intent.putExtra(MessageConstant.VIDEO_ID, "137687822667612161");
		intent.putExtra(MessageConstant.PLAYLIST_ID, "94990205768957953");
		intent.putExtra(MessageConstant.PUBLISHER_ID, "94986174405279744");
		startActivity(intent);
	}
	
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container,
					false);
			return rootView;
		}
	}

}
