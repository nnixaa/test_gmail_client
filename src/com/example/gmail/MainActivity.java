package com.example.gmail;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import com.example.gmail.proxies.GmailProxy;
import custom.gmail.Connector;
import custom.gmail.Reader;

public class MainActivity extends Activity
{

    GmailProxy gmail;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

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

        ListView messagesListView = (ListView) findViewById(R.id.messages_list);

        Connector connector = new Connector(email, password);

        gmail = new GmailProxy(connector, MainActivity.this, Reader.MESSAGE_TYPE_INBOX);
        gmail.firstPage(messagesListView, android.R.layout.simple_list_item_1, android.R.id.text1);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_update:
                gmail.update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
