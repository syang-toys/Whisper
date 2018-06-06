package com.syang.whisper.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.syang.whisper.R;
import com.syang.whisper.WhisperApplication;
import com.syang.whisper.adapter.PagerAdapter;
import com.syang.whisper.fragment.AddFragment;
import com.syang.whisper.fragment.FriendsFragment;

public class MainActivity extends AppCompatActivity {

    private WhisperApplication app;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
        }

        app = (WhisperApplication)getApplication();
        app.setMainActivity(this);

        app.bindSocketEvents();
        app.connectSocket();
        app.emitOnline();

        initToolbar();
        initViewPagerAndTabs();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        app.emitOffline();
        app.unbindSocketEvents();
    }

    private void initToolbar() {
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.title));
        mToolbar.setTitleTextColor(Color.WHITE);
    }

    private void initViewPagerAndTabs() {
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new FriendsFragment(), getString(R.string.tab_friends));
        pagerAdapter.addFragment(new AddFragment(), getString(R.string.tab_add));
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    public void notifyFriendsUpdate() {
        FriendsFragment friendsFragment = (FriendsFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.tab_friends));
        if (friendsFragment != null) {
            friendsFragment.notifyFriendsUpdate();
        }
    }

    public void notifyPendingFriendsUpdate() {
        FriendsFragment friendsFragment = (FriendsFragment) getSupportFragmentManager().findFragmentByTag(getString(R.string.tab_friends));
        friendsFragment.notifyFriendsUpdate();
    }
}
