package com.example.gmail;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import com.example.gmail.proxies.GmailDataProxy;
import custom.gmail.Connector;
import custom.gmail.Reader;

public class MainActivity extends Activity {

    GmailDataProxy gmailData;
    ListView messagesListView;
    SimpleCursorAdapter adapter;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

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

        // creates listview and sets adapter from gmail proxy
        messagesListView = (ListView) findViewById(R.id.messages_list);
        View v = getLayoutInflater().inflate(R.layout.list_view_footer, null);
        messagesListView.addFooterView(v);

        adapter = new SimpleCursorAdapter(MainActivity.this,
                android.R.layout.simple_list_item_1, null,
                new String[] { GmailDataProxy.DatabaseHelper.FIELD_SUBJECT },
                new int[] { android.R.id.text1 });
        messagesListView.setAdapter(adapter);

        // creates imap gmail connector
        Connector connector = new Connector(email, password);
        // creates gmail adapter proxy and move cursor to the first page
        gmailData = new GmailDataProxy(connector, MainActivity.this, Reader.MESSAGE_TYPE_INBOX);

        findViewById(R.id.list_view_load_more).setVisibility(View.GONE);
        findViewById(R.id.list_view_progress).setVisibility(View.VISIBLE);
        new GmailLoader().execute("first_page");

        final Button button = (Button) findViewById(R.id.list_view_load_more);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                findViewById(R.id.list_view_load_more).setVisibility(View.GONE);
                findViewById(R.id.list_view_progress).setVisibility(View.VISIBLE);
                new GmailLoader().execute("next_page");
            }
        });
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
                new GmailLoader().execute("update");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private class GmailLoader extends AsyncTask<Object, Void, Cursor> {

        @Override
        protected Cursor doInBackground(Object... params) {
            String type = (String) params[0];
            Cursor cursor;

            if (type == "update") {
                cursor = gmailData.update();
            } else if(type == "next_page") {
                cursor = gmailData.nextPage();
            } else {
                cursor = gmailData.firstPage();
            }

            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor cursor) {
            if (cursor != null) {
                adapter.changeCursor(cursor);
                adapter.notifyDataSetInvalidated();
                findViewById(R.id.list_view_load_more).setVisibility(View.VISIBLE);
                findViewById(R.id.list_view_progress).setVisibility(View.GONE);
            }
        }
    }
}
