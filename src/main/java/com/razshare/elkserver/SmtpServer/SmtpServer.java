// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.SmtpServer;

import java.util.logging.Level;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import com.razshare.elkserver.Elk;

public class SmtpServer extends Elk implements Runnable
{
    private final ArrayList<SmtpListener> listeners;
    private final ServerSocket ss;
    private String hostname;
    
    public SmtpServer(final ServerSocket ss, final String bindAddress, final int port, final String hostname) throws IOException {
        this.listeners = new ArrayList<SmtpListener>();
        this.hostname = "";
        (this.ss = ss).bind(new InetSocketAddress(bindAddress, port));
        this.hostname = hostname;
    }
    
    public String getHostname() {
        return this.hostname;
    }
    
    @Override
    public void run() {
        while (SmtpServer.listen) {
            try {
                final EmailReader emailReader = new EmailReader(this, this.ss.accept(), this.listeners);
                emailReader.parse();
            }
            catch (IOException ex) {
                SmtpServer.logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void addEventListener(final SmtpListener listener) {
        this.listeners.add(listener);
    }
    
    public void removeEventListener(final SmtpListener listener) {
        this.listeners.remove(listener);
    }
}
