package com.example.gmail;

import android.app.Activity;
import android.os.Bundle;
import android.util.Xml;
import android.webkit.WebView;
import android.widget.TextView;

/**
 */
public class MessageActivity extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_detail);
        
        Bundle extras = getIntent().getExtras();

        Integer id = extras.getInt("id");
        String subject = extras.getString("subject");
        String from = extras.getString("from");
        String message = extras.getString("message");
        String date = extras.getString("date");

        if (extras != null && id > 0) {

            TextView subjectView = (TextView) findViewById(R.id.message_subject_id);
            subjectView.setText(subject);

            TextView fromView = (TextView) findViewById(R.id.message_from_id);
            fromView.setText(from);

            TextView dateView = (TextView) findViewById(R.id.message_date_id);
            dateView.setText(date);

            WebView messageView = (WebView) findViewById(R.id.message_content_id);
            messageView.loadData(message, "text/html", "utf8");
        }

    }
}