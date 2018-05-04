package com.example.administrator.musicplayer_materialdesign;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.administrator.musicplayer_materialdesign.utils.LogUtil;

public class PlayCtlFragment extends Fragment{
    private ImageView ivMusic;
    private TextView tvSongName;
    private TextView tvAlbum;
    private ImageButton ibPlayOrPause;
    private String mArtUri;
    private MediaControllerCompat mediaCtl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_playctl,container,false);
        ivMusic=(ImageView)view.findViewById(R.id.music_tag);
        tvSongName=(TextView)view.findViewById(R.id.song_name);
        tvAlbum=(TextView)view.findViewById(R.id.artist);
        ibPlayOrPause=(ImageButton)view.findViewById(R.id.ibtn_play);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ibPlayOrPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mediaCtl=MediaControllerCompat.getMediaController(getActivity());
                if(mediaCtl==null){
                    return;
                }
                PlaybackStateCompat stateObj = mediaCtl.getPlaybackState();
                int state = stateObj == null ? PlaybackStateCompat.STATE_NONE : stateObj.getState();
                if(state==PlaybackStateCompat.STATE_NONE||state==PlaybackStateCompat.STATE_PAUSED
                        ||state==PlaybackStateCompat.STATE_STOPPED){
                    //mediaCtl.getTransportControls().playFromMediaId();
                }else if(state==PlaybackStateCompat.STATE_BUFFERING||state==PlaybackStateCompat.STATE_PLAYING
                        ||state==PlaybackStateCompat.STATE_CONNECTING){
                    mediaCtl.getTransportControls().pause();
                }

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        if(mediaCtl!=null){
            onConnected();
        }
    }

    private void onConnected() {
        if(mediaCtl!=null){
            onMetadataChanged(mediaCtl.getMetadata());
            onPlaybackStateChanged(mediaCtl.getPlaybackState());
            mediaCtl.registerCallback(callback);
        }
    }


    @Override
    public void onStop() {
        super.onStop();
        if(mediaCtl!=null){
            mediaCtl.unregisterCallback(callback);
        }
    }

    MediaControllerCompat.Callback callback=new MediaControllerCompat.Callback() {
        /**
         * Override to handle changes in playback state.
         *
         * @param state The new playback state of the session
         */
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            PlayCtlFragment.this.onPlaybackStateChanged(state);
        }

        /**
         * Override to handle changes to the current metadata.
         *
         * @param metadata The current metadata for the session or null if none.
         * @see MediaMetadataCompat
         */
        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if(metadata==null){
                return;
            }
            PlayCtlFragment.this.onMetadataChanged(metadata);
        }

        /**
         * Override to handle changes to the repeat mode.
         *
         * @param repeatMode The repeat mode. It should be one of followings:
         *                   {@link PlaybackStateCompat#REPEAT_MODE_NONE},
         *                   {@link PlaybackStateCompat#REPEAT_MODE_ONE},
         *                   {@link PlaybackStateCompat#REPEAT_MODE_ALL},
         *                   {@link PlaybackStateCompat#REPEAT_MODE_GROUP}
         */
        @Override
        public void onRepeatModeChanged(int repeatMode) {
            super.onRepeatModeChanged(repeatMode);
        }
    };

    public void onMetadataChanged(MediaMetadataCompat metadata) {
        LogUtil.LogDebug("tag",""+metadata);
        if(getActivity()==null||metadata==null){
            return;
        }
        tvSongName.setText(metadata.getDescription().getTitle());
        tvAlbum.setText(metadata.getDescription().getSubtitle());
        String pictureUrl = metadata.getDescription().getIconUri().toString();
        if(!TextUtils.equals(mArtUri,pictureUrl)){
            mArtUri=pictureUrl;
            Bitmap art=metadata.getDescription().getIconBitmap();
            LruCache cache=new LruCache(10);
            Glide.with(this).load(art).into(ivMusic);
        }


    }

    public void onPlaybackStateChanged(PlaybackStateCompat state) {
        LogUtil.LogDebug("tag",""+state);
        if(getActivity()==null||state==null){
            return;
        }
        boolean enablePlay = false;
        switch (state.getState()){
            case PlaybackStateCompat.STATE_PAUSED:
            case PlaybackStateCompat.STATE_STOPPED:
                enablePlay=true;
                break;
            case PlaybackStateCompat.STATE_ERROR:
                LogUtil.LogDebug("tag",""+state.getErrorMessage());
                Toast.makeText(getActivity(), state.getErrorMessage(), Toast.LENGTH_LONG).show();
                break;
        }
        if(enablePlay){
            ibPlayOrPause.setImageResource(R.mipmap.ic_play_arrow_black_36dp);
        }else{
            ibPlayOrPause.setImageResource(R.mipmap.ic_pause_black_36dp);
        }
    }
}
