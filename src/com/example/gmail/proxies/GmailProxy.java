package com.example.gmail.proxies;

import java.util.HashMap;

/**
 * Working with gmail
 */
public class GmailProxy {

    private String email    = "";
    private String password = "";

    public HashMap getInboxMessages(int from, int to) {
        HashMap messages = new HashMap();
        return messages;
    }

    public HashMap getSentMessages(int from, int to) {
        HashMap messages = new HashMap();

        return messages;
    }
    
    public GmailProxy setEmail(String email) {
        this.email = email;
        return this;
    }

    public GmailProxy setPassword(String password) {
        this.password = password;
        return this;
    }

}
