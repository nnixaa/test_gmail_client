package com.example.gmail.proxies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import custom.gmail.Connector;
import custom.gmail.Reader;
import custom.gmail.exceptions.NoConnectionException;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Working with gmail
 */
public class GmailProxy {
    final private static String TAG = "GmailProxy";

    private DatabaseHelper databaseHelper;
    private Context context;
    private Connector connector;

    private int lastId  = 0;
    private int count   = 10;
    private int page    = 1;

    private String type;

    private SimpleCursorAdapter adapter;

    public GmailProxy(Connector connector, Context context, String type) {
        this.connector      = connector;
        this.context        = context;
        this.type           = type;
        this.databaseHelper = new DatabaseHelper(context);
    }

    public void nextPage() {
        page ++;
        Cursor c = getMessages();

        adapter.changeCursor(c);
        adapter.notifyDataSetInvalidated();
    }

    public void firstPage(ListView listView, int layout, int subject_field) {
        Cursor c = getMessages();

        adapter = new SimpleCursorAdapter(context,
                layout, c,
                new String[] { DatabaseHelper.FIELD_SUBJECT },
                new int[] { subject_field });

        listView.setAdapter(adapter);
    }

    public Cursor getMessages() {

        Cursor cursor = getMessagesFromDb(getPage(), false);
        if (cursor.getCount() == 0) {
            // if empty, try to load messages from network
            getMessagesFromGmail(getLastId(), getCount());
        }
        // reloads cursor from db
        cursor = getMessagesFromDb(getPage(), true);

        cursor.moveToLast();
        lastId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FIELD_ID));
        cursor.moveToFirst();

        return cursor;
    }

    private int getMessagesFromGmail(int lastId, int count) {

        List<Message> messages = new ArrayList<Message>();
        Reader reader = new Reader(this.connector);
        try {
            messages = reader.getMessages(type, lastId, count);

            ContentValues cv = new ContentValues();
            for (Message m : messages) {
                cv.put(DatabaseHelper.FIELD_ID, m.getMessageNumber());
                cv.put(DatabaseHelper.FIELD_SUBJECT, m.getSubject());
                cv.put(DatabaseHelper.FIELD_TYPE, type);

                databaseHelper.getWritableDatabase().insert(databaseHelper.TABLE_NAME, null, cv);

            }

        } catch (NoConnectionException e) {
            Log.e(TAG, e.toString());
        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
        }
        databaseHelper.getWritableDatabase().close();
        return messages.size();
    }

    private Cursor getMessagesFromDb(Integer page, boolean all) {
        String where = databaseHelper.FIELD_TYPE + " = ? ";
        String[] selectionArgs = {type};
        Integer count = page * getCount();
        Integer from = 0;

        if (!all) {
            from = page * getCount() - getCount();
        }
        String limit = from.toString() + "," + count.toString();

        return databaseHelper.getWritableDatabase().query(databaseHelper.TABLE_NAME, databaseHelper.COLUMNS, where, selectionArgs, null, null, "_id DESC", limit);
    }

    public int getCount() {
        return count;
    }

    public int getPage() {
        return page;
    }

    private int getLastId() {
        return lastId;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {

        final private static String DATABASE_NAME = "custom_gmail_client.db";
        final private static int DATABASE_VERSION = 1;

        final public static String TABLE_NAME      = "messages";

        final public static String FIELD_ID         = "_id";
        final public static String FIELD_TYPE       = "type";
        final public static String FIELD_SUBJECT    = "subject";
        final public static String FIELD_FROM       = "sender";
        final public static String FIELD_TO         = "recipient";
        final public static String FIELD_MESSAGE    = "message";
        final public static String FIELD_DATE       = "date";

        final public static String[] COLUMNS = {FIELD_ID, FIELD_TYPE, FIELD_SUBJECT,
                                                FIELD_FROM, FIELD_TO, FIELD_MESSAGE, FIELD_DATE};

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
//            context.deleteDatabase(DATABASE_NAME);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            String sql = "CREATE TABLE " + TABLE_NAME + " ("
                    + FIELD_ID + " INTEGER PRIMARY KEY, " + FIELD_SUBJECT + " TEXT, "
                    + FIELD_FROM + " TEXT, " + FIELD_TO + " TEXT, " + FIELD_MESSAGE + " TEXT, "
                    + FIELD_DATE + " TEXT, " + FIELD_TYPE + " INTEGER);";

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }
}
