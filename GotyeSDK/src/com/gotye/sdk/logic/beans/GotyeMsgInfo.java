package com.gotye.sdk.logic.beans;

/**
 * Created by lhxia on 13-12-26.
 */
public class GotyeMsgInfo {
    private GotyeUserEx user;
    private int unreadCount;

    public GotyeUserEx getUser() {
        return user;
    }

    public void setUser(GotyeUserEx user) {
        this.user = user;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}
