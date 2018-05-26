package badtzmarupekkle.littlethings.fragment;

import java.io.IOException;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;
import badtzmarupekkle.littlethings.util.ImageManager.AdapterType;
import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.util.VolleyLruCache;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.littlethings.endpoint.rawr.rawrendpoint.Rawrendpoint;
import com.littlethings.endpoint.rawr.rawrendpoint.Rawrendpoint.Builder;
import com.littlethings.endpoint.rawr.rawrendpoint.model.Rawr;
import com.littlethings.endpoint.rawr.rawrendpoint.model.RawrResponse;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView;
import android.widget.TextView;

public class ViewRawrsFragment extends ViewEntitiesFragment<Rawr> {
    private static final int LIMIT_RAWRS_SIZE = 5;

    private static final String TYPE_GIF = "gif";
    private static final String TYPE_JPG = "jpg";

    private ImageLoader iLoader;
    private Rawrendpoint endpoint;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new RawrsAdapter(activity);
        iManager.addAdapter(AdapterType.VIEW_RAWRS, adapter);
        iLoader = new ImageLoader(Volley.newRequestQueue(activity), new VolleyLruCache());

        Builder builder = new Builder(AndroidHttp.newCompatibleTransport(), new JacksonFactory(), null);
        endpoint = builder.build();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = super.onCreateView(inflater, container, savedInstanceState);

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
            //SMMANAGER NOT SET BEFORE CREATEVIEW WHEN NO NETWORK CONNECTION - DIANE'S PHONE
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

        public RawrsAdapter(BlogActivity activity) {
            super(activity);
        }

        @Override
        public long getItemId(int position) {
            return entities.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView==null) {
                convertView=activity.getLayoutInflater().inflate(R.layout.list_rawr, parent, false);
                ViewHolder holder=new ViewHolder();
                holder.gifView = (WebView) convertView.findViewById(R.id.gif);
                holder.imageView = (NetworkImageView) convertView.findViewById(R.id.image);
                holder.titleView = (TextView) convertView.findViewById(R.id.title);
                holder.timestampView = (TextView) convertView.findViewById(R.id.timestamp);

                convertView.findViewById(R.id.divider).setBackgroundColor(AppManager.getColor());
                convertView.setTag(holder);
            }

            final ViewHolder holder = (ViewHolder)convertView.getTag();
            final Rawr rawr = getItem(position);
            String title = rawr.getTitle();
            if(title!=null && title.length()>0) {
                holder.titleView.setText(title);
                holder.titleView.setVisibility(View.VISIBLE);
            } else {
                holder.titleView.setText("");
                holder.titleView.setVisibility(View.GONE);
            }

            DateTime timestamp=new DateTime(rawr.getTimestamp());
            DateTimeFormatter dtf;
            if(AppManager.is24Hour())
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_24_HOURS);
            else
                dtf=DateTimeFormat.forPattern(TIMESTAMP_PATTERN_12_HOURS);
            holder.timestampView.setText(dtf.print(timestamp));

            /*if (holder.imageView.getDrawable()!= null) {
                holder.imageView.setImageBitmap(null);
                holder.imageView.setVisibility(View.GONE);
                iManager.removeImageFromDisplay(holder.id);
            }*/

            String rawrUrl=rawr.getUrl();
            if(rawrUrl != null && rawrUrl.length() > 0) {
                if (rawr.getType().equals(TYPE_GIF)) {
                    holder.gifView.loadUrl(rawrUrl);
                    holder.gifView.setVisibility(View.VISIBLE);
                } else if (rawr.getType().equals(TYPE_JPG)) {
                    holder.gifView.loadUrl(null);
                    holder.gifView.setVisibility(View.GONE);
                    //iManager.loadImageInto(AdapterType.VIEW_RAWRS, holder.imageView, rawr.getId(), rawrUrl);
                    holder.imageView.setImageUrl(rawrUrl, iLoader);
                }
            }

            holder.id=rawr.getId();

            return convertView;
        }

        private class ViewHolder {
            public long id;
            public WebView gifView;
            public NetworkImageView imageView;
            public TextView titleView;
            public TextView timestampView;
        }
    }
}
