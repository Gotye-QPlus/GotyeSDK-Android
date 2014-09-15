package com.gotye.sdk.logic.beans;

import com.gotye.api.bean.GotyeMessage;

public class GotyeMessageProxy<T extends GotyeMessage> {

	public static final int SUCCESS = 200;
	public static final int FAILED = 201;
	public static final int FORBIDDEN = 202;
	
	private T message;
	private String showTime;
	private int sendState;

	public GotyeMessageProxy(T message) {
		super();
		this.message = message;
	}
	
	public T get(){
		return message;
	}
	
	public boolean isSendBySelf() {
		return isSendBySelf;
	}

	public void setSendBySelf(boolean isSendBySelf) {
		this.isSendBySelf = isSendBySelf;
	}

	public String getShowTime() {
		return showTime;
	}

	public void setShowTime(String showTime) {
		this.showTime = showTime;
	}


	public int getSendState() {
		return sendState;
	}

	public void setSendState(int sendState) {
		this.sendState = sendState;
	}


	private boolean isSendBySelf = false;
}
