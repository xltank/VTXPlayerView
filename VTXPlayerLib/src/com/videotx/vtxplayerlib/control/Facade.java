package com.videotx.vtxplayerlib.control;

import com.videotx.vtxplayerlib.constants.APIConstant;
import com.videotx.vtxplayerlib.utils.GlobalData;

import android.os.Handler;

public class Facade {

	private static Facade _facade = new Facade();
	
	private Facade()
	{
	}
	
	public static Facade ins()
	{
		return _facade;
	}
	
	
	
	public String login(Handler handler, String email, String passwd)
	{
//		System.out.println("API : APIConstant.LOGIN");
		String params = new HttpParamsVTX("email", email, "passwd", passwd).toString();
    	
		try {
			APILoader apiLoader = new APILoader(handler, 
												GlobalData.apiDomain + APIConstant.LOGIN, 
												APILoader.POST, 
												params);
			return apiLoader.execute().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	public String getVideoInfo(Handler handler, String videoId, String publisherId, String format, String types)
	{
//		System.out.println("API : APIConstant.VIDEO_INFO");
		String params = new HttpParamsVTX("videoId", videoId, 
							  			  "publisherId", publisherId, 
							  			  "format", format, 
							  			  "types", types).toString();
		APILoader apiLoader = new APILoader(handler, 
											GlobalData.apiDomain + APIConstant.VIDEO_INFO, 
											APILoader.GET, 
											params);
		try {
			return apiLoader.execute().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getPlaylistInfo(Handler handler, String playlistId, String publisherId, String format, String types)
	{
//		System.out.println("API : APIConstant.VIDEO_INFO");
		String params = new HttpParamsVTX("playlistId", playlistId, 
							  			  "publisherId", publisherId, 
							  			  "format", format, 
							  			  "types", types).toString();
		APILoader apiLoader = new APILoader(handler, 
											GlobalData.apiDomain + APIConstant.PLAYLIST_INFO, 
											APILoader.GET, 
											params);
		try {
			return apiLoader.execute().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
	public String getRecentVideos(Handler handler, String maxResults, String firstResult)
	{
//		System.out.println("API : APIConstant.GET_RECENT_VIDEOS");
		String params = new HttpParamsVTX("maxResults", maxResults, 
							  			  "firstResult", firstResult,
							  			  "token", GlobalData.token).toString();
		APILoader apiLoader = new APILoader(handler, 
											GlobalData.apiDomain + APIConstant.GET_RECENT_VIDEOS, 
											APILoader.GET, 
											params);
		try {
			return apiLoader.execute().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getPlaylists(Handler handler, String maxResults, String firstResult)
	{
//		System.out.println("API : APIConstant.GET_PLAYLISTS");
		String params = new HttpParamsVTX("maxResults", maxResults, 
							  			  "firstResult", firstResult,
							  			  "token", GlobalData.token).toString();
		APILoader apiLoader = new APILoader(handler, 
											GlobalData.apiDomain + APIConstant.GET_PLAYLISTS, 
											APILoader.GET, 
											params);
		try {
			return apiLoader.execute().get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	
}
