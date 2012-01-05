package custom.gmail;

import android.util.Log;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.*;

/**
 */
public class Sender {

    final private static String TAG = "Sender";

    protected Connector connector;

    public Sender(Connector connector) {
        this.connector = connector;
    }

    public void send(String from, String to, String subject, String message) {
    }
}
