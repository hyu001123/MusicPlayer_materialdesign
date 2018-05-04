package com.example.administrator.musicplayer_materialdesign;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import com.example.administrator.musicplayer_materialdesign.client.MediaBrowserHelper;
import com.example.administrator.musicplayer_materialdesign.db.Music;
import com.example.administrator.musicplayer_materialdesign.utils.LogUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.crud.DataSupport;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerlayout;
    private Toolbar toolbar;
    private NavigationView navView;
    private CardView cardView;
    private ActionBarDrawerToggle drawerToggle;
    private MediaBrowserCompat mMediaBrowser;
    private PlayCtlFragment playCtlFragment;
    private MediaBrowserCompat.ConnectionCallback mConnectionCallback;
    private List<Music> db;
    private MediaBrowserFragment musicfragment=new MediaBrowserFragment();
    private String path="http://storage.googleapis.com/automotive-media/music.json";
    private String basePath="http://storage.googleapis.com/automotive-media/";
    private static final String ARTIST_LIST = "artist_list";
    private static final String ALBUM_LIST = "album_list";
    private static final String SONG_LIST = "song_list";
    private static List<MediaBrowserCompat.MediaItem> listMediaItem=new ArrayList<>();
    private static List<MediaSessionCompat.QueueItem> listMediaQueue=new ArrayList<>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        drawerlayout=(DrawerLayout)findViewById(R.id.drawerlayout);
        toolbar=(Toolbar)findViewById(R.id.toolbar);
        navView=(NavigationView)findViewById(R.id.nav_view);
        cardView=(CardView)findViewById(R.id.fragment_container);
        playCtlFragment=(PlayCtlFragment)getSupportFragmentManager()
                .findFragmentById(R.id.playctl_fragment);
        initToolBar();
        initMediaDataQueue();
        db = DataSupport.findAll(Music.class);
        if(db.size()==0) initDBData();
         getSupportFragmentManager().beginTransaction()
                .replace(R.id.lv_fragment_container, musicfragment)
                .commit();
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                LogUtil.LogDebug("info","id==="+item.getItemId());
                switch (item.getItemId()){
                    case R.id.allmusic:
                        musicfragment.setGenres();
                        toolbar.setNavigationIcon(R.mipmap.ic_menu);
                        drawerlayout.closeDrawers();
                        break;
                    case R.id.playlist:
                        musicfragment.setGenres();
                        drawerlayout.closeDrawers();
                        break;
                }
                return  false;
            }
        });

        //Create MediaBrowserServiceCompat

    }

    private void initMediaDataQueue() {
        List<Music> data = DataSupport.findAll(Music.class);
        int count=0;
        for(Music music:data){
            String album = music.getAlbum();
            String artist = music.getArtist();
            String genre = music.getGenre();
            String imageUrl = music.getImageUrl();
            String mp3Url = music.getSourceUrl();
            String title = music.getTitle();
            int duration = music.getDuration();
            int totalTraCount = music.getTotalTrackCount();
            int trackNum = music.getTrackNumber();
            MediaMetadataCompat mediaMetaData = new MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, String.valueOf(mp3Url.hashCode()))
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_URI, mp3Url)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, album)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, duration)
                    .putString(MediaMetadataCompat.METADATA_KEY_GENRE, "genre")
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, imageUrl)
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title)
                    .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, trackNum)
                    .putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, totalTraCount)
                    .build();
            MediaSessionCompat.QueueItem item=new MediaSessionCompat.QueueItem(mediaMetaData.getDescription(),count++);
            MediaMetadataCompat copy = new MediaMetadataCompat.Builder(mediaMetaData)
                    .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, "MUSIC" + mediaMetaData.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID))
                    .build();
            MediaBrowserCompat.MediaItem mMediaItem = new MediaBrowserCompat.MediaItem(copy.getDescription(),MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            LogUtil.LogDebug("aaa","mediaMetadata.Description.mediaId===="+copy.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            listMediaItem.add(mMediaItem);
            listMediaQueue.add(item);
        }
    }

    public static  List<MediaBrowserCompat.MediaItem> getListMediaItem() {
        return listMediaItem;
    }

    public static  List<MediaSessionCompat.QueueItem> getListMediaQueue() {
        return listMediaQueue;
    }

    public Toolbar getToolbar() {
        return toolbar;
    }


    private void initDBData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();
                    Request request=new Request.Builder()
                            .url(path)
                            .build();
                    Response response=null;
                    response=client.newCall(request).execute();
                    if(response.isSuccessful()){
                        String responseData = response.body().string();
                        parserJSONByGSON(responseData);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void parserJSONByGSON(String responseData) {
        try {
            JSONObject object=new JSONObject(responseData);
            JSONArray musics = object.getJSONArray("music");
            for(int i=0;i<musics.length();i++){
                JSONObject obj = musics.getJSONObject(i);
                StringBuilder sb = new StringBuilder();
                sb.append(basePath);
                Music music=new Music();
                music.setTitle(obj.getString("title"));
                music.setAlbum(obj.getString("album"));
                music.setArtist(obj.getString("artist"));
                music.setGenre(obj.getString("genre"));
                music.setSourceUrl(sb.append(obj.getString("source")).toString());
                sb.delete(0,sb.length());
                sb.append(basePath);
                music.setImageUrl(sb.append(obj.getString("image")).toString());
                music.setTrackNumber(obj.getInt("trackNumber"));
                music.setTotalTrackCount(obj.getInt("totalTrackCount"));
                music.setDuration(obj.getInt("duration"));
                music.save();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    final MediaBrowserCompat.ConnectionCallback  mediaConnectionCallback=new MediaBrowserCompat.ConnectionCallback(){
        @Override
        public void onConnected() {
            try{
                MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();
                MediaControllerCompat mediaCtl = new MediaControllerCompat(MainActivity.this, token);
                MediaControllerCompat.setMediaController(MainActivity.this,mediaCtl);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    };






    private void initToolBar() {
        toolbar.setTitle("Music Player");
        toolbar.setTitleTextColor(Color.WHITE);
        toolbar.setNavigationIcon(R.mipmap.ic_menu);
        setSupportActionBar(toolbar);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /**
         * other way to control the open or close in navigation
         */
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               if(musicfragment.getCurrentPosition().equals(ARTIST_LIST)){
                   if(navView.isShown()){
                       drawerlayout.closeDrawers();
                       toolbar.setNavigationIcon(R.mipmap.ic_menu);
                   }else{
                       drawerlayout.openDrawer(Gravity.START);
                       toolbar.setNavigationIcon(R.mipmap.backspace_white_48x48);
                   }
               }else if(musicfragment.getCurrentPosition().equals(SONG_LIST)){
                   String sqlStr="select distinct genre from music";
                   musicfragment.setCurrentPosition(ALBUM_LIST);
                   musicfragment.setAlbumData(sqlStr);
               }else if(musicfragment.getCurrentPosition().equals(ALBUM_LIST)){
                   musicfragment.setCurrentPosition(ARTIST_LIST);
                   musicfragment.setGenres();
                   toolbar.setNavigationIcon(R.mipmap.ic_menu);
               }
           }
       });
       /* drawerToggle= new ActionBarDrawerToggle(this,drawerlayout,toolbar,R.string.drawer_open,R.string.drawer_close);
        drawerToggle.syncState();
        drawerlayout.addDrawerListener(drawerToggle);*/
    }


    private void hidePlaybackControls() {
        getSupportFragmentManager().beginTransaction()
                .hide(playCtlFragment)
                .commit();
    }

    private void showPlaybackControls() {
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom,
                        R.animator.slide_in_from_bottom, R.animator.slide_out_to_bottom)
                .show(playCtlFragment)
                .commit();
    }


    protected boolean shouldShowControls() {
        MediaControllerCompat mediaController = MediaControllerCompat.getMediaController(this);
        if (mediaController == null ||
                mediaController.getMetadata() == null ||
                mediaController.getPlaybackState() == null) {
            return false;
        }
        switch (mediaController.getPlaybackState().getState()) {
            case PlaybackStateCompat.STATE_ERROR:
            case PlaybackStateCompat.STATE_NONE:
            case PlaybackStateCompat.STATE_STOPPED:
                return false;
            default:
                return true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
