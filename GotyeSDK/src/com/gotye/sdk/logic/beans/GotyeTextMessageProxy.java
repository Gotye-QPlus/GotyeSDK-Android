package com.gotye.sdk.logic.beans;

import com.gotye.api.bean.GotyeTextMessage;

public class GotyeTextMessageProxy extends GotyeMessageProxy<GotyeTextMessage> {

	public GotyeTextMessageProxy(GotyeTextMessage message) {
		super(message);
	}

	
	public CharSequence getTextCharSeq() {
		return textCharSeq;
	}


	public void setTextCharSeq(CharSequence textCharSeq) {
		this.textCharSeq = textCharSeq;
	}


	private CharSequence textCharSeq;
}
