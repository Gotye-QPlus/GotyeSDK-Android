package com.gotye.sdk.config;

import android.os.Environment;

/**
 *
 * read-only config, general before packaging
 *
 * Created by lhxia on 13-12-24.
 */
public interface Configs {

    ClientMode clientMode = ClientMode.ROOM;
    
    
    String ROOT_FOLDER = "/sdcard/gotye";
    
    String SEND_PIC_FOLDER = ROOT_FOLDER + "/sendpics";
    
    String SEND_VOICE_FOLDER = ROOT_FOLDER + "/sendvoices";
    
    String RECV_PIC_FOLDER = ROOT_FOLDER + "/recvpics";
    
    String RECV_VOICE_FOLDER = ROOT_FOLDER + "/recvvoices";
    
    String DATA_CACHE_FOLDER = ROOT_FOLDER + "/roomcache";
    
    String DOWNLOAD_FOLDER = ROOT_FOLDER + "/downloads";
    
    String DOWNLOAD_CACHE = Environment.getDataDirectory().getAbsolutePath() + "/gotye/cache";
}
