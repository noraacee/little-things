package badtzmarupekkle.littlethings.interf;

import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;

import badtzmarupekkle.littlethings.widget.RefreshViewPagerTabs;

public interface Refresher {
    public void setOnRefreshListener(OnRefreshListener rListener);
    public void setRefreshViewPagerTabs(RefreshViewPagerTabs refreshViewPagerTabs);
    public void setRefreshing(boolean refreshing);
}
