package com.example.gmail.proxies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.SimpleCursorAdapter;
import custom.gmail.Connector;
import custom.gmail.Reader;
import custom.gmail.exceptions.NoConnectionException;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Working with gmail
 */
public class GmailDataProxy {
    final private static String TAG = "GmailDataProxy";

    private DatabaseHelper databaseHelper;
    private Connector connector;

    private int lastId  = 0;
    private int firstId = 0;
    private int count   = 10;
    private int page    = 1;

    private String type;

    private SimpleCursorAdapter adapter;

    public GmailDataProxy(Connector connector, Context context, String type) {
        this.connector      = connector;
        this.type           = type;
        this.databaseHelper = new DatabaseHelper(context);
    }

    /**
     * Move the cursor to the first page
     * @return Cursor
     */
    public Cursor firstPage() {
        Cursor cursor = getMessages();
        cursor.moveToFirst();
        firstId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FIELD_ID));
        return cursor;
    }

    /**
     * Moves the cursor to the next page
     * @return Cursor
     */
    public Cursor nextPage() {
        page ++;
        Cursor cursor = getMessages();
        return cursor;
    }

    /**
     * Updates the cursor from top
     * @return Cursor
     */
    public Cursor update() {
        if (getNewMessagesFromGmail(getFirstId()) > 0) {

            Cursor cursor = getMessagesFromDb(getPage(), true);
            cursor.moveToFirst();
            firstId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.FIELD_ID));
            return cursor;
        }
        return null;
    }

    /*--------------------------------------------------------------------*/

    /**
     * Return messages from gmail or database based on current page
     * @return Cursor
     */
    private Cursor getMessages() {

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

    /**
     * Saves messages from gmail
     * @param lastId
     * @param count
     * @return int
     */
    private int getMessagesFromGmail(int lastId, int count) {

        List<Message> messages = new ArrayList<Message>();
        Reader reader = new Reader(this.connector);
        try {
            messages = reader.getMessages(type, lastId, count);

            ContentValues cv = new ContentValues();
            for (Message m : messages) {
                cv.put(DatabaseHelper.FIELD_ID, m.getMessageNumber());
                cv.put(DatabaseHelper.FIELD_SUBJECT, m.getSubject());
                cv.put(DatabaseHelper.FIELD_FROM, m.getFrom()[0].toString());
                cv.put(DatabaseHelper.FIELD_TO, m.getReplyTo()[0].toString());
                cv.put(DatabaseHelper.FIELD_TYPE, type);

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyy", Locale.getDefault());
                cv.put(DatabaseHelper.FIELD_DATE, formatter.format(m.getSentDate()));
                cv.put(DatabaseHelper.FIELD_MESSAGE, getMessageBody(m));

                databaseHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_NAME, null, cv);
            }

        } catch (NoConnectionException e) {
            Log.e(TAG, e.toString());
        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
        }

        databaseHelper.getWritableDatabase().close();
        return messages.size();
    }

    /**
     * Saves new messages from gmail
     * @param firstId
     * @return int
     */
    private int getNewMessagesFromGmail(int firstId) {

        List<Message> messages = new ArrayList<Message>();
        Reader reader = new Reader(this.connector);
        try {
            messages = reader.getNewMessages(firstId, type);

            ContentValues cv = new ContentValues();
            for (Message m : messages) {
                cv.put(DatabaseHelper.FIELD_ID, m.getMessageNumber());
                cv.put(DatabaseHelper.FIELD_SUBJECT, m.getSubject());
                cv.put(DatabaseHelper.FIELD_FROM, m.getFrom()[0].toString());
                cv.put(DatabaseHelper.FIELD_TO, m.getReplyTo()[0].toString());
                cv.put(DatabaseHelper.FIELD_TYPE, type);

                SimpleDateFormat formatter = new SimpleDateFormat("dd MMM yyy", Locale.getDefault());
                cv.put(DatabaseHelper.FIELD_DATE, formatter.format(m.getSentDate()));
                cv.put(DatabaseHelper.FIELD_MESSAGE, getMessageBody(m));

                databaseHelper.getWritableDatabase().insert(DatabaseHelper.TABLE_NAME, null, cv);
            }

        } catch (NoConnectionException e) {
            Log.e(TAG, e.toString());
        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
        }
        databaseHelper.getWritableDatabase().close();
        return messages.size();
    }

    /**
     * Returns specified page from database
     * @param page
     * @param all
     * @return Cursor
     */
    private Cursor getMessagesFromDb(Integer page, boolean all) {
        String where = DatabaseHelper.FIELD_TYPE + " = ? ";
        String[] selectionArgs = {type};

        Integer count = page * getCount();
        Integer from = 0;

        if (!all) {
            from = page * getCount() - getCount();
        }
        String limit = from.toString() + "," + count.toString();

        return databaseHelper.getWritableDatabase().query(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMNS, where, selectionArgs, null, null, "_id DESC", limit);
    }

    private int getCount() {
        return count;
    }

    private int getPage() {
        return page;
    }

    private int getLastId() {
        return lastId;
    }
    
    private int getFirstId() {
        return firstId;
    }

    private String getMessageBody(Part message) {

        String content = "";
        try {
            Object o = message.getContent();
            if (o instanceof String) {
                content = (String) o;
            } else if (o instanceof Multipart) {
                Multipart mp = (Multipart) o;
                int count = mp.getCount();
                for (int i = 0; i < count; i++) {
                    content = getMessageBody(mp.getBodyPart(i));
                }
            }

        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, e.toString());
        }
        return content;
    }
    
    public static class DatabaseHelper extends SQLiteOpenHelper {

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
