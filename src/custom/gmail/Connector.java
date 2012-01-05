package custom.gmail;

import android.util.Log;

import javax.mail.*;
import java.util.Properties;

/**
 * Gmail Connector
 */
public class Connector {

    final private static String TAG = "Connector";
    final private static String GMAIL_IMAP   = "imap.gmail.com";

    private String email        = "";
    private String password     = "";
    private Store store;

    public Connector(String email, String password) {
        this.email      = email;
        this.password   = password;
    }

    /**
     * Trying to connect to gmail imap
     * @return boolean
     */
    public boolean connect() {

        if (isConnect()) return true;

        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");

        try {
            Session session = Session.getDefaultInstance(props, null);
            session.setDebug(true);

            store = session.getStore("imaps");
            store.connect(GMAIL_IMAP, email, password);

            return isConnect();

        } catch (NoSuchProviderException e) {
            Log.e(TAG, e.toString());
        } catch (MessagingException e) {
            Log.e(TAG, e.toString());
        }
        return false;
    }

    public boolean isConnect() {
        return getStore() != null && getStore().isConnected();
    }

    public Store getStore() {
        return store;
    }
}
