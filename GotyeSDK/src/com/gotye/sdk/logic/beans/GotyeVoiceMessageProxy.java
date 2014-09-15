package com.gotye.sdk.logic.beans;

import com.gotye.api.bean.GotyeVoiceMessage;

public class GotyeVoiceMessageProxy extends GotyeMessageProxy<GotyeVoiceMessage>{
	
	public GotyeVoiceMessageProxy(GotyeVoiceMessage message) {
		super(message);
	}

	private String savePath;
	private boolean isNewRecoder = false;
	private boolean isDownloading = false;
	private boolean downloadFailed = false;

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public boolean isNewRecoder() {
		return isNewRecoder;
	}

	public void setNewRecoder(boolean isNewRecoder) {
		this.isNewRecoder = isNewRecoder;
	}

	public boolean isDownloading() {
		return isDownloading;
	}

	public void setDownloading(boolean isDownloading) {
		this.isDownloading = isDownloading;
	}

	public boolean isDownloadFailed() {
		return downloadFailed;
	}

	public void setDownloadFailed(boolean downloadFailed) {
		this.downloadFailed = downloadFailed;
	}
	
}
