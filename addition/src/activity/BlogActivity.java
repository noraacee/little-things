package badtzmarupekkle.littlethings.activity;

import android.annotation.SuppressLint;
//import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
//import android.view.ViewTreeObserver;
import android.widget.TextView;

import badtzmarupekkle.littlethings.R;
import badtzmarupekkle.littlethings.application.AppManager;
import badtzmarupekkle.littlethings.application.LittleThingsApplication;
import badtzmarupekkle.littlethings.fragment.ViewEntitiesFragment;
//import badtzmarupekkle.littlethings.fragment.ViewPostsFragment;
import badtzmarupekkle.littlethings.fragment.ViewRawrsFragment;
import badtzmarupekkle.littlethings.fragment.ViewSongsFragment;
//import badtzmarupekkle.littlethings.fragment.ViewTimeSlotsFragment;
import badtzmarupekkle.littlethings.util.ImageManager;
import badtzmarupekkle.littlethings.util.SystemMessageManager;
import badtzmarupekkle.littlethings.widget.QuickReturnViewPager;
import badtzmarupekkle.littlethings.widget.RefreshViewPagerTabs;

public class BlogActivity extends FragmentActivity {
    private static final int COUNT_FRAGMENTS = 2;
    //private static final int POSITION_VIEW_POSTS = 2;
    private static final int POSITION_VIEW_RAWRS = 1;
    //private static final int POSITION_VIEW_TIME_SLOTS = 3;
    private static final int POSITION_VIEW_SONGS = 0;

    private static final String KEY_PAGE = "page";

    private RefreshViewPagerTabs vpTabs;
    private QuickReturnViewPager vPager;

    private ImageManager iManager;
    private SystemMessageManager smManager;
    //private ViewPostsFragment vpFragment;
    private ViewRawrsFragment vrFragment;
    //private ViewTimeSlotsFragment vtsFragment;
    private ViewSongsFragment vsFragment;

    @SuppressWarnings("deprecation")
    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog);
        vpTabs = (RefreshViewPagerTabs) findViewById(R.id.view_pager_tabs);
        vPager = (QuickReturnViewPager) findViewById(R.id.view_pager);

        iManager = new ImageManager(this);
        smManager = new SystemMessageManager(this, (TextView) findViewById(R.id.system_message));
        AppManager.setActivity(this);

        vPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager()));
        vPager.setQuickReturnUpView(vpTabs);
        vPager.setOffscreenPageLimit(1);
        if (savedInstanceState != null)
            vPager.setCurrentItem(savedInstanceState.getInt(KEY_PAGE), true);

        /*vPager.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @SuppressLint("NewApi")
            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    vPager.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    vPager.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }

                vPager.addQuickReturnDownView(vpFragment.getCreateEntityView());
            }
        });*/

        vpTabs.setBackgroundColor(AppManager.getColor());
        vpTabs.setViewPager(vPager);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LittleThingsApplication.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppManager.setActivity(this);
        LittleThingsApplication.onResume();
        if (iManager == null)
            iManager = new ImageManager(this);
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
                /*case POSITION_VIEW_POSTS:
                    if (vpFragment == null)
                        vpFragment = new ViewPostsFragment();
                    return vpFragment;*/
                case POSITION_VIEW_RAWRS:
                    if (vrFragment == null)
                        vrFragment = new ViewRawrsFragment();
                    return vrFragment;
                /*case POSITION_VIEW_TIME_SLOTS:
                    if (vtsFragment == null)
                        vtsFragment = new ViewTimeSlotsFragment();
                    return vtsFragment;*/
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
                /*case POSITION_VIEW_POSTS:
                    return R.drawable.post;*/
                case POSITION_VIEW_RAWRS:
                    return R.drawable.rawr;
                /*case POSITION_VIEW_TIME_SLOTS:
                    return R.drawable.calendar;*/
                case POSITION_VIEW_SONGS:
                    return R.drawable.rawr;
            }
            return 0;
        }
    }
}
