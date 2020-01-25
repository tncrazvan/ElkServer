// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.SmtpServer;

import java.util.Iterator;
import com.razshare.elkserver.Elk;
import java.io.IOException;
import java.net.Socket;
import java.net.InetAddress;
import javax.net.ssl.SSLSocketFactory;

public class EmailComposer extends SmtpMessageManager
{
    private final Email email;
    
    public EmailComposer(final Email email, final SmtpServer server, final String clientHostname, final int port, final boolean ssl) throws IOException {
        super(server, ssl ? SSLSocketFactory.getDefault().createSocket(InetAddress.getByName(clientHostname), port) : new Socket(clientHostname, port));
        this.email = email;
    }
    
    public EmailComposer(final Email email, final SmtpServer server, final String clientHostname, final int port) throws IOException {
        super(server, new Socket(clientHostname, port));
        this.email = email;
    }
    
    public boolean submit() throws IOException {
        if (!this.isReady(this.read())) {
            return false;
        }
        this.sayHelo();
        if (!this.isOk(this.read())) {
            return false;
        }
        this.sayMailFrom(this.email.getSender());
        if (!this.isOk(this.read())) {
            return false;
        }
        for (final String recipient : this.email.getRecipients()) {
            this.sayRecipient(recipient);
            final String error;
            if (!this.isOk(error = this.read())) {
                System.err.println("[WARNING] Server replied with: " + error);
            }
        }
        this.sayData();
        final String eodLine;
        if (!this.isEndDataWith(eodLine = this.read())) {
            return false;
        }
        final String eodSequence = this.getEndDataWithValue(eodLine);
        this.sayDataFrom(this.email.getSender());
        this.sayDataDate(Elk.calendar.getTime().getTime());
        this.sayDataSubject(this.email.getSubject());
        this.sayDataTo((String[])this.email.getRecipients().toArray());
        this.setMultipartBoundaryId(Elk.generateMultipartBoundary());
        this.sayDataContentType();
        this.sayNothing();
        this.sayDataFrames(this.email.getFrames());
        this.say(".");
        if (!this.isOk(this.read())) {
            return false;
        }
        this.sayQuitAndClose();
        return true;
    }
}
