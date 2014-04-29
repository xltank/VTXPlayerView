package com.videotx.vtxplayerlib.vo;

import org.json.JSONException;
import org.json.JSONObject;


public class Video{

	
	public Video(JSONObject json)
	{
		try {
			setPublisherId(json.getString("publisherId"));
			setManagerId(json.getString("managerId"));
			setFolderId(json.getString("folderId"));
			setTitle(json.getString("title"));
			setDescription(json.getString("description"));
			setStatus(json.getString("status"));
			setDeleted(json.getBoolean("deleted"));
			setFileSize(json.getInt("fileSize"));
			setWidth(json.getInt("width"));
			setHeight(json.getInt("height"));
			setDuration(json.getInt("duration"));
			setKbps(json.getInt("kbps"));
			setFrameRate(json.getInt("frameRate"));
			setAudioChannels(json.getInt("audioChannels"));
			setAudioSampleRate(json.getInt("audioSampleRate"));
			setTags(json.getString("tags"));
			setVideoKey(json.getString("videoKey"));
			setVideoUrl(json.getString("videoUrl"));
			setThumbnailUrl(json.getString("thumbnailUrl"));
			setSnapshotUrl(json.getString("snapshotUrl"));
			setCoverUrl(json.getString("coverUrl"));
			setErrorMessage(json.getString("errorMessage"));
			setSnapshotGroupId(json.getString("snapshotGroupId"));
			setRenditionGroupId(json.getInt("renditionGroupId"));
			setIpRestriction(json.getBoolean("ipRestriction"));
			setPublicRestriction(json.getBoolean("publicRestriction"));
			setFileType(json.getString("fileType"));
			setLinkText(json.getString("linkText"));
			setLinkUrl(json.getString("linkUrl"));
			setLocal(json.getBoolean("local"));
			setRemote(json.getBoolean("remote"));
			setAutoBps(json.getBoolean("autoBps"));
			setMetadataUrl(json.getString("metadataUrl"));
			setLockVideo(json.getBoolean("lockVideo"));
			setSharedVideoId(json.getString("sharedVideoId"));
			setHasGeo(json.getBoolean("hasGeo"));
			setLatitude(json.getInt("latitude"));
			setLongitude(json.getInt("longitude"));
			setLastFinishedTime(json.getInt("lastFinishedTime"));
			setEncrypted(json.getBoolean("encrypted"));
			setAudioKbps(json.getInt("audioKbps"));
			setVideoKbps(json.getInt("videoKbps"));
			setCustomFields(json.getString("customFields"));
			setId(json.getString("id"));
			setVersion(json.getInt("version"));
			setCreationTime(json.getInt("creationTime"));
			setModifiedTime(json.getInt("modifiedTime"));
		} 
		catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public String toString()
	{
		return this.title;
	}
	
	
    public static enum VideoStatus {

    	//@Label("�ȴ��ϴ�")
    	WAITING_UPLOAD,

    	//@Label("�ļ�����")
    	FILE_ERROR,
    	
    	//@Label("���ת��")
    	AUDIT_TRANSCODING,
    	
    	//@Label("�������")
    	AUDIT_DISTRIBUTING,

    	//@Label("�ȴ����")
    	WAITING_APPROVE,

    	//@Label("���δͨ��")
    	NOT_APPROVED,

    	//@Label("�ȴ�����")
    	PENDING,
    	
    	//@Label("�ϲ����")
    	CLIPPED,

    	//@Label("����ת��")
    	TRANSCODING,

    	//@Label("��������")
    	DISTRIBUTING,
    	
    	//@Label("����Ԫ����")
    	META_MERGING,
    	
    	//@Label("����Ԫ����")
    	META_DISTRIBUTING,

    	//@Label("�������")
    	FINISHED,

    	//@Label("ת�����")
    	TRANSCODE_ERROR,

    	//@Label("���ʹ���")
    	DISTRIBUTE_ERROR,
    	
    	//@Label("��ͼ����")
        SNAPSHOT_ERROR,
        
        //@Label("����Ԫ���ݴ���")
    	META_MERGE_ERROR,
    	
    	//@Label("����Ԫ���ݴ���")
    	META_DISTRIBUTE_ERROR,
        
        //@Label("��Ƶ����")
        OFFLINE,
        
    	@Deprecated
    	//@Label("NO_RENDITION")
        NO_RENDITION,
    }
    /**
     * ��˾ID
     */
    private String publisherId;
    /**
     * ����ԱID
     */
    private String managerId;
    /**
     * ����Ŀ¼ID
     */
    private String folderId;
    /**
     * ����
     */
    private String title;
    /**
     * ����
     */
    private String description;
    /**
     * ��Ƶ״̬
     */
    private String status;
    /**
     * �Ƿ�ɾ��
     */
    private boolean deleted;
    /**
     * �ļ���С
     */
    private long fileSize;
    /**
     * ���
     */
    private int width;
    /**
     * �߶�
     */
    private int height;
    /**
     * ʱ��
     */
    private int duration;
    /**
     * ����
     */
    private int kbps;
    /**
     * ֡��
     */
    private int frameRate;
    /**
     * ��ƵƵ��
     */
    private int audioChannels;
    /**
     * ��Ƶ������
     */
    private int audioSampleRate;
    /**
     * ��ǩ
     */
    private String tags;
    /**
     * videoKey
     */
    private String videoKey;
    /**
     * ��ƵURL��ַ
     */
    private String videoUrl;
    /**
     * СͼƬ��ַ
     */
    private String thumbnailUrl;
    /**
     * ����ͼ��ַ
     */
    private String snapshotUrl;
    /**
     * ����URL
     */
    private String coverUrl;
    /**
     * ������Ϣ
     */
    private String errorMessage;
    /**
     * ����ͼ����ID
     */
    private String snapshotGroupId;
    /**
     * ����汾����ID
     */
    private int renditionGroupId;
    /**
     * �Ƿ���IP����
     */
    private boolean ipRestriction;
    /**
     * �Ƿ񹫿�����
     */
    private boolean publicRestriction;
    /**
     * �ļ�����
     */
    private String fileType;
    /**
     * �����ı�
     */
    private String linkText;
    /**
     * ����URL
     */
    private String linkUrl;
    /**
     * �Ƿ��ڱ��ش洢
     */
    private boolean local = true;
    /**
     * �Ƿ�Զ�˴洢
     */
    private boolean remote = false;
    /**
     * autobps
     */
    private boolean autoBps;
    /**
     * metadataURL��ַ
     */
    private String metadataUrl;
    /**
     * �Ƿ���������ֹɾ��������ת���
     */
    private boolean lockVideo;
    /**
     * �����ԭ��ƵID
     */
    private String sharedVideoId;
    /**
     * �Ƿ����geo��Ϣ
     */
    private boolean hasGeo;
    /**
     * ά��
     */
    double latitude;
    /**
     * ����
     */
    double longitude;
    /**
     * ���һ��ת��ɹ���ʱ��
     */
    private long lastFinishedTime;
    /**
     * �Ƿ�����DRM
     */
    private boolean encrypted;
    /**
     * ��Ƶ����
     */
    private int audioKbps;
    /**
     * ��Ƶ����
     */
    private int videoKbps;
    
    private String customFields = "";
    private String id = "";
	private int version = 0;
    private long creationTime = 0L;
    private long modifiedTime = 0L;
    
    
    
    public String getPublisherId() {
		return publisherId;
	}
	public void setPublisherId(String publisherId) {
		this.publisherId = publisherId;
	}
	public String getManagerId() {
		return managerId;
	}
	public void setManagerId(String managerId) {
		this.managerId = managerId;
	}
	public String getFolderId() {
		return folderId;
	}
	public void setFolderId(String folderId) {
		this.folderId = folderId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public boolean isDeleted() {
		return deleted;
	}
	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	public long getFileSize() {
		return fileSize;
	}
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	public int getWidth() {
		return width;
	}
	public void setWidth(int width) {
		this.width = width;
	}
	public int getHeight() {
		return height;
	}
	public void setHeight(int height) {
		this.height = height;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public int getKbps() {
		return kbps;
	}
	public void setKbps(int kbps) {
		this.kbps = kbps;
	}
	public int getFrameRate() {
		return frameRate;
	}
	public void setFrameRate(int frameRate) {
		this.frameRate = frameRate;
	}
	public int getAudioChannels() {
		return audioChannels;
	}
	public void setAudioChannels(int audioChannels) {
		this.audioChannels = audioChannels;
	}
	public int getAudioSampleRate() {
		return audioSampleRate;
	}
	public void setAudioSampleRate(int audioSampleRate) {
		this.audioSampleRate = audioSampleRate;
	}
	public String getTags() {
		return tags;
	}
	public void setTags(String tags) {
		this.tags = tags;
	}
	public String getVideoKey() {
		return videoKey;
	}
	public void setVideoKey(String videoKey) {
		this.videoKey = videoKey;
	}
	public String getVideoUrl() {
		return videoUrl;
	}
	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}
	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}
	public String getSnapshotUrl() {
		return snapshotUrl;
	}
	public void setSnapshotUrl(String snapshotUrl) {
		this.snapshotUrl = snapshotUrl;
	}
	public String getCoverUrl() {
		return coverUrl;
	}
	public void setCoverUrl(String coverUrl) {
		this.coverUrl = coverUrl;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getSnapshotGroupId() {
		return snapshotGroupId;
	}
	public void setSnapshotGroupId(String snapshotGroupId) {
		this.snapshotGroupId = snapshotGroupId;
	}
	public int getRenditionGroupId() {
		return renditionGroupId;
	}
	public void setRenditionGroupId(int renditionGroupId) {
		this.renditionGroupId = renditionGroupId;
	}
	public boolean isIpRestriction() {
		return ipRestriction;
	}
	public void setIpRestriction(boolean ipRestriction) {
		this.ipRestriction = ipRestriction;
	}
	public boolean isPublicRestriction() {
		return publicRestriction;
	}
	public void setPublicRestriction(boolean publicRestriction) {
		this.publicRestriction = publicRestriction;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	public String getLinkText() {
		return linkText;
	}
	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}
	public String getLinkUrl() {
		return linkUrl;
	}
	public void setLinkUrl(String linkUrl) {
		this.linkUrl = linkUrl;
	}
	public boolean isLocal() {
		return local;
	}
	public void setLocal(boolean local) {
		this.local = local;
	}
	public boolean isRemote() {
		return remote;
	}
	public void setRemote(boolean remote) {
		this.remote = remote;
	}
	public boolean isAutoBps() {
		return autoBps;
	}
	public void setAutoBps(boolean autoBps) {
		this.autoBps = autoBps;
	}
	public String getMetadataUrl() {
		return metadataUrl;
	}
	public void setMetadataUrl(String metadataUrl) {
		this.metadataUrl = metadataUrl;
	}
	public boolean isLockVideo() {
		return lockVideo;
	}
	public void setLockVideo(boolean lockVideo) {
		this.lockVideo = lockVideo;
	}
	public String getSharedVideoId() {
		return sharedVideoId;
	}
	public void setSharedVideoId(String sharedVideoId) {
		this.sharedVideoId = sharedVideoId;
	}
	public boolean isHasGeo() {
		return hasGeo;
	}
	public void setHasGeo(boolean hasGeo) {
		this.hasGeo = hasGeo;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public long getLastFinishedTime() {
		return lastFinishedTime;
	}
	public void setLastFinishedTime(long lastFinishedTime) {
		this.lastFinishedTime = lastFinishedTime;
	}
	public boolean isEncrypted() {
		return encrypted;
	}
	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
	public int getAudioKbps() {
		return audioKbps;
	}
	public void setAudioKbps(int audioKbps) {
		this.audioKbps = audioKbps;
	}
	public int getVideoKbps() {
		return videoKbps;
	}
	public void setVideoKbps(int videoKbps) {
		this.videoKbps = videoKbps;
	}
	public String getCustomFields() {
		return customFields;
	}
	public void setCustomFields(String customFields) {
		this.customFields = customFields;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public long getCreationTime() {
		return creationTime;
	}
	public void setCreationTime(long creationTime) {
		this.creationTime = creationTime;
	}
	public long getModifiedTime() {
		return modifiedTime;
	}
	public void setModifiedTime(long modifiedTime) {
		this.modifiedTime = modifiedTime;
	}
	
	
}
