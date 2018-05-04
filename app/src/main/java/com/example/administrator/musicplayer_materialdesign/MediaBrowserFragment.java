package com.example.administrator.musicplayer_materialdesign;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaDescriptionCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.administrator.musicplayer_materialdesign.Modle.InfoMusic;
import com.example.administrator.musicplayer_materialdesign.client.MediaBrowserHelper;
import com.example.administrator.musicplayer_materialdesign.db.Music;
import com.example.administrator.musicplayer_materialdesign.utils.LogUtil;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class MediaBrowserFragment extends Fragment{
    private static final int ALBUM_DATA_DISPLAY = 1;
    private static final int MUSIC_DATA_DISPLAY = 2;
    private static final String ARTIST_LIST = "artist_list";
    private static final String ALBUM_LIST = "album_list";
    private static final String SONG_LIST = "song_list";
    private ListView lvMusic;
    private MediaMusicAdapter adapter;
    private String currentPosition;
    private Cursor dbcursor;
    private List<Music>  listData=new ArrayList<>();
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case ALBUM_DATA_DISPLAY:
                    adapter.notifyDataSetChanged();
                    lvMusic.setSelection(0);
                    break;
                case MUSIC_DATA_DISPLAY:
                    adapter.notifyDataSetChanged();
                    lvMusic.setSelection(0);
                    break;
            }
        }
    };
    private Toolbar toolbar;
    private MediaBrowserConnection mMediaBrowserHelper;
    private String mp3Url;
    private MediaSessionCompat mMediaSession;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_mediabrowser,container,false);
        lvMusic=(ListView)view.findViewById(R.id.lv_music);
        return view;
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        adapter=new MediaMusicAdapter((AppCompatActivity)getActivity(),listData);
        lvMusic.setAdapter(adapter);
        setGenres();
        lvMusic.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if(currentPosition.equals(ARTIST_LIST)){
                    String sqlStr="select distinct genre from music";
                    setAlbumData(sqlStr);


                }else if(currentPosition.equals(ALBUM_LIST)){
                    switch (position){
                        case 0:
                            String sqlStr1="select * from music where genre == \"Jazz & Blues\"";
                            setMusicData(sqlStr1);
                            break;
                        case 1:
                            String sqlStr2="select * from music where genre == \"Cinematic\"";
                            setMusicData(sqlStr2);
                            break;
                        case 2:
                            String sqlStr3="select * from music where genre == \"Rock\"";
                            setMusicData(sqlStr3);
                            break;
                        default:
                            break;
                    }
                }else if(currentPosition.equals(SONG_LIST)){
                    String mp3Source = listData.get(position).getSourceUrl();
                    LogUtil.LogDebug("tag","media Id==="+mp3Source.hashCode());
                   mMediaBrowserHelper.getMediaController().getTransportControls().playFromMediaId(String.valueOf(mp3Source.hashCode()),null);

                }
            }
        });
        mMediaBrowserHelper = new MediaBrowserConnection(getActivity());
        mMediaBrowserHelper.registerCallback(new MediaBrowserListener());
    }


    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowserHelper.onStart();
    }


    @Override
    public void onStop() {
        super.onStop();
        mMediaBrowserHelper.onStop();
    }

    public void setGenres() {
        currentPosition=ARTIST_LIST;
        listData.clear();
        Music music = new Music();
        music.setTitle("Genres");
        music.setArtist("songs by genre");
        listData.add(music);
        adapter.notifyDataSetChanged();
    }

    private void setMusicData(final String sqlStr) {
        currentPosition=SONG_LIST;
        new Thread(new Runnable() {
            @Override
            public void run() {
                listData.clear();
                dbcursor= DataSupport.findBySQL(sqlStr);
                if(!dbcursor.isLast()) {
                    dbcursor.moveToFirst();
                    while (!dbcursor.isAfterLast()) {
                        String title = dbcursor.getString(dbcursor.getColumnIndex("title"));
                        String artist=dbcursor.getString(dbcursor.getColumnIndex("artist"));
                        String album=dbcursor.getString(dbcursor.getColumnIndex("album"));
                        mp3Url=dbcursor.getString(dbcursor.getColumnIndex("sourceurl"));
                        String picture=dbcursor.getString(dbcursor.getColumnIndex("imageurl"));
                        int duration=dbcursor.getInt(dbcursor.getColumnIndex("duration"));
                        int trackNumber=dbcursor.getInt(dbcursor.getColumnIndex("tracknumber"));
                        int totalTrackCount=dbcursor.getInt(dbcursor.getColumnIndex("totaltrackcount"));
                        Music music = new Music();
                        music.setTitle(title);
                        music.setArtist(artist);
                        music.setAlbum(album);
                        music.setSourceUrl(mp3Url);
                        music.setImageUrl(picture);
                        music.setDuration(duration);
                        music.setTrackNumber(trackNumber);
                        music.setTotalTrackCount(totalTrackCount);
                        listData.add(music);
                        dbcursor.moveToNext();
                    }
                    LogUtil.LogDebug("tag","music data========"+listData.toString());
                    dbcursor.close();
                    handler.sendEmptyMessage(MUSIC_DATA_DISPLAY);
                }
            }
        }).start();

    }




    public void setAlbumData(final String sql){
        currentPosition=ALBUM_LIST;
        MainActivity activity = (MainActivity) getActivity();
        activity.getToolbar().setNavigationIcon(R.mipmap.reply_white_54x54);
        new Thread(new Runnable() {
            @Override
            public void run() {
                listData.clear();
                dbcursor= DataSupport.findBySQL(sql);
                if(!dbcursor.isLast()) {
                    dbcursor.moveToFirst();
                    while (!dbcursor.isAfterLast()) {
                        String itemData = dbcursor.getString(dbcursor.getColumnIndex("genre"));
                        Music info = new Music();
                        info.setTitle(itemData);
                        info.setArtist(itemData + " songs");
                        listData.add(info);
                        dbcursor.moveToNext();
                    }
                    dbcursor.close();
                    handler.sendEmptyMessage(ALBUM_DATA_DISPLAY);
                }
            }
        }).start();
    }

    public String getCurrentPosition(){
        return currentPosition;
    }

    public void setCurrentPosition(String str){
        currentPosition=str;
    }


   class MediaMusicAdapter extends BaseAdapter{
        private Context context;
        private List<Music>  infos;
        private String curPosition;

       public MediaMusicAdapter(Context context, List<Music> infos) {
           this.context=context;
           this.infos=infos;
       }

       @Override
       public int getCount() {
           return infos.size();
       }


       @Override
       public Object getItem(int position) {
           return infos.get(position);
       }


       @Override
       public long getItemId(int position) {
           return position;
       }


       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           ViewHolder holder;
           if(convertView==null){
               holder=new ViewHolder();
               convertView=View.inflate(context,R.layout.item_mediabrowser,null);
               holder.ivPic=(ImageView)convertView.findViewById(R.id.iv_pic);
               holder.tvTitle=(TextView)convertView.findViewById(R.id.item_songname);
               holder.tvSubTitle=(TextView)convertView.findViewById(R.id.item_artist);
               convertView.setTag(holder);
           }else{
               holder=(ViewHolder)convertView.getTag();
           }
              if(!currentPosition.equals(ARTIST_LIST)&&!currentPosition.equals(ALBUM_LIST)) {
               holder.ivPic.setVisibility(View.VISIBLE);
              }else{
                  holder.ivPic.setVisibility(View.INVISIBLE);
              }

           holder.tvTitle.setText(infos.get(position).getTitle());
           holder.tvSubTitle.setText(infos.get(position).getArtist());
           return convertView;

       }

       class ViewHolder{
           public ImageView  ivPic;
           public TextView   tvTitle;
           public TextView   tvSubTitle;
       }
   }


    class MediaBrowserConnection extends MediaBrowserHelper {

        public MediaBrowserConnection(Context context) {
            super(context, MediaPlaybackService.class);
        }

        @Override
        public void onConnected(MediaControllerCompat mMediaController) {
            super.onConnected(mMediaController);
        }

        @Override
        public void onChildrenLoaded(@NonNull String parentId, @NonNull List<MediaBrowserCompat.MediaItem> children) {
            //super.onChildrenLoaded(parentId, children);
            MediaControllerCompat mMediaController = getMediaController();

            for(MediaBrowserCompat.MediaItem mediaItem:children){
                LogUtil.LogDebug("info","mediabrowser......fragment.......mediaItem====="+children);
               mMediaController.addQueueItem(mediaItem.getDescription());
            }
            mMediaController.getTransportControls().prepare();
        }
    }

    class MediaBrowserListener extends MediaControllerCompat.Callback{
        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            super.onPlaybackStateChanged(state);
        }

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            if(metadata==null){
                return;
            }
            LogUtil.LogDebug("info","mediaBrowserfragment....metadata===="+metadata);
        }
    }
}
