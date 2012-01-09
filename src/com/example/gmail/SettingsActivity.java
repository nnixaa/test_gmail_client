package com.example.gmail;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends Activity {

    final public static String PREF_NAME       = "gmail_account";
    final public static String PREF_EMAIL_KEY  = "gmail_email";
    final public static String PREF_PASS_KEY   = "gmail_password";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        SharedPreferences settings = SettingsActivity.this.getSharedPreferences(PREF_NAME, 0);

        final EditText email = (EditText) findViewById(R.id.settings_email_input);
        email.setText(settings.getString(PREF_EMAIL_KEY, ""));

        final EditText password = (EditText) findViewById(R.id.settings_password_input);
        password.setText(settings.getString(PREF_PASS_KEY, ""));

        final Button button = (Button) findViewById(R.id.settings_button_submit);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String emailValue = email.getText().toString();
                String passwordValue = password.getText().toString();

                if (email.length() == 0 || password.length() == 0) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.main_need_credentials), Toast.LENGTH_SHORT).show();
                } else {

                    SharedPreferences settings = SettingsActivity.this.getSharedPreferences(PREF_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();

                    editor.putString(PREF_EMAIL_KEY, emailValue);
                    editor.putString(PREF_PASS_KEY, passwordValue);
                    editor.commit();

                    Intent intent = new Intent(SettingsActivity.this, InboxActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}