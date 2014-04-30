package com.videotx.vtxplayerlib.comps;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.videotx.vtxplayerlib.R;
import com.videotx.vtxplayerlib.control.ImageManager;
import com.videotx.vtxplayerlib.utils.CommonUtil;
import com.videotx.vtxplayerlib.vo.VideoInfo;

public class PlaylistArrayAdaptor extends ArrayAdapter<VideoInfo> 
{
	public PlaylistArrayAdaptor(Context context, int resource, List<VideoInfo> objects) {
        super(context, resource, objects);
    }
	
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		if(convertView == null)
		{
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.item_playlist, null);
		}
		ImageView imageView = (ImageView) convertView.findViewById(R.id.image_item_playlist);
		TextView titleText = (TextView) convertView.findViewById(R.id.title_item_playlist);
		TextView durationText = (TextView) convertView.findViewById(R.id.duration_item_playlist);
		
		VideoInfo video = getItem(position);
		titleText.setText(video.title);
		if(video.thumbnails.size() > 0)
			ImageManager.ins().loadImage(video.thumbnails.get(0).url, imageView, 2);
		if(video.renditions.size() > 0)
			durationText.setText(CommonUtil.formatDuration(video.renditions.get(0).duration));
		
		return convertView;
	}
}
