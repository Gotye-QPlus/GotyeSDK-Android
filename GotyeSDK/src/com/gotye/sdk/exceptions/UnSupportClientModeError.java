package com.gotye.sdk.exceptions;

/**
 *
 * un support client mode
 *
 * Created by lhxia on 13-12-24.
 */
public class UnSupportClientModeError extends Error {

    public UnSupportClientModeError() {
    }

    public UnSupportClientModeError(String detailMessage) {
        super(detailMessage);
    }

    public UnSupportClientModeError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UnSupportClientModeError(Throwable throwable) {
        super(throwable);
    }
}
