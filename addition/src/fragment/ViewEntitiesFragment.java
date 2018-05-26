package badtzmarupekkle.littlethings.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.activity.BlogActivity;
import badtzmarupekkle.littlethings.util.ImageManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;
import badtzmarupekkle.littlethings.widget.RefreshListView;
import badtzmarupekkle.littlethings.widget.RefreshViewPagerTabs;

public abstract class ViewEntitiesFragment<E> extends Fragment implements OnRefreshListener {
    protected static final String TIMESTAMP_PATTERN_12_HOURS = "MMMM dd, YYYY - hh:mm aa";
    protected static final String TIMESTAMP_PATTERN_24_HOURS = "MMMM dd, YYYY - HH:mm";
    protected boolean loading;

    protected long timestamp;

    protected RefreshViewPagerTabs refreshViewPagerTabs;
    protected RefreshListView entitiesView;
    protected EntityAdapter adapter;

    protected BlogActivity activity;
    protected ImageManager iManager;
    protected SystemMessageManager smManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        activity = (BlogActivity)getActivity();

        loading = false;

        iManager = new ImageManager(activity);
        refreshViewPagerTabs = activity.getRefreshViewPagerTabs();
        smManager = activity.getSystemMessageManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getLayoutId(), container, false);
        entitiesView = (RefreshListView) rootView.findViewById(R.id.entities);

        entitiesView.setAdapter(adapter);
        entitiesView.setOnRefreshListener(this);
        entitiesView.setRefreshViewPagerTabs(refreshViewPagerTabs);

        return rootView;
    }

    @Override
    public void onRefresh() {
        timestamp = System.currentTimeMillis();
        getTask(true).execute();
    }

    public abstract View getCreateEntityView();

    protected abstract int getEntitiesLimit();

    protected abstract int getLayoutId();

    protected abstract GetEntitiesTask getTask(boolean refresh);

    protected abstract void setRefreshing(boolean refreshing);

    protected abstract void updateTimestamp();

    protected abstract class EntityAdapter extends BaseAdapter {
        protected BlogActivity activity;
        protected List<E> entities;

        public EntityAdapter(BlogActivity activity) {
            this.activity = activity;
            entities = new ArrayList<>();
        }

        @Override
        public int getCount() {
            return entities.size();
        }

        @Override
        public E getItem(int position) {
            return entities.get(position);
        }

        public void addAll(List<E> entities) {
            this.entities.addAll(entities);
        }

        public void clear() { entities.clear(); }

        public void insert(E e) {
            entities.add(0, e);
        }

        public void remove(int position) {
            entities.remove(position);
        }
    }

    protected abstract class GetEntitiesTask extends AsyncTask<Void, Void, EntityResponse> {

        boolean refresh;

        public GetEntitiesTask(boolean refresh) {
            this.refresh = refresh;
        }

        @Override
        protected void onPreExecute() {
            loading = true;
        }

        @Override
        protected void onPostExecute(EntityResponse response) {
            if (response != null) {
                if (response.success) {
                    if (response.entities != null) {
                        if(refresh)
                            adapter.clear();
                        adapter.addAll(response.entities);

                        if (getEntitiesLimit() == -1)
                            loading = false;
                        else if(response.entities.size() > 0 && response.entities.size() == getEntitiesLimit())
                            loading = false;
                        adapter.notifyDataSetChanged();
                        updateTimestamp();
                    }
                } else {
                    loading = false;
                    smManager.displayError(response.errorCode);
                }
            } else {
                loading = false;
                smManager.displayDefaultError();
            }

            setRefreshing(false);
        }
    }

    protected class EntityResponse {
        public boolean success;
        public int errorCode;
        public List<E> entities;

        public EntityResponse(boolean success, int errorCode, List<E> entities) {
            this.success = success;
            this.errorCode = errorCode;
            this.entities = entities;
        }
    }
}
