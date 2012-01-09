package custom.gmail;

import android.util.Log;
import custom.gmail.exceptions.NoConnectionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Read gmail inbox/sent messages
 */
public class Reader {

    final private static String TAG = "Reader";
    final public static String MESSAGE_TYPE_INBOX = "inbox";
    final public static String MESSAGE_TYPE_SENT  = "sent";

    protected Connector connector;
    
    public Reader(Connector connector) {
        this.connector = connector;
    }
    
    public List<Message> getMessages(String type, int lastId, int count) throws NoConnectionException, MessagingException {

        if (!this.connector.connect()) {
            throw new NoConnectionException("No connection, check your gmail email or password");
        }

        Folder folder = this.connector.getStore().getFolder(type);
        folder.open(Folder.READ_ONLY);

        Integer inboxFrom   = getFrom(folder.getMessageCount(), count, lastId);
        Integer inboxTo     = getTo(folder.getMessageCount(), count, lastId);

        Message[] messages = folder.getMessages(inboxFrom, inboxTo);
        List<Message> list = Arrays.asList(messages);
        Collections.reverse(list);

        return list;

    }

    public List<Message> getNewMessages(int firstId, String type) throws NoConnectionException, MessagingException {

        if (!this.connector.connect()) {
            throw new NoConnectionException("No connection, check your gmail email or password");
        }

        List<Message> list = new ArrayList<Message>();

        Folder folder = this.connector.getStore().getFolder(type);
        folder.open(Folder.READ_ONLY);

        if (folder.getMessageCount() > firstId) {
            list = getMessages(type, 0, folder.getMessageCount() - firstId);
        }
        Log.e(TAG, ((Integer)folder.getMessageCount()).toString());
        Log.e(TAG, ((Integer)firstId).toString());

        return list;
    }


    protected int getFrom(int messagesCount, int count, int lastId) {
        int from = lastId == 0 ? messagesCount - count + 1: lastId - count;
        return from <= 0 ? 1 : from;
    }

    protected int getTo(int messagesCount, int count, int lastId) {
        return lastId == 0 ? messagesCount : lastId - 1;
    }
}
