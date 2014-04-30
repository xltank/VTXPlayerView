package com.videotx.vtxplayerlib.comps;


import java.util.Timer;

import com.videotx.vtxplayerlib.vo.PlaylistInfo;
import com.videotx.vtxplayerlib.vo.VideoInfo;
import com.videotx.vtxplayerlib.R;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class VTXVideoView extends RelativeLayout {

	public VTXVideoView(Activity activity) {
		super(activity);
		init(activity);
	}

	public VTXVideoView(Activity activity, AttributeSet attrs) {
		super(activity, attrs);
		init(activity);
	}

	public VTXVideoView(Activity activity, AttributeSet attrs, int defStyle) {
		super(activity, attrs, defStyle);
		init(activity);
	}
	
	
	
	private final int AUTO_HIDE_DELAY_MILLIS = 3000;
	
	private final int FLOAT_PANEL_MIN_WIDTH = 40;
	
	private String curVideoInfoJSON ;
	private VideoInfo curVideoInfo; 
	
	private String curPlaylistInfoJSON;
	private PlaylistInfo curPlaylistInfo;
	
	private SurfaceView playerView;
	private MediaPlayer player;
	private AudioManager audioManager;
	
	private VideoInfo videoInfo;
	private String videoUrl;
	
	private Timer tikerTimer;

	private CustomRelativeLayout playerViewContainer;
	private LinearLayout controlBar;
	private ImageView snapshot;
	private View slideSeekHint;
	private TextView slideSeekTime;
	private View slideVolumeHint;
	private TextView slideVolume;
	private View slideBrightnessHint;
	private TextView slideBrightness;
	
	// TODO: a control-bar interface is needed for customized control-bar.
	private TextView timeLabel;
	private SeekBar seekBar;
	private PlayPauseButton playPauseButton;
	private VolumeButton volumeButton;
	private ImageButton fullScreenButton;
	private ImageButton fullScreenExitButton;
	private TextView videoTitle;
	private TextView videoDesc;
	
	private RelativeLayout menubar;
	private LinearLayout floatPanel;
	
	private int bufferPercent;
	private long duration;
	private int videoWidth;
	private int videoHeight;
	
	private int maxVolumeIndex;
	
	private WindowManager windowManager;
	
	//TODO: use Context instead of Activity
	private void init(Activity activity)
	{
		activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		activity.getWindow().setContentView(R.layout.player_view);
		activity.getWindow().setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

}
