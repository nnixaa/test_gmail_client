package com.example.gmail;

import android.app.ActivityGroup;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

/**
 */
public class MainActivity extends ActivityGroup {

    private TabHost mTabHost;

    private void setupTabHost() {
        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup(this.getLocalActivityManager());
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_content);

        SharedPreferences settings = MainActivity.this.getSharedPreferences(SettingsActivity.PREF_NAME, 0);
        String email    = settings.getString(SettingsActivity.PREF_EMAIL_KEY, "");
        String password = settings.getString(SettingsActivity.PREF_PASS_KEY, "");

        if (email.length() == 0 || password.length() == 0) {
            Toast.makeText(MainActivity.this, getString(R.string.main_need_credentials), Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        setupTabHost();
        Intent intent;

        intent = new Intent().setClass(this, InboxActivity.class);
        setupTab(intent, "Tab 1");

        intent = new Intent().setClass(this, OutboxActivity.class);
        setupTab(intent, "Tab 2");

    }

    private void setupTab(final Intent intent, final String tag) {
        View tabview = createTabView(mTabHost.getContext(), tag);

        TabHost.TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
        mTabHost.addTab(setContent);

    }

    private static View createTabView(final Context context, final String text) {
        View view = LayoutInflater.from(context).inflate(R.layout.tab_indicator, null);
        TextView tv = (TextView) view.findViewById(R.id.tab_title);
        tv.setText(text);
        return view;
    }

}