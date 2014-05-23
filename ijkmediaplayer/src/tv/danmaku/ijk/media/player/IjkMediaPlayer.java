/*
 * Copyright (C) 2006 The Android Open Source Project
 * Copyright (C) 2013 Zhang Rui <bbcallen@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tv.danmaku.ijk.media.player;

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.ref.WeakReference;

import tv.danmaku.ijk.media.player.annotations.AccessedByNative;
import tv.danmaku.ijk.media.player.option.AvFormatOption;
import tv.danmaku.ijk.media.player.pragma.DebugLog;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.view.Surface;
import android.view.SurfaceHolder;

/**
 * @author bbcallen
 * 
 *         Java wrapper of ffplay.
 */
public final class IjkMediaPlayer extends SimpleMediaPlayer {
    private final static String TAG = IjkMediaPlayer.class.getName();

    private static final int MEDIA_NOP = 0; // interface test message
    private static final int MEDIA_PREPARED = 1;
    private static final int MEDIA_PLAYBACK_COMPLETE = 2;
    private static final int MEDIA_BUFFERING_UPDATE = 3;
    private static final int MEDIA_SEEK_COMPLETE = 4;
    private static final int MEDIA_SET_VIDEO_SIZE = 5;
    private static final int MEDIA_TIMED_TEXT = 99;
    private static final int MEDIA_ERROR = 100;
    private static final int MEDIA_INFO = 200;

    protected static final int MEDIA_SET_VIDEO_SAR = 10001;

    static {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("ffmpeg");
        System.loadLibrary("ijkutil");
        System.loadLibrary("ijksdl");
        System.loadLibrary("ijkplayer");
        native_init();
    }

    @AccessedByNative
    private long mNativeMediaPlayer;

    @AccessedByNative
    private int mNativeSurfaceTexture;

    @AccessedByNative
    private int mListenerContext;

    private SurfaceHolder mSurfaceHolder;
    private EventHandler mEventHandler;
    private PowerManager.WakeLock mWakeLock = null;
    private boolean mScreenOnWhilePlaying;
    private boolean mStayAwake;

    private int mVideoWidth;
    private int mVideoHeight;
    private int mVideoSarNum;
    private int mVideoSarDen;

    private String mDataSource;

    /**
     * Default constructor. Consider using one of the create() methods for
     * synchronously instantiating a IjkMediaPlayer from a Uri or resource.
     * <p>
     * When done with the IjkMediaPlayer, you should call {@link #release()}, to
     * free the resources. If not released, too many IjkMediaPlayer instances
     * may result in an exception.
     * </p>
     */
    public IjkMediaPlayer() {
        Looper looper;
        if ((looper = Looper.myLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else if ((looper = Looper.getMainLooper()) != null) {
            mEventHandler = new EventHandler(this, looper);
        } else {
            mEventHandler = null;
        }

        /*
         * Native setup requires a weak reference to our object. It's easier to
         * create it here than in C++.
         */
        native_setup(new WeakReference<IjkMediaPlayer>(this));
    }

    /*
     * Update the IjkMediaPlayer SurfaceTexture. Call after setting a new
     * display surface.
     */
    private native void _setVideoSurface(Surface surface);

    /**
     * Sets the {@link SurfaceHolder} to use for displaying the video portion of
     * the media.
     * 
     * Either a surface holder or surface must be set if a display or video sink
     * is needed. Not calling this method or {@link #setSurface(Surface)} when
     * playing back a video will result in only the audio track being played. A
     * null surface holder or surface will result in only the audio track being
     * played.
     * 
     * @param sh
     *            the SurfaceHolder to use for video display
     */
    @Override
    public void setDisplay(SurfaceHolder sh) {
        mSurfaceHolder = sh;
        Surface surface;
        if (sh != null) {
            surface = sh.getSurface();
        } else {
            surface = null;
        }
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    /**
     * Sets the {@link Surface} to be used as the sink for the video portion of
     * the media. This is similar to {@link #setDisplay(SurfaceHolder)}, but
     * does not support {@link #setScreenOnWhilePlaying(boolean)}. Setting a
     * Surface will un-set any Surface or SurfaceHolder that was previously set.
     * A null surface will result in only the audio track being played.
     * 
     * If the Surface sends frames to a {@link SurfaceTexture}, the timestamps
     * returned from {@link SurfaceTexture#getTimestamp()} will have an
     * unspecified zero point. These timestamps cannot be directly compared
     * between different media sources, different instances of the same media
     * source, or multiple runs of the same program. The timestamp is normally
     * monotonically increasing and is unaffected by time-of-day adjustments,
     * but it is reset when the position is set.
     * 
     * @param surface
     *            The {@link Surface} to be used for the video portion of the
     *            media.
     */
    @Override
    public void setSurface(Surface surface) {
        if (mScreenOnWhilePlaying && surface != null) {
            DebugLog.w(TAG,
                    "setScreenOnWhilePlaying(true) is ineffective for Surface");
        }
        mSurfaceHolder = null;
        _setVideoSurface(surface);
        updateSurfaceScreenOn();
    }

    /**
     * Sets the data source (file-path or http/rtsp URL) to use.
     * 
     * @param path
     *            the path of the file, or the http/rtsp URL of the stream you
     *            want to play
     * @throws IllegalStateException
     *             if it is called in an invalid state
     * 
     *             <p>
     *             When <code>path</code> refers to a local file, the file may
     *             actually be opened by a process other than the calling
     *             application. This implies that the pathname should be an
     *             absolute path (as any other process runs with unspecified
     *             current working directory), and that the pathname should
     *             reference a world-readable file. As an alternative, the
     *             application could first open the file for reading, and then
     *             use the file descriptor form
     *             {@link #setDataSource(FileDescriptor)}.
     */
    @Override
    public void setDataSource(String path) throws IOException,
            IllegalArgumentException, SecurityException, IllegalStateException {
        mDataSource = path;
        _setDataSource(path, null, null);
    }

    private native void _setDataSource(String path, String[] keys,
            String[] values) throws IOException, IllegalArgumentException,
            SecurityException, IllegalStateException;

    @Override
    public String getDataSource() {
        return mDataSource;
    }

    @Override
    public native void prepareAsync() throws IllegalStateException;

    @Override
    public void start() throws IllegalStateException {
        stayAwake(true);
        _start();
    }

    private native void _start() throws IllegalStateException;

    @Override
    public void stop() throws IllegalStateException {
        stayAwake(false);
        _stop();
    }

    private native void _stop() throws IllegalStateException;

    @Override
    public void pause() throws IllegalStateException {
        stayAwake(false);
        _pause();
    }

    private native void _pause() throws IllegalStateException;

    @SuppressLint("Wakelock")
    @Override
    public void setWakeMode(Context context, int mode) {
        boolean washeld = false;
        if (mWakeLock != null) {
            if (mWakeLock.isHeld()) {
                washeld = true;
                mWakeLock.release();
            }
            mWakeLock = null;
        }

        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(mode | PowerManager.ON_AFTER_RELEASE,
                IjkMediaPlayer.class.getName());
        mWakeLock.setReferenceCounted(false);
        if (washeld) {
            mWakeLock.acquire();
        }
    }

    @Override
    public void setScreenOnWhilePlaying(boolean screenOn) {
        if (mScreenOnWhilePlaying != screenOn) {
            if (screenOn && mSurfaceHolder == null) {
                DebugLog.w(TAG,
                        "setScreenOnWhilePlaying(true) is ineffective without a SurfaceHolder");
            }
            mScreenOnWhilePlaying = screenOn;
            updateSurfaceScreenOn();
        }
    }

    @SuppressLint("Wakelock")
    private void stayAwake(boolean awake) {
        if (mWakeLock != null) {
            if (awake && !mWakeLock.isHeld()) {
                mWakeLock.acquire();
            } else if (!awake && mWakeLock.isHeld()) {
                mWakeLock.release();
            }
        }
        mStayAwake = awake;
        updateSurfaceScreenOn();
    }

    private void updateSurfaceScreenOn() {
        if (mSurfaceHolder != null) {
            mSurfaceHolder.setKeepScreenOn(mScreenOnWhilePlaying && mStayAwake);
        }
    }

    @Override
    public int getVideoWidth() {
        return mVideoWidth;
    }

    @Override
    public int getVideoHeight() {
        return mVideoHeight;
    }

    @Override
    public int getVideoSarNum() {
        return mVideoSarNum;
    }

    @Override
    public int getVideoSarDen() {
        return mVideoSarDen;
    }

    @Override
    public native boolean isPlaying();

    @Override
    public native void seekTo(long msec) throws IllegalStateException;

    @Override
    public native long getCurrentPosition();

    @Override
    public native long getDuration();

    /**
     * Releases resources associated with this IjkMediaPlayer object. It is
     * considered good practice to call this method when you're done using the
     * IjkMediaPlayer. In particular, whenever an Activity of an application is
     * paused (its onPause() method is called), or stopped (its onStop() method
     * is called), this method should be invoked to release the IjkMediaPlayer
     * object, unless the application has a special need to keep the object
     * around. In addition to unnecessary resources (such as memory and
     * instances of codecs) being held, failure to call this method immediately
     * if a IjkMediaPlayer object is no longer needed may also lead to
     * continuous battery consumption for mobile devices, and playback failure
     * for other applications if no multiple instances of the same codec are
     * supported on a device. Even if multiple instances of the same codec are
     * supported, some performance degradation may be expected when unnecessary
     * multiple instances are used at the same time.
     */
    @Override
    public void release() {
        stayAwake(false);
        updateSurfaceScreenOn();
        resetListeners();
        _release();
    }

    private native void _release();

    @Override
    public void reset() {
        stayAwake(false);
        _reset();
        // make sure none of the listeners get called anymore
        mEventHandler.removeCallbacksAndMessages(null);

        mVideoWidth = 0;
        mVideoHeight = 0;
    }

    private native void _reset();

    @Override
    public MediaInfo getMediaInfo() {
        MediaInfo mediaInfo = new MediaInfo();

        mediaInfo.mVideoDecoder = "ijkmedia";
        mediaInfo.mVideoDecoderImpl = "SW";

        mediaInfo.mAudioDecoder = "ijkmedia";
        mediaInfo.mAudioDecoderImpl = "SW";

        return mediaInfo;
    }

    public void setAvOption(AvFormatOption option) {
        setAvFormatOption(option.getName(), option.getValue());
    }

    public void setAvFormatOption(String name, String value) {
        _setAvFormatOption(name, value);
    }

    private native void _setAvFormatOption(String name, String value);

    @Override
    public void setAudioStreamType(int streamtype) {
        // do nothing
    }

    private static native final void native_init();

    private native final void native_setup(Object IjkMediaPlayer_this);

    private native final void native_finalize();

    private native final void native_message_loop(Object IjkMediaPlayer_this);

    protected void finalize() {
        native_finalize();
    }

    private static class EventHandler extends Handler {
        private WeakReference<IjkMediaPlayer> mWeakPlayer;

        public EventHandler(IjkMediaPlayer mp, Looper looper) {
            super(looper);
            mWeakPlayer = new WeakReference<IjkMediaPlayer>(mp);
        }

        @Override
        public void handleMessage(Message msg) {
            IjkMediaPlayer player = mWeakPlayer.get();
            if (player == null || player.mNativeMediaPlayer == 0) {
                DebugLog.w(TAG,
                        "IjkMediaPlayer went away with unhandled events");
                return;
            }

            switch (msg.what) {
            case MEDIA_PREPARED:
                player.notifyOnPrepared();
                return;

            case MEDIA_PLAYBACK_COMPLETE:
                player.notifyOnCompletion();
                player.stayAwake(false);
                return;

            case MEDIA_BUFFERING_UPDATE:
                player.notifyOnBufferingUpdate(msg.arg1);
                return;

            case MEDIA_SEEK_COMPLETE:
                player.notifyOnSeekComplete();
                return;

            case MEDIA_SET_VIDEO_SIZE:
                player.mVideoWidth = msg.arg1;
                player.mVideoHeight = msg.arg2;
                player.notifyOnVideoSizeChanged(msg.arg1, msg.arg2,
                        player.mVideoSarNum, player.mVideoSarDen);
                return;

            case MEDIA_ERROR:
                DebugLog.e(TAG, "Error (" + msg.arg1 + "," + msg.arg2 + ")");
                if (!player.notifyOnError(msg.arg1, msg.arg2)) {
                    player.notifyOnCompletion();
                }
                player.stayAwake(false);
                return;

            case MEDIA_INFO:
                if (msg.arg1 != MEDIA_INFO_VIDEO_TRACK_LAGGING) {
                    DebugLog.i(TAG, "Info (" + msg.arg1 + "," + msg.arg2 + ")");
                }
                player.notifyOnInfo(msg.arg1, msg.arg2);
                // No real default action so far.
                return;
            case MEDIA_TIMED_TEXT:
                // do nothing
                break;

            case MEDIA_NOP: // interface test message - ignore
                break;

            case MEDIA_SET_VIDEO_SAR:
                player.mVideoSarNum = msg.arg1;
                player.mVideoSarDen = msg.arg2;
                player.notifyOnVideoSizeChanged(msg.arg1, msg.arg2,
                        player.mVideoSarNum, player.mVideoSarDen);
                break;

            default:
                DebugLog.e(TAG, "Unknown message type " + msg.what);
                return;
            }
        }
    }

    /*
     * Called from native code when an interesting event happens. This method
     * just uses the EventHandler system to post the event back to the main app
     * thread. We use a weak reference to the original IjkMediaPlayer object so
     * that the native code is safe from the object disappearing from underneath
     * it. (This is the cookie passed to native_setup().)
     */
    private static void postEventFromNative(Object weakThiz, int what,
            int arg1, int arg2, Object obj) {
        if (weakThiz == null)
            return;

        @SuppressWarnings("rawtypes")
        IjkMediaPlayer mp = (IjkMediaPlayer) ((WeakReference) weakThiz).get();
        if (mp == null) {
            return;
        }

        if (what == MEDIA_INFO && arg1 == MEDIA_INFO_STARTED_AS_NEXT) {
            // this acquires the wakelock if needed, and sets the client side
            // state
            mp.start();
        }
        if (mp.mEventHandler != null) {
            Message m = mp.mEventHandler.obtainMessage(what, arg1, arg2, obj);
            mp.mEventHandler.sendMessage(m);
        }
    }
}
