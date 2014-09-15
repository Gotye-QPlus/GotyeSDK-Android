package com.gotye.sdk.logic.beans;

import com.gotye.api.bean.GotyeImageMessage;

public class GotyeImageMessageProxy extends GotyeMessageProxy<GotyeImageMessage> {

	public GotyeImageMessageProxy(GotyeImageMessage message) {
		super(message);
	}

	public String getSavePath() {
		return savePath;
	}

	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	private String savePath;
}
