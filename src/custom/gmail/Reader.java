package custom.gmail;

import android.util.Log;
import custom.gmail.exceptions.NoConnectionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
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
        if (this.connector.connect()) {

            Folder inbox = this.connector.getStore().getFolder(type);
            inbox.open(Folder.READ_ONLY);

            Integer inboxFrom   = getFrom(inbox.getMessageCount(), count, lastId);
            Integer inboxTo     = getTo(inbox.getMessageCount(), count, lastId);

            Log.e(TAG, ((Integer)inbox.getMessageCount()).toString());
            Log.e(TAG, inboxFrom.toString());
            Log.e(TAG, inboxTo.toString());

            Message[] messages = inbox.getMessages(inboxFrom, inboxTo);
            List<Message> list = Arrays.asList(messages);
            Collections.reverse(list);

            return list;

        } else {
            throw new NoConnectionException("No connection, check your gmail email or password");
        }
    }

    protected int getFrom(int messagesCount, int count, int lastId) {
        return lastId == 0 ? messagesCount - count + 1: lastId - count;
    }

    protected int getTo(int messagesCount, int count, int lastId) {
        return lastId == 0 ? messagesCount : lastId - 1;
    }
}
