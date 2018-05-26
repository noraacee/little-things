package badtzmarupekkle.littlethings.fragment;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.dialog.ShowGIFDialog;
import badtzmarupekkle.littlethings.image.VolleyImageView;
import badtzmarupekkle.littlethings.image.VolleyLruCache;
import badtzmarupekkle.littlethings.util.SystemMessageManager;
import badtzmarupekkles.littlethings.R;
import badtzmarupekkle.littlethings.image.VolleySingleton;
import badtzmarupekkle.littlethings.widget.GIFWebView;

import com.android.volley.toolbox.ImageLoader;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.rawr.rawrendpoint.Rawrendpoint;
import com.littlethings.endpoint.rawr.rawrendpoint.Rawrendpoint.Builder;
import com.littlethings.endpoint.rawr.rawrendpoint.model.Rawr;
import com.littlethings.endpoint.rawr.rawrendpoint.model.RawrResponse;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

public class ViewRawrsFragment extends ViewEntitiesFragment<Rawr> {
    private static final int LIMIT_RAWRS_SIZE = 5;

    private static final String TYPE_GIF = "gif";
    private static final String TYPE_JPG = "jpg";

    private ImageLoader iLoader;
    private Rawrendpoint endpoint;
    private ShowGIFDialog sgDialog;
    private VolleyLruCache imageCache;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new RawrsAdapter(activity);
        VolleySingleton instance = VolleySingleton.getInstance(getActivity());
        imageCache = instance.getLruCache();
        iLoader = instance.getImageLoader();

        sgDialog = new ShowGIFDialog();

        Builder builder = new Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = super.onCreateView(inflater, container, savedInstanceState);

        entitiesView.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (!loading && firstVisibleItem + visibleItemCount == totalItemCount - 2) {
                    if (AppManager.checkNetworkConnection())
                        new GetRawrsTask(false).execute();
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
            timestamp=System.currentTimeMillis();
            new GetRawrsTask(false).execute();
        } else {
            smManager.displayError(SystemMessageManager.ERROR_NETWORK);
        }

        return rootView;
    }

    @Override
    protected int getEntitiesLimit() {
        return LIMIT_RAWRS_SIZE;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_view_rawrs;
    }

    @Override
    protected GetEntitiesTask getTask(boolean refresh) {
        return new GetRawrsTask(refresh);
    }

    @Override
    protected void setRefreshing(boolean refreshing) {
        entitiesView.setRefreshing(refreshing);
    }

    @Override
    protected void updateTimestamp() {
        timestamp = adapter.getItem(adapter.getCount() - 1).getTimestamp();
    }

    private class GetRawrsTask extends GetEntitiesTask {

        public GetRawrsTask(boolean refresh) {
            super(refresh);
        }

        @Override
        protected EntityResponse doInBackground(Void... nothing) {
            Rawr rawr=new Rawr();
            rawr.setSecret(AppManager.getSecret());
            rawr.setTimestamp(timestamp);

            try {
                RawrResponse response = endpoint.getRawrs(rawr).execute();
                return new EntityResponse(response.getSuccess(), response.getErrorCode(), response.getRawrs());
            } catch (IOException e) {
                return null;
            }
        }
    }

    private class RawrsAdapter extends EntityAdapter {
        private GradientDrawable playBackground;

        public RawrsAdapter(BlogActivity activity) {
            super(activity);
            playBackground = (GradientDrawable) getResources().getDrawable(R.drawable.circle);
            playBackground.setColor(AppManager.getColor());
        }

        @Override
        public long getItemId(int position) {
            return entities.get(position).getId();
        }

        @SuppressWarnings("deprecation")
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.list_rawr, parent, false);
                ViewHolder holder = new ViewHolder();
                holder.gifView = (GIFWebView) convertView.findViewById(R.id.gif);
                holder.imageView = (VolleyImageView) convertView.findViewById(R.id.image);
                holder.titleView = (TextView) convertView.findViewById(R.id.title);
                holder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);

                    ImageView playView = (ImageView) convertView.findViewById(R.id.play);
                if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
                    playView.setBackgroundDrawable(playBackground);
                else
                    playView.setBackground(playBackground);
                holder.gifView.init(getActivity(), sgDialog, imageCache, (ImageView) convertView.findViewById(R.id.first_image), playView);
                holder.imageView.setLruCache(imageCache);

                convertView.findViewById(R.id.divider).setBackgroundColor(AppManager.getColor());
                convertView.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder)convertView.getTag();
            final Rawr rawr = getItem(position);
            String title = rawr.getTitle();
            if(title != null && title.length() > 0) {
                holder.titleView.setText(title);
                holder.titleView.setVisibility(View.VISIBLE);
            } else {
                holder.titleView.setText("");
                holder.titleView.setVisibility(View.GONE);
            }

            DateTime timestamp = new DateTime(rawr.getTimestamp());
            DateTimeFormatter dtf;
            if(AppManager.is24Hour())
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_24_HOURS);
            else
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_12_HOURS);
            holder.timestampView.setText(dtf.print(timestamp));

            final String rawrUrl=rawr.getUrl();
            if (rawr.getType().equals(TYPE_GIF)) {
                holder.imageView.setVisibility(View.GONE);
                holder.gifView.setFirstImage(rawrUrl);
            } else if (rawr.getType().equals(TYPE_JPG)) {
                holder.gifView.setVisibility(true, View.GONE);
                holder.imageView.setImageUrl(rawrUrl, iLoader);
                holder.imageView.setVisibility(View.VISIBLE);
            }

            return convertView;
        }

        private class ViewHolder {
            public GIFWebView gifView;
            public VolleyImageView imageView;
            public TextView titleView;
            public TextView timestampView;
        }
    }
}
