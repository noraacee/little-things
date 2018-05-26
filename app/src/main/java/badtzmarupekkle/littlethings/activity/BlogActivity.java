package badtzmarupekkle.littlethings.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.widget.TextView;

import badtzmarupekkles.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.fragment.ViewEntitiesFragment;
import badtzmarupekkle.littlethings.fragment.ViewRawrsFragment;
import badtzmarupekkle.littlethings.fragment.ViewSongsFragment;
import badtzmarupekkle.littlethings.util.SystemMessageManager;
import badtzmarupekkle.littlethings.widget.QuickReturnViewPager;
import badtzmarupekkle.littlethings.widget.RefreshViewPagerTabs;

public class BlogActivity extends FragmentActivity {
    private static final int COUNT_FRAGMENTS = 2;
    private static final int POSITION_VIEW_SONGS = 0;
    private static final int POSITION_VIEW_RAWRS = 1;

    private static final String KEY_PAGE = "page";

    private RefreshViewPagerTabs vpTabs;
    private QuickReturnViewPager vPager;

    private SystemMessageManager smManager;
    private ViewRawrsFragment vrFragment;
    private ViewSongsFragment vsFragment;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        vpTabs = (RefreshViewPagerTabs) findViewById(R.id.view_pager_tabs);
        vPager = (QuickReturnViewPager) findViewById(R.id.view_pager);

        smManager = new SystemMessageManager(this, (TextView) findViewById(R.id.system_message));
        AppManager.setActivity(this);

        vPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        vPager.setQuickReturnUpView(vpTabs);
        vPager.setOffscreenPageLimit(1);
        if (savedInstanceState != null)
            vPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGE), true);

        vpTabs.setBackgroundColor(AppManager.getColor());
        vpTabs.setViewPager(vPager);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppManager.setActivity(this);
        if (smManager == null)
            smManager = new SystemMessageManager(this, (TextView) findViewById(R.id.system_message));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_PAGE, vPager.getCurrentItem());
    }

    public RefreshViewPagerTabs getRefreshViewPagerTabs() {
        return vpTabs;
    }

    public SystemMessageManager getSystemMessageManager() {
        return smManager;
    }

    private class ViewPagerAdapter extends FragmentPagerAdapter implements RefreshViewPagerTabs.IconTabProvider {
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public ViewEntitiesFragment getItem(int position) {
            switch (position) {
                case POSITION_VIEW_RAWRS:
                    if (vrFragment == null)
                        vrFragment = new ViewRawrsFragment();
                    return vrFragment;
                case POSITION_VIEW_SONGS:
                    if (vsFragment == null)
                        vsFragment = new ViewSongsFragment();
                    return vsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return COUNT_FRAGMENTS;
        }

        @Override
        public int getTabIconResId(int position) {
            switch (position) {
                case POSITION_VIEW_RAWRS:
                    return R.drawable.rawr;
                case POSITION_VIEW_SONGS:
                    return R.drawable.song;
            }
            return 0;
        }
    }
}
