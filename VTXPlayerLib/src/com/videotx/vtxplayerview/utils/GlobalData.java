package com.videotx.vtxplayerview.utils;

import java.util.List;

import com.videotx.vtxplayerview.vo.Manager;
import com.videotx.vtxplayerview.vo.Playlist;
import com.videotx.vtxplayerview.vo.Publisher;
import com.videotx.vtxplayerview.vo.Video;

public class GlobalData {

	public static String DEBUG_TAG = "VTXPlayer";
	
	public static String apiDomain = "http://api.staging.video-tx.com/";
	
	public static String token = "";
	
	// If true, viewing video is allowed in 2G/3G.
	public static Boolean mobile_data_allowed = false; 
	
	public static int IMAGE_CACHE_SIZE = 100;
	
	public static Publisher curPublisher = null;
	public static Manager curManager = null;
	
	
	public static List<Video> videos;
	public static List<Playlist> playlists;
	
}
