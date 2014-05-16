package com.videotx.vtxplayerlib;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnBufferingUpdateListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnInfoListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener;
import tv.danmaku.ijk.media.player.IMediaPlayer.OnVideoSizeChangedListener;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.videotx.vtxplayerlib.comps.CustomRelativeLayout;
import com.videotx.vtxplayerlib.comps.PlayPauseButton;
import com.videotx.vtxplayerlib.comps.PlayPauseButton.OnPlayButtonStateChangeListener;
import com.videotx.vtxplayerlib.comps.PlaylistArrayAdaptor;
import com.videotx.vtxplayerlib.comps.VolumeButton;
import com.videotx.vtxplayerlib.constants.APIConstant;
import com.videotx.vtxplayerlib.constants.MessageConstant;
import com.videotx.vtxplayerlib.control.Facade;
import com.videotx.vtxplayerlib.control.ImageManager;
import com.videotx.vtxplayerlib.utils.CommonUtil;
import com.videotx.vtxplayerlib.utils.GlobalData;
import com.videotx.vtxplayerlib.utils.VideoUtil;
import com.videotx.vtxplayerlib.vo.PlaylistInfo;
import com.videotx.vtxplayerlib.vo.ThumbnailsVTX;
import com.videotx.vtxplayerlib.vo.VideoInfo;


public class VTXViewVideoActivity extends Activity 
	implements Callback, OnPreparedListener, OnBufferingUpdateListener, OnVideoSizeChangedListener,
	OnCompletionListener, OnSeekCompleteListener, OnErrorListener, OnInfoListener{

//	private final boolean AUTO_HIDE = true;
	private final int AUTO_HIDE_DELAY_MILLIS = 3000;
	
	private String curVideoInfoJSON ;
	private VideoInfo curVideoInfo; 
	
	private String curPlaylistInfoJSON;
	private PlaylistInfo curPlaylistInfo;
	
	private SurfaceView playerView;
	private IjkMediaPlayer player;
	private AudioManager audioManager;
	
//	private VideoInfo videoInfo;
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
	
	private LinearLayout menubar;
	private Button playlistButton;
//	private Button menuButton;
	
	private LinearLayout playlistPanel;
	private ListView playlistView;
	private PlaylistArrayAdaptor playlistAdaptor;
	
	private Boolean isFullscreenState = false;
	
	private int bufferPercent;
	private long duration;
	private int videoWidth;
	private int videoHeight;
	
	private int maxVolumeIndex;
	
	private int playlistIndex=0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		setContentView(R.layout.activity_view_video);
		setVolumeControlStream(AudioManager.STREAM_MUSIC);
//		setupActionBar();
		
		Intent intent = getIntent();
		String videoId = intent.getStringExtra(MessageConstant.VIDEO_ID);
		String playlistId = intent.getStringExtra(MessageConstant.PLAYLIST_ID);
		String publisherId = intent.getStringExtra(MessageConstant.PUBLISHER_ID);
		if(videoId != null && publisherId != null)
		{
			getVideoInfo(videoId, publisherId);
		}
		else if(playlistId != null && publisherId != null)
		{
			getPlaylistInfo(playlistId, publisherId);
		}
		
		initComps(); 
	}
	
	private void initComps()
	{
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		maxVolumeIndex = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		
		playerViewContainer = (CustomRelativeLayout) findViewById(R.id.playerViewContainer);
		playerViewContainer.setOnSizeChangedListener(onPlayerViewContainerSizeChcanged);
		playerViewContainer.setOnTouchListener(onPlayerViewTouchListener);
		playerViewContainer.setOnClickListener(onPlayerViewClickListener);
		
		slideSeekHint = findViewById(R.id.slide_seek_hint);
		slideSeekTime = (TextView) findViewById(R.id.slide_seek_time);
		slideVolumeHint = findViewById(R.id.slide_volume_hint);
		slideVolume = (TextView) findViewById(R.id.slide_volume);
		slideBrightnessHint = findViewById(R.id.slide_brightness_hint);
		slideBrightness = (TextView) findViewById(R.id.slide_brightness);
		
		controlBar = (LinearLayout) findViewById(R.id.controlBar);
		
		timeLabel = (TextView) findViewById(R.id.time);
		
		seekBar = (SeekBar) findViewById(R.id.seekBar);
		seekBar.setOnSeekBarChangeListener(onSeekBarChange);
		
		playPauseButton = (PlayPauseButton) findViewById(R.id.play_pause_button);
		playPauseButton.setOnPlayButtonStateChangeListener(onPlayButtonStateChangeListener);
		
		volumeButton = (VolumeButton) findViewById(R.id.volume_button);
		volumeButton.setOnClickListener(onVolumeButtonclick);
		
		fullScreenButton = (ImageButton) findViewById(R.id.fullscreen_button);
		fullScreenButton.setOnClickListener(onFullScreenButtonClick);
		
		fullScreenExitButton = (ImageButton) findViewById(R.id.fullscreen_exit_button);
		fullScreenExitButton.setOnClickListener(onFullScreenExitButtonClick);
		
		videoTitle = (TextView) findViewById(R.id.video_title);
		videoDesc = (TextView) findViewById(R.id.video_desc);
		
		menubar = (LinearLayout) findViewById(R.id.menuBar);
//		menubar.setOnClickListener(onMenubarClick);
		
		playlistButton = (Button) findViewById(R.id.playlist_button);
		playlistButton.setOnClickListener(onPlaylistButtonClick);
//		menuButton = (Button) findViewById(R.id.menu_button);
		
		playlistPanel = (LinearLayout) findViewById(R.id.playlist_panel);
		playlistView = (ListView) findViewById(R.id.playlist_view);
	}
	
	
	// ########## video
	private void getVideoInfo(String videoId, String publisherId)
	{
		Facade.ins().getVideoInfo(
				onVideoInfoHandler, 
				videoId, 
				publisherId, 
				APIConstant.RESULT_FORMAT_JSON, 
//				APIConstant.VIDEO_TYPE_M3U8
				APIConstant.VIDEO_TYPE_MP4
				);
	}
	final Handler onVideoInfoHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			
			curVideoInfoJSON = msg.getData().getString("result");
			curVideoInfo = VideoUtil.parseVideoInfoJSON(curVideoInfoJSON);
			prepareVideoInfo();
		}
	};
	
	private void prepareVideoInfo()
	{
		if(curVideoInfo==null || curVideoInfo.renditions.size() == 0)
		{
			Log.e(GlobalData.DEBUG_TAG, "ERROR: video info error or no playable rendition");
			return ;
		}

		videoUrl = curVideoInfo.renditions.get(0).getUrl();
//		Log.w(GlobalData.DEBUG_TAG, "videoURL: "+videoUrl);
		duration = curVideoInfo.renditions.get(0).getDuration();
		videoWidth = curVideoInfo.renditions.get(0).getWidth();
		videoHeight = curVideoInfo.renditions.get(0).getHeight();
		
		videoTitle.setText(curVideoInfo.title);
		videoDesc.setText("video URL " + videoUrl);
		
		if(player != null)
		{
			player.reset(); // without reset(), last video is still there...
			player.release();// without release() after reset(), player does not work.
			player = null;
		}
		// should we remove all listeners? 
		//or we need not to do that because they are all in the same activity?
		if(player == null)
		{
			initPlayer();
			autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS);
			if(isFullscreenState == true)
				autoHide(menubar, AUTO_HIDE_DELAY_MILLIS);
//			autoHide(playlistPanel, AUTO_HIDE_DELAY_MILLIS);
		}
		
		try {
			player.setDataSource(videoUrl);
			player.prepareAsync();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		setSnapshot();
	}
	
	// ########## playlist
	private void getPlaylistInfo(String playlistId, String publisherId)
	{
		Facade.ins().getPlaylistInfo(
				onPlaylistInfoHandler, 
				playlistId, 
				publisherId, 
				APIConstant.RESULT_FORMAT_JSON, 
				APIConstant.VIDEO_TYPE_MP4);
	}
	final Handler onPlaylistInfoHandler = new Handler()
	{
		@Override
		public void handleMessage(Message msg){
			super.handleMessage(msg);
			
			curPlaylistInfoJSON = msg.getData().getString("result");
			curPlaylistInfo = VideoUtil.parsePlaylistInfoJSON(curPlaylistInfoJSON);
			if(curPlaylistInfo==null || curPlaylistInfo.videos.size() == 0)
			{
				Log.e(GlobalData.DEBUG_TAG, "ERROR: video info error or no playable rendition");
				return ;
			}
			preparePlaylist();
		}
	};
	private void preparePlaylist()
	{
		playlistAdaptor = new PlaylistArrayAdaptor(this, R.layout.item_playlist, curPlaylistInfo.videos);
		playlistView.setAdapter(playlistAdaptor);
		playlistView.setOnItemClickListener(onPlaylistItemClick);
		
		gotoPlaylistIndex(0);
	}
	
	private void gotoPlaylistIndex(int index)
	{
//		playlistView.setSelection(index);
//		playlistView.setItemChecked(index, true);
//		View itemView = playlistView.getSelectedView();
//		Log.w(GlobalData.DEBUG_TAG, ""+playlistView.getSelectedItem()+", "+playlistView.getSelectedItemId()+", "+playlistView.getSelectedItemPosition()+", "+playlistView.getSelectedView());
//		Log.w(GlobalData.DEBUG_TAG, ""+playlistView.getCheckedItemCount()+", "+playlistView.getCheckedItemPosition()+", "+playlistView.getCheckedItemPositions()+", "+playlistView.getCheckedItemIds());
		
//		playlistView.smoothScrollToPosition(0);
		curVideoInfo = (VideoInfo) curPlaylistInfo.videos.get(index);
		prepareVideoInfo();
		
		for(int i=0; i<curPlaylistInfo.videos.size(); i++) 
		{
			curPlaylistInfo.videos.get(i).selected = false;
		}
		curPlaylistInfo.videos.get(index).selected = true;
		playlistAdaptor.notifyDataSetChanged();
	}
	
	final OnItemClickListener onPlaylistItemClick = new OnItemClickListener() 
	{
		public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    	{
			if(position == playlistView.getSelectedItemPosition())
				return ;
			
//			view.setSelected(true);
//			view.setBackgroundResource(R.color.highlight_bg_item_playlist);
    		gotoPlaylistIndex(position);
    		// TODO: highlight selected item
    	}
	};
	
	
	// ##########
	private void initPlayer()
	{
		playerView = (SurfaceView) findViewById(R.id.playerView);
		playerView.getHolder().addCallback(this);
		
		player = new IjkMediaPlayer();
		player.setOnPreparedListener(this);
		player.setOnBufferingUpdateListener(this);
		player.setOnVideoSizeChangedListener(this);
		player.setOnCompletionListener(this);
		player.setOnSeekCompleteListener(this);
		player.setOnErrorListener(this);
		player.setOnInfoListener(this);
		
		player.setScreenOnWhilePlaying(true);
		
		Intent i = new Intent("com.android.music.musicservicecommand");
        i.putExtra("command", "pause");
        sendBroadcast(i);
	}
	
	private void setSnapshot()
	{
		snapshot = (ImageView) findViewById(R.id.snapshot);
		String snapshotUrl ="";
		Collections.sort(curVideoInfo.thumbnails);
		int containerW = playerViewContainer.getWidth();
		int containerH = playerViewContainer.getHeight();
		for(ThumbnailsVTX t : curVideoInfo.thumbnails)
		{
			snapshotUrl = t.url;
			if(t.width > containerW || t.height > containerH)
				break;
		}
		ImageManager.ins().loadImage(snapshotUrl, snapshot, 1);
	}
	
	
	////////////// player event listeners
	
	public void onPrepared(IMediaPlayer mp) {
		if(duration <= 0)
			duration = player.getDuration();
		if(videoWidth <= 0)
			videoWidth = player.getVideoWidth();
		if(videoHeight <= 0)
			videoHeight = player.getVideoHeight();
		layoutVideo();
		if(playerView != null)
			startPlaying();
	}
	
	public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sarNum, int sarDen) {
	}
	
	public void onBufferingUpdate(IMediaPlayer mp, int percent) {
		bufferPercent = percent;
	}

	public boolean onInfo(IMediaPlayer mp, int what, int extra) {
//		switch (what) {
//			case MediaPlayer.MEDIA_INFO_UNKNOWN :
//				break;
//			case MediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING :
//				break;
//			case MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START :
//				Log.w(GlobalData.DEBUG_TAG, "onInfo : MEDIA_INFO_VIDEO_RENDERING_START");
//				break;
//			case MediaPlayer.MEDIA_INFO_BUFFERING_START :
//				Log.w(GlobalData.DEBUG_TAG, "onInfo : MEDIA_INFO_BUFFERING_START");
//				break;
//			case MediaPlayer.MEDIA_INFO_BUFFERING_END :
//				Log.w(GlobalData.DEBUG_TAG, "onInfo : MEDIA_INFO_BUFFERING_END");
//				break;
//			case MediaPlayer.MEDIA_INFO_BAD_INTERLEAVING :
//				break;
//			case MediaPlayer.MEDIA_INFO_NOT_SEEKABLE :
//				break;
//			case MediaPlayer.MEDIA_INFO_METADATA_UPDATE :
//				Log.w(GlobalData.DEBUG_TAG, "onInfo : MEDIA_INFO_METADATA_UPDATE");
//				break;
//			case MediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE :
//				break;
//			case MediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT :
//				break;
//		}
		return false;
	}

	public boolean onError(IMediaPlayer mp, int what, int extra) {
		Log.w(GlobalData.DEBUG_TAG, "onError : " + what + " , "  + extra);
		return false;
	}

	public void onSeekComplete(IMediaPlayer mp) {
		Log.w(GlobalData.DEBUG_TAG, "onSeekComplete : " + mp.getCurrentPosition());
	}

	public void onCompletion(IMediaPlayer mp) 
	{
		Log.w(GlobalData.DEBUG_TAG, "onComplete ");
		if(curPlaylistInfo == null) // video
			playPauseButton.setPlayState(false);
		else // playlist
			if(playlistIndex < curPlaylistInfo.videos.size()-1)
			{
				gotoPlaylistIndex(playlistIndex+1);
			}else
			{
				// TODO: do sth ???
			}
		
		//TODO: loop when it's video, but not playlist.
	}


	////////// SurfaceView implements
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//		Log.w(GlobalData.DEBUG_TAG, "surface Changed");
	}
	// when activity is paused, surface is destroyed.
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.w(GlobalData.DEBUG_TAG, "surface Destroyed");
//		playerView.setOnTouchListener(null);
//		playerView.setOnClickListener(null);
	}
	// when activity is started/restarted, surface is created.
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.w(GlobalData.DEBUG_TAG, "surface Created");
//		playerView.setOnTouchListener(onPlayerViewTouchListener);
//		playerView.setOnClickListener(onPlayerViewClickListener);
		startPlaying();
	}

	private void startPlaying()
	{
		Log.w(GlobalData.DEBUG_TAG, "start playing");
		player.setDisplay(playerView.getHolder());
		player.start();
		snapshot.setVisibility(View.INVISIBLE);// 
		
		playPauseButton.setPlayState(true);

		startTimer();
	}
	
	private void startTimer()
	{
		tikerTimer = new Timer();
		tikerTimer.scheduleAtFixedRate(new TimerTask() 
		{
			@Override
			public void run() {
				if(player != null && player.isPlaying())
				{
					timeLabel.post(new Runnable() 
					{	
						@Override
						public void run() {
							playProgressUpdate();
						}
					});
				}
			}
		}, 0, 200);
	}
	
	private void playProgressUpdate()
	{
		if(player == null)
			return ;
			
		if(timeLabel != null)
			timeLabel.setText(player.getCurrentPosition()+"");
		
		if(seekBar != null)
		{
			seekBar.setMax((int)player.getDuration());
			seekBar.setProgress((int)player.getCurrentPosition());
			seekBar.setSecondaryProgress((int) (bufferPercent * 0.01 * duration));
			
			String time = CommonUtil.formatDuration(player.getCurrentPosition());
			String duration = CommonUtil.formatDuration(player.getDuration());
			timeLabel.setText(time+"/"+duration);
		}
	}
	
	
	final CustomRelativeLayout.OnSizeChangedListener onPlayerViewContainerSizeChcanged = new CustomRelativeLayout.OnSizeChangedListener() 
	{
		private int oldW;
		private int oldH;
		@Override
		public void onEvent() 
		{
			int w = playerViewContainer.getWidth();
			int h = playerViewContainer.getHeight();
			if(oldW != w || oldH != h)
			{
				oldW = w;
				oldH = h;
				layoutVideo();
				
				slideSeekHint.setX((w-slideSeekHint.getWidth())/2);
				slideSeekHint.setY((h-slideSeekHint.getHeight())/2);
				
				slideVolumeHint.setX((w-slideVolumeHint.getWidth())/2);
				slideVolumeHint.setY((h-slideVolumeHint.getHeight())/2);
				
				slideBrightnessHint.setX((w-slideBrightnessHint.getWidth())/2);
				slideBrightnessHint.setY((h-slideBrightnessHint.getHeight())/2);
			}
		}
	};
	
	// TODO: origin, zoom, scale, stretch; or 50%, 75%, 100%, stretch;
	private void layoutVideo()
	{
		if(player == null || playerView == null)
			return ;
		
		if(videoWidth == 0 || videoHeight == 0)
			return ;
		
		View playerViewParent = (View) playerView.getParent();
		int containerW = playerViewParent.getWidth();
		int containerH = playerViewParent.getHeight();
		if(containerW == 0 || containerH == 0)
			return ;
		
		Point newSize = CommonUtil.getSuitableSize(videoWidth, videoHeight, containerW, containerH);
//		Log.w(GlobalData.DEBUG_TAG, newSize.x + "," + newSize.y);
		// !!! when setLayoutParams is called rapidly, video will not show.
		playerView.setLayoutParams(new RelativeLayout.LayoutParams(newSize.x, newSize.y));
		
		playerView.setX((containerW - newSize.x)/2);
		playerView.setY((containerH - newSize.y)/2);
	}
	
	
	//////////////// play control
	
	private OnPlayButtonStateChangeListener onPlayButtonStateChangeListener = new OnPlayButtonStateChangeListener() 
	{
		@Override
		public void onClick(View v, String state) 
		{
			if(state == "play")
			{
				player.start();
			}
			else if(state == "pause")
			{
				player.pause();
			}
		}
	};
	
	
	private OnClickListener onVolumeButtonclick = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			
		}
	};
	
	
	private OnClickListener onFullScreenButtonClick = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			toFullScreen();
		}
	};
	
	private void toFullScreen()
	{
		isFullscreenState = true;
		player.pause();
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		fullScreenButton.setVisibility(View.INVISIBLE);
		fullScreenExitButton.setVisibility(View.VISIBLE);
		findViewById(R.id.gap).setVisibility(View.GONE);
		findViewById(R.id.video_info).setVisibility(View.GONE);
		
		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs); 

		layoutVideo();
		player.start();
	}
	
	private OnClickListener onFullScreenExitButtonClick = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
			exitFullScreen();
		}
	};
	
	private void exitFullScreen()
	{
		isFullscreenState = false;
		player.pause();
		
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		fullScreenButton.setVisibility(View.VISIBLE);
		fullScreenExitButton.setVisibility(View.INVISIBLE);
		findViewById(R.id.gap).setVisibility(View.VISIBLE);
		findViewById(R.id.video_info).setVisibility(View.VISIBLE);

		WindowManager.LayoutParams attrs = getWindow().getAttributes();
		attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
		getWindow().setAttributes(attrs);
		
		layoutVideo();
		
		playlistPanel.setVisibility(View.INVISIBLE);
		
		player.start();
	}
	
	// TODO: test MOVEMENT_THRESHOLD
	private final int MOVEMENT_THRESHOLD = 10; 
	private final int LEFT = 1;
	private final int RIGHT = 2;
	private final int TOP = 3;
	private final int BOTTOM = 4;
	final OnTouchListener onPlayerViewTouchListener = new OnTouchListener() 
	{	
		int direction=0; // left=1, right=2, top=3, bottom=4;
		int area=0; // left part=1, right part=2;
		float value=0; // xValue or yValue;
		float firstDelta=0; // for movement threshold in pixel to prevent click from touch. 
		float lastX=0;
		float lastY=0;
		float timeDelta=0;
		float timeTarget=0;
		float volumeDelta=0;
		float volumeTarget=0;
		float brightnessDelta=0;
		float brightnessTarget=0;
		
		@Override
		public boolean onTouch(View v, MotionEvent event) 
		{ 
			int action = event.getActionMasked();
			float curX = event.getX();
			float curY = event.getY();
			switch(action) {
		        case (MotionEvent.ACTION_DOWN) :
//					Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_DOWN");
	        		area = event.getX()*2 < playerViewContainer.getWidth() ? 1 : 2;
		        	lastX = curX;
		        	lastY = curY;
		            return false;
		        case (MotionEvent.ACTION_MOVE) :
//		        	Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_MOVE");
		        	// getHistoricalX()/getHistoricalY() is not perfect, for sometime getHistorySize() == 0.
		        	float xValue = curX - lastX;
		        	float yValue = curY - lastY;
		        	if(xValue == 0 && yValue == 0) // first move event.
		        		return true;
		        	if(firstDelta == 0)
	        		{
		        		firstDelta = Math.max(xValue, yValue);
		        		if(firstDelta < MOVEMENT_THRESHOLD)
		        			return true;
	        		}
		        	if(direction == 0)
		        	{
			        	float diff = Math.abs(xValue) - Math.abs(yValue);
			        	if(diff >= 0)
			        		direction = xValue > 0 ? RIGHT : LEFT;
		        		else
		        			direction = yValue > 0 ? BOTTOM : TOP;
		        	}
		        	if(direction == LEFT || direction == RIGHT)
						value = xValue;
		        	else // if(direction == TOP || direction == BOTTOM)
						value = -yValue;
//		        	Log.w(GlobalData.DEBUG_TAG, "direction: " + direction + "," + value + "," + (curX - lastX));
		        	
		        	if(direction == LEFT || direction == RIGHT) // progress
		        	{
		        		timeDelta += value * 50; // time delta = 1/20 * xValue.
		        		timeTarget = player.getCurrentPosition() + timeDelta;
		        		if(timeTarget < 0 || timeTarget > duration)
		        			return true;
		        		slideSeekTime.setText(CommonUtil.formatDuration((long)timeTarget)+"/"+CommonUtil.formatDuration((long)duration));
		        		autoHide(slideSeekHint, 1000);
		        	}
		        	else if(area == 1)  // brightness
		        	{
		        		brightnessDelta += value * 0.1;
		        		int curBrightness = 0;
						try {
							curBrightness = System.getInt(getContentResolver(), System.SCREEN_BRIGHTNESS);
//							LayoutParams lp = getWindow().getAttributes();
//							Log.w(GlobalData.DEBUG_TAG, "curBrightness " + curBrightness + ", " + lp.screenBrightness);
						}
						catch (SettingNotFoundException e) {
							e.printStackTrace();
						}
		        		brightnessTarget = curBrightness + brightnessDelta;
		        		if(brightnessTarget < 0 || brightnessTarget > 255)
		        			return true;
//		        		Log.w(GlobalData.DEBUG_TAG, brightnessDelta + ", " + curBrightness + ", " + brightnessTarget);
		        		LayoutParams lp = getWindow().getAttributes();
		        		lp.screenBrightness = brightnessTarget/255;
		                getWindow().setAttributes(lp);
		                System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, (int)brightnessTarget);
		                slideBrightness.setText((int)brightnessTarget*100/255 + "%");
		                autoHide(slideBrightnessHint, 1000);
		        	}
		        	else if(area == 2)  // volume
		        	{
		        		// Android does not provide explicit volume control.
		        		int curVolumeIndex = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		        		volumeDelta += value * 0.005; // volume grade index.
		        		volumeTarget = Math.min(Math.max(curVolumeIndex + volumeDelta, 0), maxVolumeIndex);
		        		audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int)volumeTarget, AudioManager.FLAG_PLAY_SOUND);
		        		slideVolume.setText((int)(volumeTarget/maxVolumeIndex*100)+"%");
		        		autoHide(slideVolumeHint, 1000);
		        	}
		        	
		        	lastX = curX;
		        	lastY = curY;
		            return true;
		        case (MotionEvent.ACTION_UP) :
//		        	Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_UP");
		        	firstDelta = 0;
		        	if(direction == LEFT || direction == RIGHT)
		        		player.seekTo((int)(player.getCurrentPosition() + timeDelta));
//		        	else if(area == 1)
		        	else if(area == 2)
		        	{
		        		volumeButton.setVolume((int)(volumeTarget/maxVolumeIndex*100));
		        	}
		        	direction = 0;
		        	timeDelta = 0;
		        	volumeDelta = 0;
		        	brightnessDelta = 0;
		            return false;
		        case (MotionEvent.ACTION_OUTSIDE) :
//		        	Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_OUTSIDE");
		        	firstDelta = 0;
		        	direction = 0;
		        	timeDelta = 0;
		        	volumeDelta = 0;
		        	brightnessDelta = 0;
		        	return true;      
		    }      
			
			return false;
		}
	};
	
	
	final OnClickListener onPlayerViewClickListener = new OnClickListener() 
	{
		@Override
		public void onClick(View v) 
		{
//			Log.w(GlobalData.DEBUG_TAG, "On PlayerView Click");
//			if(controlBar.getVisibility() == View.VISIBLE)
//				controlBar.setVisibility(View.INVISIBLE);
//			else
				autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS);
			
//			if(menubar.getVisibility() == View.VISIBLE)
//				menubar.setVisibility(View.INVISIBLE);
//			else
			if(isFullscreenState == true)
				autoHide(menubar, AUTO_HIDE_DELAY_MILLIS);
			
			playlistPanel.setVisibility(View.INVISIBLE);
		}
	};
	private HashMap<View, Runnable> autoHideHash = new HashMap<View, Runnable>();
	private Handler mHideHandler = new Handler();
	private void autoHide(View v, int duration)
	{
		Log.w(GlobalData.DEBUG_TAG, "autohide : " + v.getTag());
		final View view = v;
		Runnable mHideRunnable = new Runnable() {
			@Override
			public void run() {
				Log.w(GlobalData.DEBUG_TAG, "hid : " + view.getTag());
				view.setVisibility(View.INVISIBLE);
			}
		};
		
		Runnable lastRunnable = autoHideHash.get(v);
		if(lastRunnable != null)
		{
			Log.w(GlobalData.DEBUG_TAG, "autohide : exists " + v.getTag());
			autoHideHash.remove(v);
			mHideHandler.removeCallbacks(lastRunnable);
		}
		if(v.getVisibility() == View.VISIBLE)
		{
			v.setVisibility(View.INVISIBLE);
		}
		else
		{
			autoHideHash.put(v, mHideRunnable);
			mHideHandler.postDelayed(mHideRunnable, duration);
			view.setVisibility(View.VISIBLE);
		}
	}
	
	
//	private View.OnClickListener onMenubarClick = new OnClickListener() {
//		
//		@Override
//		public void onClick(View v) {
//			Log.w(GlobalData.DEBUG_TAG, "playlistPanel Clicked.");
//			autoHide(playlistPanel, 3000);
//		}
//	};
	
	private View.OnClickListener onPlaylistButtonClick = new OnClickListener() 
	{
		@Override
		public void onClick(View v) {
//			Log.w(GlobalData.DEBUG_TAG, "On onPlaylistButton Click");
//			autoHide(playlistPanel, 3000);
			if(playlistPanel.getVisibility() == View.VISIBLE)
				playlistPanel.setVisibility(View.INVISIBLE);
			else if(isFullscreenState == true)
				playlistPanel.setVisibility(View.VISIBLE);
		}
	};
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			toFullScreen();
		}else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			exitFullScreen();
		}
	}
	
	
	private OnSeekBarChangeListener onSeekBarChange = new OnSeekBarChangeListener() 
	{
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
		{
			if(!fromUser) return ;
			
			player.seekTo(progress);
		}
		
		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}
		
		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
	};

	
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
		if(player != null && player.isPlaying())
		{
			if(tikerTimer != null)
			{
				tikerTimer.cancel();
				tikerTimer.purge();
			}
			
			player.pause();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onResume");
		
		startTimer();
	}
	
	@Override
	protected void onStop()
	{
		super.onStop();
		Log.w(GlobalData.DEBUG_TAG, "ViewVideoActivity  onStop");
		if(player != null && player.isPlaying())
		{
			if(tikerTimer != null)
			{
				tikerTimer.cancel();
				tikerTimer.purge();
			}
			
			player.release();
			player = null;
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
