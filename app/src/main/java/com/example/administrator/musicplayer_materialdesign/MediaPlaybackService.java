package com.example.administrator.musicplayer_materialdesign;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.example.administrator.musicplayer_materialdesign.utils.LogUtil;

import java.util.ArrayList;
import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ID_ROOT = "__ROOT__";
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private MediaSessionCallback mCallback;

    @Override
    public void onCreate() {
        super.onCreate();
        //Create a MediaSessionCompat
        mMediaSession=new MediaSessionCompat(this,"MusicService");
        mCallback=new MediaSessionCallback();
        //Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS|
                                    MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mStateBuilder=new PlaybackStateCompat.Builder()
                                            .setActions(
                                                    PlaybackStateCompat.ACTION_PLAY|
                                                    PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());
        //MySessionCallback() has methods that handle callbacks from a media controller
        mMediaSession.setCallback(mCallback);
        //mMediaSession.setCallback(new MySessionCallback());
        Intent mediaButtonIntent=new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(getApplicationContext(),MediaPlaybackService.class);
        PendingIntent mbrIntent=PendingIntent.getService(getApplicationContext(),0,mediaButtonIntent,0);
        mMediaSession.setMediaButtonReceiver(mbrIntent);
        //Set the session's token so that client activities can communicate with it;
        setSessionToken(mMediaSession.getSessionToken());
        List<MediaSessionCompat.QueueItem> a = MainActivity.getListMediaQueue();
        LogUtil.LogDebug("aaa","playback.......service========="+a);
        mMediaSession.setQueue(a);
        //Get the session's metadata

    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        //mMediaSession=new MediaSessionCompat(this,"MusicService");
        MediaControllerCompat controller = mMediaSession.getController();
        MediaMetadataCompat mediaMetadata = controller.getMetadata();
        MediaDescriptionCompat description = mediaMetadata.getDescription();



        NotificationCompat.Builder builder= new NotificationCompat.Builder(this);
        builder.setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())

                .setContentIntent(controller.getSessionActivity())
                //stop the service when the notification is swiped away
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                        PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(R.mipmap.ic_notification)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimaryDark))
                .addAction(new NotificationCompat.Action(
                        R.mipmap.ic_pause_black_36dp,getString(R.string.drawer_close),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_PLAY_PAUSE)))
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(mMediaSession.getSessionToken())
                        .setShowActionsInCompactView(0)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP)));
        startForeground(0,builder.build());
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called to get the root information for browsing by a particular client.
     * <p>
     * The implementation should verify that the client package has permission
     * to access browse media information before returning the root id; it
     * should return null if the client is not allowed to access this
     * information.
     * </p>
     *
     * @param clientPackageName The package name of the application which is
     *                          requesting access to browse media.
     * @param clientUid         The uid of the application which is requesting access to
     *                          browse media.
     * @param rootHints         An optional bundle of service-specific arguments to send
     *                          to the media browse service when connecting and retrieving the
     *                          root id for browsing, or null if none. The contents of this
     *                          bundle may affect the information returned when browsing.
     * @return The {@link BrowserRoot} for accessing this app's content or null.
     * @see BrowserRoot#EXTRA_RECENT
     * @see BrowserRoot#EXTRA_OFFLINE
     * @see BrowserRoot#EXTRA_SUGGESTED
     */

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {

            return new MediaBrowserServiceCompat.BrowserRoot(MEDIA_ID_ROOT, null);

    }

    /**
     * Called to get information about the children of a media item.
     * <p>
     * Implementations must call {@link Result#sendResult result.sendResult}
     * with the list of children. If loading the children will be an expensive
     * operation that should be performed on another thread,
     * {@link Result#detach result.detach} may be called before returning from
     * this function, and then {@link Result#sendResult result.sendResult}
     * called when the loading is complete.
     * </p><p>
     * In case the media item does not have any children, call {@link Result#sendResult}
     * with an empty list. When the given {@code parentId} is invalid, implementations must
     * call {@link Result#sendResult result.sendResult} with {@code null}, which will invoke
     * {@link MediaBrowserCompat.SubscriptionCallback#onError}.
     * </p>
     *
     * @param parentId The id of the parent media item whose children are to be
     *                 queried.
     * @param result   The Result to send the list of children to.
     */
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        if(parentId==null){
            result.sendResult(null);
            return;
        }

        List<MediaBrowserCompat.MediaItem> mediaItems=new ArrayList<>();
        if(MEDIA_ID_ROOT.equals(parentId)){
            //build the MediaItem objects for the top level, and put them in the mediaItems list
        }else{
            // examine the passed parentMediaId to see which submenu we are at,
            //and put the children of that menu in the mediaItems list
        }
        result.detach();
        result.sendResult(MainActivity.getListMediaItem());
        LogUtil.LogDebug("info","playback...service...======="+result);
    }


    class MediaSessionCallback extends MediaSessionCompat.Callback{
        private final List<MediaSessionCompat.QueueItem> mPlaylist=new ArrayList<>();
        private int mQueueIndex=-1;
        private MediaMetadataCompat mPrepareMedia;
        @Override
        public void onAddQueueItem(MediaDescriptionCompat description) {
            mPlaylist.add(new MediaSessionCompat.QueueItem(description,description.hashCode()));
            mQueueIndex=(mQueueIndex==-1)? 0:mQueueIndex;
            mMediaSession.setQueue(mPlaylist);
        }

        @Override
        public void onRemoveQueueItem(MediaDescriptionCompat description) {
            mPlaylist.remove(new MediaSessionCompat.QueueItem(description,description.hashCode()));
            mQueueIndex=(mPlaylist.isEmpty())? -1:mQueueIndex;
            mMediaSession.setQueue(mPlaylist);
        }

        @Override
        public void onPrepare() {
            if(mQueueIndex<0&&mPlaylist.isEmpty()){
                return;
            }
            final String mediaId=mPlaylist.get(mQueueIndex).getDescription().getMediaId();

        }
    }
}
