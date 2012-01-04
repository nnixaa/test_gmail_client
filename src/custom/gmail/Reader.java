package custom.gmail;

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
    
    public List<Message> getMessages(String type, int from, int count) throws NoConnectionException, MessagingException {
        if (this.connector.connect()) {

            Folder inbox = this.connector.getStore().getFolder(type);
            inbox.open(Folder.READ_ONLY);

            int inboxCount  = inbox.getMessageCount();
            int inboxFrom   = inboxCount - (from - 1) - count;
            int inboxTo     = inboxCount - from;

            Message[] messages = inbox.getMessages(inboxFrom, inboxTo);
            List<Message> list = Arrays.asList(messages);
            Collections.reverse(list);

            return list;

        } else {
            throw new NoConnectionException("No connection, check your gmail email or password");
        }
    }
}
