// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.SmtpServer;

import java.util.ArrayList;

public class Email
{
    private final String subject;
    private final String sender;
    private final ArrayList<EmailFrame> frames;
    private final ArrayList<String> recipents;
    
    public Email(final String subject, final ArrayList<EmailFrame> frames, final String sender, final ArrayList<String> recipents) {
        this.subject = subject;
        this.sender = sender;
        this.frames = frames;
        this.recipents = recipents;
    }
    
    public boolean addRecipient(final String recipient) {
        return this.recipents.add(recipient);
    }
    
    public boolean removeRcipient(final String recipient) {
        return this.recipents.remove(recipient);
    }
    
    public ArrayList<EmailFrame> getFrames() {
        return this.frames;
    }
    
    public ArrayList<String> getRecipients() {
        return this.recipents;
    }
    
    public String getSubject() {
        return this.subject;
    }
    
    public String getSender() {
        return this.sender;
    }
}
