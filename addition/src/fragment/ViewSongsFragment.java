package badtzmarupekkle.littlethings.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.song.songendpoint.Songendpoint;
import com.littlethings.endpoint.song.songendpoint.model.Song;
import com.littlethings.endpoint.song.songendpoint.model.SongResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;

public class ViewSongsFragment extends ViewEntitiesFragment<Song> {
    private static final int LIMIT_SONGS_SIZE = 10;

    private static final String SONG_URL = "https://badtzmaru-pekkle-littlethings.appspot.com/serve?key=";

    private enum PlayerState {
        playing, paused, stopped
    }

    private boolean playLoading;
    private int currentPlaying;

    private Drawable pauseDrawable;
    private Drawable playDrawable;
    private ImageView currentPlay;
    private MediaPlayer player;
    private PlayerState state;
    private GradientDrawable playBackground;
    private Songendpoint endpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new SongsAdapter(activity);

        playLoading = false;
        currentPlaying = -1;
        state = PlayerState.stopped;
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                player.reset();
                currentPlay.setImageDrawable(playDrawable);
                state = PlayerState.stopped;
            }
        });

        pauseDrawable = getResources().getDrawable(R.drawable.pause);
        playDrawable = getResources().getDrawable(R.drawable.play);
        playBackground = (GradientDrawable) getResources().getDrawable(R.drawable.circle);
        playBackground.setColor(AppManager.getColor());

        Songendpoint.Builder builder = new Songendpoint.Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

        entitiesView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (playLoading)
                    return;
                switch(state) {
                    case playing:
                        currentPlay.setImageDrawable(playDrawable);
                        if (currentPlaying == position) {
                            state = PlayerState.paused;
                            player.pause();
                        } else {
                            state = PlayerState.stopped;
                            player.reset();
                            currentPlay = (ImageView) view.findViewById(R.id.play);
                            currentPlaying = position;
                            currentPlay.setImageDrawable(getResources().getDrawable(R.drawable.loading));

                            try {
                                player.setDataSource(SONG_URL + adapter.getItem(position).getKey());
                            } catch (IOException e) {
                                return;
                            }

                            new PlaySong().execute();
                        }
                        break;
                    case paused:
                        if (currentPlaying == position) {
                            state = PlayerState.playing;
                            currentPlay.setImageDrawable(pauseDrawable);
                            player.start();
                        } else {
                            playLoading = true;
                            currentPlay.setImageDrawable(pauseDrawable);
                            state = PlayerState.stopped;
                            player.reset();
                            currentPlay = (ImageView) view.findViewById(R.id.play);
                            currentPlaying = position;
                            currentPlay.setImageDrawable(getResources().getDrawable(R.drawable.loading));

                            try {
                                player.setDataSource(SONG_URL + adapter.getItem(position).getKey());
                            } catch (IOException e) {
                                return;
                            }

                            new PlaySong().execute();
                        }
                        break;
                    case stopped:
                        playLoading = true;
                        currentPlaying = position;
                        currentPlay = (ImageView) view.findViewById(R.id.play);
                        currentPlay.setImageDrawable(getResources().getDrawable(R.drawable.loading));

                        try {
                            player.setDataSource(SONG_URL + adapter.getItem(position).getKey());
                        } catch (IOException e) {
                            return;
                        }

                        new PlaySong().execute();
                        break;
                }
            }
        });

        entitiesView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final Song song = adapter.getItem(position);
                if (song.getWriter() != AppManager.getWriter())
                    return false;

                final Dialog dialog = new Dialog(getActivity());
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setContentView(R.layout.dialog_delete);

                int color = AppManager.getColor();

                ImageView accept = (ImageView) dialog.findViewById(R.id.accept);
                accept.setBackgroundColor(color);
                accept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new DeleteSongTask(position, dialog, song).execute();
                    }
                });

                ImageView cancel = (ImageView) dialog.findViewById(R.id.decline);
                cancel.setBackgroundColor(color);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                dialog.show();
                return true;
            }
        });

        entitiesView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!loading && firstVisibleItem + visibleItemCount == totalItemCount - 2) {
                    if (AppManager.checkNetworkConnection())
                        new GetSongsTask(false).execute();
                    else
                        smManager.displayError(SystemMessageManager.ERROR_NETWORK);
                }
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }
        });

        if(AppManager.checkNetworkConnection()) {
            entitiesView.setRefreshing(true);
            timestamp = System.currentTimeMillis();
            new GetSongsTask(false).execute();
        } else {
            smManager.displayError(SystemMessageManager.ERROR_NETWORK);
        }

        return rootView;
    }

    @Override
    public View getCreateEntityView() {
        return null;
    }

    @Override
    protected int getEntitiesLimit() {
        return LIMIT_SONGS_SIZE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_view_songs;
    }

    @Override
    protected GetEntitiesTask getTask(boolean refresh) {
        return new GetSongsTask(refresh);
    }

    @Override
    protected void setRefreshing(boolean refreshing) {
        entitiesView.setRefreshing(refreshing);
    }

    @Override
    protected void updateTimestamp() {
        timestamp = adapter.getItem(adapter.getCount() - 1).getTimestamp();
    }

    private class DeleteSongTask extends AsyncTask<Void,Void,SongResponse> {
        private int position;
        private Dialog dialog;
        private Song song;
        private ProgressDialog pDialog;

        public DeleteSongTask(int position, Dialog dialog, Song song) {
            this.position = position;
            this.dialog = dialog;
            this.song = song;
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Deleting...");
            pDialog.show();
        }

        @Override
        protected SongResponse doInBackground(Void... nothing) {
            song.setSecret(AppManager.getSecret());

            try {
                return endpoint.delete(song).execute();
            } catch(IOException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(SongResponse response) {
            pDialog.dismiss();
            dialog.dismiss();
            if(response!=null) {
                if(response.getSuccess()) {
                    adapter.remove(position);
                    adapter.notifyDataSetChanged();
                } else {
                    smManager.displayError(response.getErrorCode());
                }
            } else {
                smManager.displayDefaultError();
            }
        }
    }

    private class GetSongsTask extends GetEntitiesTask {

        public GetSongsTask(boolean refresh) {
            super(refresh);
        }

        @Override
        protected EntityResponse doInBackground(Void... params) {
            Song song = new Song();
            song.setSecret(AppManager.getSecret());
            song.setTimestamp(timestamp);

            try {
                SongResponse response = endpoint.get(song).execute();
                return new EntityResponse(response.getSuccess(), response.getErrorCode(), response.getSongs());
            } catch(IOException e) {
                return null;
            }
        }
    }

    private class PlaySong extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                player.prepare();
                return true;
            } catch (IOException e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean prepared) {
            if (prepared) {
                state = PlayerState.playing;
                player.start();
                currentPlay.setImageDrawable(pauseDrawable);
            }
            playLoading = false;
        }
    }

    private class SongsAdapter extends EntityAdapter {

        public SongsAdapter(BlogActivity activity) {
            super(activity);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.list_song, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.play = (ImageView) convertView.findViewById(R.id.play);
                holder.message = (TextView) convertView.findViewById(R.id.message);
                holder.timestamp = (TextView) convertView.findViewById(R.id.timestamp);
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
                    holder.play.setBackgroundDrawable(playBackground);
                else
                    holder.play.setBackground(playBackground);

                convertView.findViewById(R.id.divider).setBackgroundColor(AppManager.getColor());

                convertView.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder) convertView.getTag();
            final Song song = getItem(position);

            holder.message.setText(song.getMessage());

            DateTime dt = new DateTime(song.getTimestamp());
            DateTimeFormatter dtf;
            if(AppManager.is24Hour())
                dtf= DateTimeFormat.forPattern(TIMESTAMP_PATTERN_24_HOURS);
            else
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_12_HOURS);
            holder.timestamp.setText(dtf.print(dt));

            return convertView;
        }

        private class ViewHolder {
            public ImageView play;
            public TextView message;
            public TextView timestamp;
        }
    }
}
