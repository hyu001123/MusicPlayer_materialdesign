package com.example.administrator.musicplayer_materialdesign.client;

import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.media.session.MediaControllerCompat.Callback;

import com.example.administrator.musicplayer_materialdesign.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MediaBrowserHelper {

    private final Context mContext;
    private final Class<? extends MediaBrowserServiceCompat> mMediaService;
    private final MediaBrowserConnectionCallback mMediaBrowserConnectionCallback;
    private final MediaControllerCallback mMediaControllerCallback;
    private final MediaBrowserSubscriptionCallback mMediaBrowserSubscriptionCallback;
    private MediaBrowserCompat mMediaBrowser;
    private MediaControllerCompat mMediaController;
    private final List<Callback> mCallbackList = new ArrayList<>();

    public MediaBrowserHelper(Context context, Class<? extends MediaBrowserServiceCompat> serviceMedia){
        mContext=context;
        mMediaService=serviceMedia;
        mMediaBrowserConnectionCallback=new MediaBrowserConnectionCallback();
        mMediaControllerCallback=new MediaControllerCallback();
        mMediaBrowserSubscriptionCallback=new MediaBrowserSubscriptionCallback();
    }

    public void onStart(){
        if(mMediaBrowser==null){
            mMediaBrowser=new MediaBrowserCompat(mContext,
                    new ComponentName(mContext,mMediaService),mMediaBrowserConnectionCallback,null);
            mMediaBrowser.connect();
        }
        LogUtil.LogDebug("tag","connectionBrowser is connecting");
    }

    public void onStop(){
        if(mMediaController!=null){
            mMediaController.unregisterCallback(mMediaControllerCallback);
            mMediaController=null;
        }else if(mMediaBrowser!=null&&mMediaBrowser.isConnected()){
            mMediaBrowser.disconnect();
            mMediaBrowser=null;
        }
        resetState();
    }

    private void resetState() {
        performOnAllCallbacks(new CallbackCommand() {
            @Override
            public void perform(@NonNull MediaControllerCompat.Callback callback) {
                callback.onPlaybackStateChanged(null);
            }
        });

    }


    public void registerCallback(Callback callback){
        if (callback != null) {
            mCallbackList.add(callback);

            // Update with the latest metadata/playback state.
            if (mMediaController != null) {
                final MediaMetadataCompat metadata = mMediaController.getMetadata();
                if (metadata != null) {
                    callback.onMetadataChanged(metadata);
                }

                final PlaybackStateCompat playbackState = mMediaController.getPlaybackState();
                if (playbackState != null) {
                    callback.onPlaybackStateChanged(playbackState);
                }
            }
        }

    }
    private void performOnAllCallbacks(@NonNull CallbackCommand command) {
        for (MediaControllerCompat.Callback callback : mCallbackList) {
            if (callback != null) {
                command.perform(callback);
            }
        }
    }

    /**
     * Helper for more easily performing operations on all listening clients.
     */
    private interface CallbackCommand {
        void perform(@NonNull MediaControllerCompat.Callback callback);
    }




    public MediaControllerCompat getMediaController(){
        return mMediaController;
    }


    public MediaControllerCompat.TransportControls getTransportControls(){
        return mMediaController.getTransportControls();
    }



    class MediaBrowserConnectionCallback extends MediaBrowserCompat.ConnectionCallback{
        @Override
        public void onConnected() {
            try{
                mMediaController=new MediaControllerCompat(mContext,mMediaBrowser.getSessionToken());
                mMediaController.registerCallback(mMediaControllerCallback);
                mMediaControllerCallback.onMetadataChanged(mMediaController.getMetadata());
                mMediaControllerCallback.onPlaybackStateChanged(mMediaController.getPlaybackState());
                MediaBrowserHelper.this.onConnected(mMediaController);
            }catch (Exception e ){
                e.printStackTrace();
            }
            mMediaBrowser.subscribe(mMediaBrowser.getRoot(),mMediaBrowserSubscriptionCallback);
        }
    }


    class MediaControllerCallback extends MediaControllerCompat.Callback{
        @Override
        public void onMetadataChanged(final MediaMetadataCompat metadata) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaControllerCompat.Callback callback) {
                    callback.onMetadataChanged(metadata);
                }
            });
        }

        @Override
        public void onPlaybackStateChanged(final PlaybackStateCompat state) {
            performOnAllCallbacks(new CallbackCommand() {
                @Override
                public void perform(@NonNull MediaControllerCompat.Callback callback) {
                    callback.onPlaybackStateChanged(state);
                }
            });
        }

        public void onSessionDestroyed() {
            resetState();
            onPlaybackStateChanged(null);

            MediaBrowserHelper.this.onDisconnected();
        }
    }

    class MediaBrowserSubscriptionCallback extends MediaBrowserCompat.SubscriptionCallback{
        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            MediaBrowserHelper.this.onChildrenLoaded(parentId, children);
        }
    }


    public void onConnected(MediaControllerCompat mMediaController) {
    }


    public void onChildrenLoaded(@NonNull String parentId,
                                  @NonNull List<MediaBrowserCompat.MediaItem> children) {
    }


    public void onDisconnected() {
    }
}
