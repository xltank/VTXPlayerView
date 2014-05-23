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
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
import com.videotx.vtxplayerlib.control.Facade;
import com.videotx.vtxplayerlib.control.ImageManager;
import com.videotx.vtxplayerlib.utils.CommonUtil;
import com.videotx.vtxplayerlib.utils.GlobalData;
import com.videotx.vtxplayerlib.utils.VideoUtil;
import com.videotx.vtxplayerlib.vo.PlaylistInfo;
import com.videotx.vtxplayerlib.vo.ThumbnailsVTX;
import com.videotx.vtxplayerlib.vo.VideoInfo;

public class VTXViewVideoComp extends LinearLayout 
							  implements Callback, 
							  OnPreparedListener, 
							  OnBufferingUpdateListener, 
							  OnVideoSizeChangedListener,
							  OnCompletionListener, 
							  OnSeekCompleteListener, 
							  OnErrorListener, 
							  OnInfoListener{
	
	
	public static final int STATE_IDLE = 0;
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PREPARED = 2;
    public static final int STATE_PLAYING = 3;
    public static final int STATE_PAUSED = 4;
    public static final int STATE_PLAYBACK_COMPLETED = 5;
    

	private Activity activity;

	private final int AUTO_HIDE_DELAY_MILLIS = 3000;

	private String curVideoInfoJSON;
	private VideoInfo curVideoInfo;

	private String curPlaylistInfoJSON;
	private PlaylistInfo curPlaylistInfo;

	private SurfaceView playerView;
	private IjkMediaPlayer player;
	private AudioManager audioManager;

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

	private LinearLayout menuBar;
	private TextView videoTitle;
	private Button playlistButton;
	// private Button menuButton;

	private LinearLayout playlistPanel;
	private ListView playlistView;
	private PlaylistArrayAdaptor playlistAdaptor;

	private Boolean isFullscreenState = false;

	private int bufferPercent;
	private long duration;
	private int videoWidth;
	private int videoHeight;

	private int maxVolumeIndex;

	private int playlistIndex = 0;
	
	

	public VTXViewVideoComp(Context context, Activity activity) {
		super(context);
		this.activity = activity;
		init();
	}

	public VTXViewVideoComp(Context context, AttributeSet attrs, Activity activity) {
		super(context, attrs);
		this.activity = activity;
		init();
	}

	public VTXViewVideoComp(Context context, AttributeSet attrs, int defStyle, Activity activity) {
		super(context, attrs, defStyle);
		this.activity = activity;
		init();
	}
	
	// ########## public methods
	
	public void playSingleVideo(String publisherId, String videoId) {
		Log.w(GlobalData.DEBUG_TAG, "play: publisherId - " + publisherId + "videoId - " + videoId);
		
		if( publisherId == null || publisherId == "" || videoId == null || videoId == "")
			return ;
		
		getVideoInfo(videoId, publisherId);
		startTimer();
	}
	
	public void playPlaylist(String publisherId, String playlistId) {
		Log.w(GlobalData.DEBUG_TAG, "play: publisherId - " + publisherId + "playlistId - " + playlistId);
		
		if( publisherId == null || publisherId == "" || playlistId == null || playlistId == "")
			return ;
		
		getPlaylistInfo(playlistId, publisherId);
		startTimer();
	}
	
	public void pause() {
		if (player != null && player.isPlaying()) {
			if (tikerTimer != null) {
				tikerTimer.cancel();
				tikerTimer.purge();
			}
			player.pause();
		}
	}
	
	public void resume() {
		player.start();
	}

	public void stop() {
		Log.w(GlobalData.DEBUG_TAG, " stop");
		if (player != null && player.isPlaying()) {
			player.stop();
		}
	}
	
	public void dispose() {
		Log.w(GlobalData.DEBUG_TAG, " dispose");
		if (player != null && player.isPlaying()) {
			if (tikerTimer != null) {
				tikerTimer.cancel();
				tikerTimer.purge();
			}

			player.release();
//			player = null;
		}
	}
	
    // TODO: 
	private int playState = STATE_IDLE;
	public int getPlayState() {
		return playState;
	}

	
	
	
	
	
	// ########## private methods
	
	private void init() {
		
//		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
//				 			 WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
		// requestFeature() must be called before add contents.
//		activity.requestWindowFeature(Window.FEATURE_NO_TITLE); 
//		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		addView(inflate(activity, R.layout.comp_view_video, null));

		audioManager = (AudioManager) activity.getSystemService(Service.AUDIO_SERVICE);
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
		controlBar.setOnTouchListener(onControlBarTouch);

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

		videoTitle = (TextView) findViewById(R.id.title);

		menuBar = (LinearLayout) findViewById(R.id.menuBar);
		menuBar.setOnTouchListener(onMenuBarTouch);

		playlistButton = (Button) findViewById(R.id.playlist_button);
		playlistButton.setOnClickListener(onPlaylistButtonClick);

		playlistPanel = (LinearLayout) findViewById(R.id.playlist_panel);
		playlistView = (ListView) findViewById(R.id.playlist_view);
	}

	// ########## video
	private void getVideoInfo(String videoId, String publisherId) {
		Facade.ins().getVideoInfo(onVideoInfoHandler, 
								  videoId, 
								  publisherId,
								  APIConstant.RESULT_FORMAT_JSON, 
								  APIConstant.VIDEO_TYPE_M3U8
//						          APIConstant.VIDEO_TYPE_MP4
								  );
	}

	final Handler onVideoInfoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			curVideoInfoJSON = msg.getData().getString("result");
			curVideoInfo = VideoUtil.parseVideoInfoJSON(curVideoInfoJSON);
			prepareVideoInfo();
		}
	};

	private void prepareVideoInfo() {
		if (curVideoInfo == null || curVideoInfo.renditions.size() == 0) {
			Log.e(GlobalData.DEBUG_TAG,
					"ERROR: video info error or no playable rendition");
			return;
		}

		videoUrl = curVideoInfo.renditions.get(0).getUrl();
		Log.w(GlobalData.DEBUG_TAG, "videoURL: "+videoUrl);
		duration = curVideoInfo.renditions.get(0).getDuration();
		videoWidth = curVideoInfo.renditions.get(0).getWidth();
		videoHeight = curVideoInfo.renditions.get(0).getHeight();

		videoTitle.setText(curVideoInfo.title);

		if (player != null) {
			player.reset(); // without reset(), last video is still there...
			player.release();// without release() after reset(), player does not
								// work.
			player = null;
		}
		// should we remove all listeners?
		// or we need not to do that because they are all in the same activity?
		if (player == null) {
			initPlayer();
			autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS, true);
			if (isFullscreenState == true)
				autoHide(menuBar, AUTO_HIDE_DELAY_MILLIS, true);
			// playlistPanel.setVisibility(View.INVISIBLE);
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
	private void getPlaylistInfo(String playlistId, String publisherId) {
		Facade.ins().getPlaylistInfo(onPlaylistInfoHandler, 
									playlistId,
									publisherId, 
									APIConstant.RESULT_FORMAT_JSON,
									APIConstant.VIDEO_TYPE_M3U8
//									APIConstant.VIDEO_TYPE_MP4
									);
	}

	final Handler onPlaylistInfoHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			curPlaylistInfoJSON = msg.getData().getString("result");
			curPlaylistInfo = VideoUtil.parsePlaylistInfoJSON(curPlaylistInfoJSON);
			if (curPlaylistInfo == null || curPlaylistInfo.videos.size() == 0) {
				Log.e(GlobalData.DEBUG_TAG,
						"ERROR: video info error or no playable rendition");
				return;
			}
			preparePlaylist();
		}
	};

	private void preparePlaylist() {
		playlistAdaptor = new PlaylistArrayAdaptor(activity, R.layout.item_playlist, curPlaylistInfo.videos);
		playlistView.setAdapter(playlistAdaptor);
		playlistView.setOnItemClickListener(onPlaylistItemClick);

		gotoPlaylistIndex(0);
	}

	private void gotoPlaylistIndex(int index) {
		// Log.w(GlobalData.DEBUG_TAG,
		// ""+playlistView.getSelectedItem()+", "+playlistView.getSelectedItemId()+", "+playlistView.getSelectedItemPosition()+", "+playlistView.getSelectedView());
		// Log.w(GlobalData.DEBUG_TAG,
		// ""+playlistView.getCheckedItemCount()+", "+playlistView.getCheckedItemPosition()+", "+playlistView.getCheckedItemPositions()+", "+playlistView.getCheckedItemIds());

		playlistIndex = index;
		// playlistView.smoothScrollToPosition(index);
		curVideoInfo = (VideoInfo) curPlaylistInfo.videos.get(index);
		prepareVideoInfo();

		// ###workaround of highlighting selected item...
		for (int i = 0; i < curPlaylistInfo.videos.size(); i++) {
			curPlaylistInfo.videos.get(i).selected = false;
		}
		curPlaylistInfo.videos.get(index).selected = true;
		playlistAdaptor.notifyDataSetChanged();
	}

	final OnItemClickListener onPlaylistItemClick = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (position == playlistView.getSelectedItemPosition())
				return;

			gotoPlaylistIndex(position);
			autoHide(playlistPanel, AUTO_HIDE_DELAY_MILLIS, false);
			autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS, false);
			autoHide(menuBar, AUTO_HIDE_DELAY_MILLIS, false);
		}
	};

	// ##########
	private void initPlayer() {
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

//		Intent i = new Intent("com.android.music.musicservicecommand");
//		i.putExtra("command", "pause");
//		sendBroadcast(i);
	}

	private void setSnapshot() {
		snapshot = (ImageView) findViewById(R.id.snapshot);
		String snapshotUrl = "";
		Collections.sort(curVideoInfo.thumbnails);
		int containerW = playerViewContainer.getWidth();
		int containerH = playerViewContainer.getHeight();
		for (ThumbnailsVTX t : curVideoInfo.thumbnails) {
			snapshotUrl = t.url;
			if (t.width > containerW || t.height > containerH)
				break;
		}
		ImageManager.ins().loadImage(snapshotUrl, snapshot, 1);
	}

	// //////////// player event listeners

	public void onPrepared(IMediaPlayer mp) {
		if (duration <= 0)
			duration = player.getDuration();
		if (videoWidth <= 0)
			videoWidth = player.getVideoWidth();
		if (videoHeight <= 0)
			videoHeight = player.getVideoHeight();
		layoutVideo();
		if (playerView != null)
			startPlaying();
	}

	public void onVideoSizeChanged(IMediaPlayer mp, int width, int height,
			int sarNum, int sarDen) {
	}

	public void onBufferingUpdate(IMediaPlayer mp, int percent) {
		bufferPercent = percent;
	}

	public boolean onError(IMediaPlayer mp, int what, int extra) {
		Log.w(GlobalData.DEBUG_TAG, "onError : " + what + " , " + extra);
		return false;
	}
	
	public boolean onInfo(IMediaPlayer mp, int what, int extra) {
		return false;
	}

	public void onSeekComplete(IMediaPlayer mp) {
		Log.w(GlobalData.DEBUG_TAG, "onSeekComplete : " + mp.getCurrentPosition());
	}

	public void onCompletion(IMediaPlayer mp) {
		Log.w(GlobalData.DEBUG_TAG, "onComplete ");
		if (curPlaylistInfo == null) // video
			playPauseButton.setPlayState(false);
		else // playlist
		if (playlistIndex < curPlaylistInfo.videos.size() - 1) {
			gotoPlaylistIndex(playlistIndex + 1);
		}

		// TODO: loop when it's video, but not playlist.
	}

	
	
	// //////// SurfaceView implements

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		// Log.w(GlobalData.DEBUG_TAG, "surface Changed");
	}

	// when activity is paused, surface is destroyed.
	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.w(GlobalData.DEBUG_TAG, "surface Destroyed");
	}

	// when activity is started/restarted, surface is created.
	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.w(GlobalData.DEBUG_TAG, "surface Created");
		startPlaying();
	}

	private void startPlaying() {
		Log.w(GlobalData.DEBUG_TAG, "start playing");
		player.setDisplay(playerView.getHolder());
		player.start();
		snapshot.setVisibility(View.INVISIBLE);//

		playPauseButton.setPlayState(true);

		startTimer();
	}

	private void startTimer() {
		tikerTimer = new Timer();
		tikerTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (player != null && player.isPlaying()) {
					timeLabel.post(new Runnable() {
						@Override
						public void run() {
							playProgressUpdate();
						}
					});
				}
			}
		}, 0, 200);
	}

	private void playProgressUpdate() {
		if (player == null)
			return;

		if (timeLabel != null)
			timeLabel.setText(player.getCurrentPosition() + "");

		if (seekBar != null) {
			seekBar.setMax((int) player.getDuration());
			seekBar.setProgress((int) player.getCurrentPosition());
			seekBar.setSecondaryProgress((int) (bufferPercent * 0.01 * duration));

			String time = CommonUtil
					.formatDuration(player.getCurrentPosition());
			String duration = CommonUtil.formatDuration(player.getDuration());
			timeLabel.setText(time + "/" + duration);
		}
	}

	final CustomRelativeLayout.OnSizeChangedListener onPlayerViewContainerSizeChcanged = new CustomRelativeLayout.OnSizeChangedListener() {
		private int oldW;
		private int oldH;

		@Override
		public void onEvent() {
			int w = playerViewContainer.getWidth();
			int h = playerViewContainer.getHeight();
			if (oldW != w || oldH != h) {
				oldW = w;
				oldH = h;
				layoutVideo();

				slideSeekHint.setX((w - slideSeekHint.getWidth()) / 2);
				slideSeekHint.setY((h - slideSeekHint.getHeight()) / 2);

				slideVolumeHint.setX((w - slideVolumeHint.getWidth()) / 2);
				slideVolumeHint.setY((h - slideVolumeHint.getHeight()) / 2);

				slideBrightnessHint
						.setX((w - slideBrightnessHint.getWidth()) / 2);
				slideBrightnessHint
						.setY((h - slideBrightnessHint.getHeight()) / 2);
			}
		}
	};

	// TODO: origin, zoom, scale, stretch; or 50%, 75%, 100%, stretch;
	private void layoutVideo() {
		if (player == null || playerView == null)
			return;

		if (videoWidth == 0 || videoHeight == 0)
			return;

		View playerViewParent = (View) playerView.getParent();
		int containerW = playerViewParent.getWidth();
		int containerH = playerViewParent.getHeight();
		if (containerW == 0 || containerH == 0)
			return;

		Point newSize = CommonUtil.getSuitableSize(videoWidth, videoHeight,
				containerW, containerH);
		// Log.w(GlobalData.DEBUG_TAG, newSize.x + "," + newSize.y);
		// !!! when setLayoutParams is called rapidly, video will not show.
		playerView.setLayoutParams(new RelativeLayout.LayoutParams(newSize.x,
				newSize.y));

		playerView.setX((containerW - newSize.x) / 2);
		playerView.setY((containerH - newSize.y) / 2);
	}

	// ////////////// play control

	private OnPlayButtonStateChangeListener onPlayButtonStateChangeListener = new OnPlayButtonStateChangeListener() {
		@Override
		public void onClick(View v, String state) {
			if (state == "play") {
				player.start();
			} else if (state == "pause") {
				player.pause();
			}
		}
	};

	private OnClickListener onVolumeButtonclick = new OnClickListener() {
		@Override
		public void onClick(View v) {

		}
	};

	private OnClickListener onFullScreenButtonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			toFullScreen();
		}
	};

	private void toFullScreen() {
		isFullscreenState = true;
		fullScreenButton.setVisibility(View.INVISIBLE);
		fullScreenExitButton.setVisibility(View.VISIBLE);

//		player.pause();
//
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//		findViewById(R.id.gap).setVisibility(View.GONE);
//		findViewById(R.id.video_info).setVisibility(View.GONE);
//
//		WindowManager.LayoutParams attrs = getWindow().getAttributes();
//		attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//		getWindow().setAttributes(attrs);
//
//		menuBar.setVisibility(View.VISIBLE);
//
//		layoutVideo();
//		player.start();
	}

	private OnClickListener onFullScreenExitButtonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			exitFullScreen();
		}
	};

	private void exitFullScreen() {
		isFullscreenState = false;
		fullScreenButton.setVisibility(View.VISIBLE);
		fullScreenExitButton.setVisibility(View.INVISIBLE);

//		player.pause();
//
//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//		findViewById(R.id.gap).setVisibility(View.VISIBLE);
//		findViewById(R.id.video_info).setVisibility(View.VISIBLE);
//
//		WindowManager.LayoutParams attrs = getWindow().getAttributes();
//		attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
//		getWindow().setAttributes(attrs);
//
//		layoutVideo();
//
//		menuBar.setVisibility(View.INVISIBLE);
//		playlistPanel.setVisibility(View.INVISIBLE);
//
//		player.start();
	}

	// TODO: test MOVEMENT_THRESHOLD
	private final int MOVEMENT_THRESHOLD = 10;
	private final int LEFT = 1;
	private final int RIGHT = 2;
	private final int TOP = 3;
	private final int BOTTOM = 4;
	final OnTouchListener onPlayerViewTouchListener = new OnTouchListener() {
		int direction = 0; // left=1, right=2, top=3, bottom=4;
		int area = 0; // left part=1, right part=2;
		float value = 0; // xValue or yValue;
		float firstDelta = 0; // for movement threshold in pixel to prevent
								// click from touch.
		float lastX = 0;
		float lastY = 0;
		float timeDelta = 0;
		float timeTarget = 0;
		float volumeDelta = 0;
		float volumeTarget = 0;
		float brightnessDelta = 0;
		float brightnessTarget = 0;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			int action = event.getActionMasked();
			float curX = event.getX();
			float curY = event.getY();
			switch (action) {
			case (MotionEvent.ACTION_DOWN):
				// Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_DOWN");
				area = event.getX() * 2 < playerViewContainer.getWidth() ? 1
						: 2;
				lastX = curX;
				lastY = curY;
				return false;
			case (MotionEvent.ACTION_MOVE):
				// Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_MOVE");
				// getHistoricalX()/getHistoricalY() is not perfect, for
				// sometime getHistorySize() == 0.
				float xValue = curX - lastX;
				float yValue = curY - lastY;
				if (xValue == 0 && yValue == 0) // first move event.
					return true;
				if (firstDelta == 0) {
					firstDelta = Math.max(xValue, yValue);
					if (firstDelta < MOVEMENT_THRESHOLD)
						return true;
				}
				if (direction == 0) {
					float diff = Math.abs(xValue) - Math.abs(yValue);
					if (diff >= 0)
						direction = xValue > 0 ? RIGHT : LEFT;
					else
						direction = yValue > 0 ? BOTTOM : TOP;
				}
				if (direction == LEFT || direction == RIGHT)
					value = xValue;
				else
					// if(direction == TOP || direction == BOTTOM)
					value = -yValue;
				// Log.w(GlobalData.DEBUG_TAG, "direction: " + direction + "," +
				// value + "," + (curX - lastX));

				if (direction == LEFT || direction == RIGHT) // progress
				{
					timeDelta += value * 50; // time delta = 1/20 * xValue.
					timeTarget = player.getCurrentPosition() + timeDelta;
					if (timeTarget < 0 || timeTarget > duration)
						return true;
					slideSeekTime.setText(CommonUtil
							.formatDuration((long) timeTarget)
							+ "/"
							+ CommonUtil.formatDuration((long) duration));
					autoHide(slideSeekHint, 1000, true);
				} else if (area == 1) // brightness
				{
					brightnessDelta += value * 0.1;
					int curBrightness = 0;
					try {
						curBrightness = System.getInt(activity.getContentResolver(),
								System.SCREEN_BRIGHTNESS);
						// LayoutParams lp = getWindow().getAttributes();
						// Log.w(GlobalData.DEBUG_TAG, "curBrightness " +
						// curBrightness + ", " + lp.screenBrightness);
					} catch (SettingNotFoundException e) {
						e.printStackTrace();
					}
					brightnessTarget = curBrightness + brightnessDelta;
					if (brightnessTarget < 0 || brightnessTarget > 255)
						return true;
					// Log.w(GlobalData.DEBUG_TAG, brightnessDelta + ", " +
					// curBrightness + ", " + brightnessTarget);
//					getLayoutParams().
					WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
					lp.screenBrightness = brightnessTarget / 255;
					activity.getWindow().setAttributes(lp);
					System.putInt(activity.getContentResolver(),
							System.SCREEN_BRIGHTNESS, (int) brightnessTarget);
					slideBrightness.setText((int) brightnessTarget * 100 / 255
							+ "%");
					autoHide(slideBrightnessHint, 1000, true);
				} else if (area == 2) // volume
				{
					// Android does not provide explicit volume control.
					int curVolumeIndex = audioManager
							.getStreamVolume(AudioManager.STREAM_MUSIC);
					volumeDelta += value * 0.005; // volume grade index.
					volumeTarget = Math.min(
							Math.max(curVolumeIndex + volumeDelta, 0),
							maxVolumeIndex);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							(int) volumeTarget, AudioManager.FLAG_PLAY_SOUND);
					slideVolume
							.setText((int) (volumeTarget / maxVolumeIndex * 100)
									+ "%");
					autoHide(slideVolumeHint, 1000, true);
				}

				lastX = curX;
				lastY = curY;
				return true;
			case (MotionEvent.ACTION_UP):
				// Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_UP");
				firstDelta = 0;
				if (direction == LEFT || direction == RIGHT)
					player.seekTo((int) (player.getCurrentPosition() + timeDelta));
				// else if(area == 1)
				else if (area == 2) {
					volumeButton
							.setVolume((int) (volumeTarget / maxVolumeIndex * 100));
				}
				direction = 0;
				timeDelta = 0;
				volumeDelta = 0;
				brightnessDelta = 0;
				return false;
			case (MotionEvent.ACTION_OUTSIDE):
				// Log.w(GlobalData.DEBUG_TAG, "On Touch ACTION_OUTSIDE");
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

	final OnClickListener onPlayerViewClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.w(GlobalData.DEBUG_TAG, "On PlayerView Click");
			if (controlBar.getVisibility() == View.VISIBLE
					|| menuBar.getVisibility() == View.VISIBLE
					|| playlistPanel.getVisibility() == View.VISIBLE) {
				controlBar.setVisibility(View.INVISIBLE);
				menuBar.setVisibility(View.INVISIBLE);
				playlistPanel.setVisibility(View.INVISIBLE);
			} else {
				autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS, true);
				if (isFullscreenState == true)
					autoHide(menuBar, AUTO_HIDE_DELAY_MILLIS, true);
			}

		}
	};

	private View.OnTouchListener onMenuBarTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.w(GlobalData.DEBUG_TAG, "onMenubarTouch Touch.");
			autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS, false);
			autoHide(menuBar, AUTO_HIDE_DELAY_MILLIS, false);
			return true;
		}
	};

	private View.OnTouchListener onControlBarTouch = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.w(GlobalData.DEBUG_TAG, "onControlBarTouch Touch.");
			autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS, false);
			autoHide(menuBar, AUTO_HIDE_DELAY_MILLIS, false);
			return true;
		}
	};

	private View.OnClickListener onPlaylistButtonClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			Log.w(GlobalData.DEBUG_TAG, "On onPlaylistButton Click");
			// if(playlistPanel.getVisibility() == View.VISIBLE)
			// playlistPanel.setVisibility(View.INVISIBLE);
			// else
			// playlistPanel.setVisibility(View.VISIBLE);
			autoHide(playlistPanel, AUTO_HIDE_DELAY_MILLIS, true);
			autoHide(controlBar, AUTO_HIDE_DELAY_MILLIS, false);
			autoHide(menuBar, AUTO_HIDE_DELAY_MILLIS, false);
		}
	};

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			toFullScreen();
		} else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
			exitFullScreen();
		}
	}

	private OnSeekBarChangeListener onSeekBarChange = new OnSeekBarChangeListener() {
		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			if (!fromUser)
				return;

			player.seekTo(progress);
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}
	};

	
	// utils

	private HashMap<View, Runnable> autoHideHash = new HashMap<View, Runnable>();
	private Handler mHideHandler = new Handler();

	private void autoHide(View v, int duration, Boolean hideIfVisible) {
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
		if (lastRunnable != null) {
			Log.w(GlobalData.DEBUG_TAG, "autohide : exists " + v.getTag());
			autoHideHash.remove(v);
			mHideHandler.removeCallbacks(lastRunnable);
		}
		if (hideIfVisible == true && v.getVisibility() == View.VISIBLE) {
			v.setVisibility(View.INVISIBLE);
		}

		autoHideHash.put(v, mHideRunnable);
		mHideHandler.postDelayed(mHideRunnable, duration);
		view.setVisibility(View.VISIBLE);
	}

}
