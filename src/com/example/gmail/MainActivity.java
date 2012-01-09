package com.example.gmail;

import android.app.TabActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.Toast;

/**
 */
public class MainActivity extends TabActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        Resources resources = getResources();
        TabHost tabHost = getTabHost();

        TabHost.TabSpec tabSpec;
        Intent intent;

        // Timeline acivity, default
        intent = new Intent().setClass(this, InboxActivity.class);
        tabSpec = tabHost.newTabSpec("plans").setIndicator(getString(R.string.app_tab_one))
                .setContent(intent);
        tabHost.addTab(tabSpec);

        // Public activity
        intent = new Intent().setClass(this, OutboxActivity.class);
        tabSpec = tabHost.newTabSpec("explore").setIndicator(getString(R.string.app_tab_two))
                .setContent(intent);
        tabHost.addTab(tabSpec);

        tabHost.setCurrentTab(0);
    }
}