package custom.gmail;

import custom.gmail.exceptions.NoConnectionException;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * Read gmail inbox/sent messages
 */
public class Reader {
    
    private static final String MESSAGE_TYPE_INBOX = "inbox";
    private static final String MESSAGE_TYPE_SENT  = "sent";

    protected Connector connector;
    
    public Reader(Connector connector) {
        this.connector = connector;
    }
    
    public Message[] getMessages(String type, int from, int to) throws NoConnectionException, MessagingException {
        if (this.connector.connect()) {
            Folder inbox = this.connector.getStore().getFolder(type);
            inbox.open(Folder.READ_ONLY);

            return inbox.getMessages(from, to);

        } else {
            throw new NoConnectionException("No connection, check your gmail email or password");
        }
    }
}
