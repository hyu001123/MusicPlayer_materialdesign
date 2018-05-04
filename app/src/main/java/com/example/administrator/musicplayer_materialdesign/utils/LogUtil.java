package com.example.administrator.musicplayer_materialdesign.utils;

import android.util.Log;

public class LogUtil {
    private static  boolean DEBUG=true;
    public static void LogDebug(String tag,String msg){
        if(DEBUG) {
            Log.i(tag, msg);
        }
    }
}
