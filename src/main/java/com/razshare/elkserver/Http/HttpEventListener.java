// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import java.util.logging.Level;
import com.razshare.elkserver.WebSocket.WebSocketEvent;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class HttpEventListener extends HttpRequestReader
{
    private Matcher matcher;
    private static final Pattern upgradePattern;
    private static final Pattern keepAlivePattern;
    private static final Pattern websocketPattern;
    private static final Pattern http2Pattern;
    
    public HttpEventListener(final Socket client) throws IOException, NoSuchAlgorithmException {
        super(client);
    }
    
    @Override
    public void onRequest(final HttpHeader clientHeader, final String content) {
        if (clientHeader != null && clientHeader.get("Connection") != null) {
            this.matcher = HttpEventListener.upgradePattern.matcher(clientHeader.get("Connection"));
            if (this.matcher.find()) {
                this.matcher = HttpEventListener.websocketPattern.matcher(clientHeader.get("Upgrade"));
                if (this.matcher.find()) {
                    try {
                        new WebSocketEvent(this.reader, this.client, clientHeader).execute();
                    }
                    catch (IOException e) {
                        try {
                            this.client.close();
                        }
                        catch (IOException ex) {
                            HttpEventListener.logger.log(Level.SEVERE, null, ex);
                        }
                    }
                    catch (InstantiationException | IllegalAccessException | NoSuchMethodException ex5) {
                        HttpEventListener.logger.log(Level.SEVERE, null, ex5);
                    }
                }
                else {
                    this.matcher = HttpEventListener.http2Pattern.matcher(clientHeader.get("Upgrade"));
                    if (this.matcher.find()) {
                        System.out.println("Http 2.0 connection detected. Not yet implemented.");
                    }
                }
            }
            else {
                try {
                    this.client.setSoTimeout(HttpEventListener.timeout);
                    new HttpEvent(this.output, clientHeader, this.client, content).execute();
                }
                catch (IOException ex3) {
                    HttpEventListener.logger.log(Level.SEVERE, null, ex3);
                }
            }
        }
    }
    
    static {
        upgradePattern = Pattern.compile("Upgrade");
        keepAlivePattern = Pattern.compile("keep-alive");
        websocketPattern = Pattern.compile("websocket");
        http2Pattern = Pattern.compile("h2c");
    }
}
