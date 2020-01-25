// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.WebSocket;

import java.util.logging.Logger;
import java.util.Iterator;
import java.util.Arrays;
import java.io.UnsupportedEncodingException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import com.razshare.elkserver.Elk;
import java.util.HashMap;
import java.util.Map;
import java.io.OutputStream;
import java.io.BufferedReader;
import com.razshare.elkserver.Http.HttpHeader;
import java.net.Socket;
import java.util.ArrayList;
import com.razshare.elkserver.EventManager;

public abstract class WebSocketManager extends EventManager
{
    private static ArrayList<WebSocketEvent> subscriptions;
    protected final Socket client;
    protected final HttpHeader clientHeader;
    protected final BufferedReader reader;
    protected final String requesteId;
    protected final OutputStream outputStream;
    private Map<String, String> userLanguages;
    private boolean connected;
    private final int FIRST_BYTE = 0;
    private final int SECOND_BYTE = 1;
    private final int LENGTH2 = 2;
    private final int LENGTH8 = 3;
    private final int MASK = 4;
    private final int PAYLOAD = 5;
    private final int DONE = 6;
    private int lengthKey;
    private int reading;
    private int lengthIndex;
    private int maskIndex;
    private int payloadIndex;
    private int payloadLength;
    private boolean fin;
    private boolean rsv1;
    private boolean rsv2;
    private boolean rsv3;
    private byte opcode;
    private byte[] payload;
    private byte[] mask;
    private byte[] length;
    private String base;
    
    public WebSocketManager(final BufferedReader reader, final Socket client, final HttpHeader clientHeader) throws IOException {
        super(clientHeader);
        this.userLanguages = new HashMap<String, String>();
        this.connected = true;
        this.lengthKey = 0;
        this.reading = 0;
        this.lengthIndex = 0;
        this.maskIndex = 0;
        this.payloadIndex = 0;
        this.payloadLength = 0;
        this.payload = null;
        this.mask = null;
        this.length = null;
        this.base = "";
        this.client = client;
        this.clientHeader = clientHeader;
        this.reader = reader;
        this.requesteId = Elk.getSha1String(System.identityHashCode(client) + "::" + System.currentTimeMillis());
        this.outputStream = client.getOutputStream();
    }
    
    public HttpHeader getClientHeader() {
        return this.clientHeader;
    }
    
    public Socket getClient() {
        return this.client;
    }
    
    public Map<String, String> getUserLanguages() {
        return this.userLanguages;
    }
    
    public String getUserDefaultLanguage() {
        return this.userLanguages.get("DEFAULT-LANGUAGE");
    }
    
    public String getUserAgent() {
        return this.clientHeader.get("User-Agent");
    }
    
    public void execute() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final String acceptKey = DatatypeConverter.printBase64Binary(Elk.getSha1Bytes(WebSocketManager.this.clientHeader.get("Sec-WebSocket-Key") + WebSocketManager.wsAcceptKey));
                    WebSocketManager.this.header.set("Status", "HTTP/1.1 101 Switching Protocols");
                    WebSocketManager.this.header.set("Connection", "Upgrade");
                    WebSocketManager.this.header.set("Upgrade", "websocket");
                    WebSocketManager.this.header.set("Sec-WebSocket-Accept", acceptKey);
                    WebSocketManager.this.outputStream.write((WebSocketManager.this.header.toString() + "\r\n").getBytes());
                    WebSocketManager.this.outputStream.flush();
                    WebSocketManager.this.onOpen(WebSocketManager.this.client);
                    final byte[] data = { 0 };
                    final InputStream read = WebSocketManager.this.client.getInputStream();
                    while (WebSocketManager.this.connected) {
                        WebSocketManager.this.unmask((byte)read.read());
                    }
                }
                catch (IOException ex2) {
                    WebSocketManager.this.close();
                }
                catch (NoSuchAlgorithmException ex) {
                    WebSocketManager.logger.log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }
    
    public void unmask(final byte b) throws UnsupportedEncodingException, IOException {
        switch (this.reading) {
            case 0: {
                this.fin = ((b & 0x80) != 0x0);
                this.rsv1 = ((b & 0x40) != 0x0);
                this.rsv2 = ((b & 0x20) != 0x0);
                this.rsv3 = ((b & 0x10) != 0x0);
                this.opcode = (byte)(b & 0xF);
                if (this.opcode == 8) {
                    this.close();
                }
                this.mask = new byte[4];
                this.reading = 1;
                break;
            }
            case 1: {
                this.lengthKey = (b & 0x7F);
                if (this.lengthKey <= 125) {
                    (this.length = new byte[1])[0] = (byte)this.lengthKey;
                    this.payloadLength = (this.lengthKey & 0xFF);
                    this.reading = 4;
                    break;
                }
                if (this.lengthKey == 126) {
                    this.reading = 2;
                    this.length = new byte[2];
                    break;
                }
                if (this.lengthKey == 127) {
                    this.reading = 3;
                    this.length = new byte[8];
                    break;
                }
                break;
            }
            case 2: {
                this.length[this.lengthIndex] = b;
                ++this.lengthIndex;
                if (this.lengthIndex == 2) {
                    this.payloadLength = ((this.length[0] & 0xFF) << 8 | (this.length[1] & 0xFF));
                    this.reading = 4;
                    break;
                }
                break;
            }
            case 3: {
                this.length[this.lengthIndex] = b;
                ++this.lengthIndex;
                if (this.lengthIndex == 8) {
                    this.payloadLength = (this.length[0] & 0xFF);
                    for (int i = 1; i < this.length.length; ++i) {
                        this.payloadLength = (this.payloadLength << 8 | (this.length[i] & 0xFF));
                    }
                    this.reading = 4;
                    break;
                }
                break;
            }
            case 4: {
                this.mask[this.maskIndex] = b;
                ++this.maskIndex;
                if (this.maskIndex == 4) {
                    this.reading = 5;
                    this.payload = new byte[this.payloadLength];
                    break;
                }
                break;
            }
            case 5: {
                try {
                    this.payload[this.payloadIndex] = (byte)(b ^ this.mask[this.payloadIndex % 4]);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                ++this.payloadIndex;
                if (this.payloadIndex == this.payload.length) {
                    this.reading = 6;
                    this.onMessage(this.client, this.payload);
                    this.lengthKey = 0;
                    this.reading = 0;
                    this.lengthIndex = 0;
                    this.maskIndex = 0;
                    this.payloadIndex = 0;
                    this.payload = null;
                    this.mask = null;
                    this.length = null;
                    break;
                }
                break;
            }
        }
    }
    
    public void close() {
        try {
            this.connected = false;
            this.client.close();
            this.onClose(this.client);
        }
        catch (IOException ex) {
            WebSocketManager.logger.log(Level.SEVERE, null, ex);
        }
    }
    
    public void send(final String data) {
        try {
            this.send(data.getBytes(Elk.charset), false);
        }
        catch (UnsupportedEncodingException ex) {
            WebSocketManager.logger.log(Level.SEVERE, null, ex);
        }
    }
    
    public void send(final byte[] data) {
        this.send(data, true);
    }
    
    public void send(final byte[] data, final boolean binary) {
        int offset = 0;
        final int maxLength = 65535;
        if (data.length > maxLength) {
            while (offset < data.length) {
                if (offset + maxLength > data.length) {
                    this.encodeAndSendBytes(Arrays.copyOfRange(data, offset, data.length), binary);
                    offset = data.length;
                }
                else {
                    this.encodeAndSendBytes(Arrays.copyOfRange(data, offset, offset + maxLength), binary);
                    offset += maxLength;
                }
            }
        }
        else {
            this.encodeAndSendBytes(data, binary);
        }
    }
    
    private void encodeAndSendBytes(final byte[] messageBytes, final boolean binary) {
        try {
            this.outputStream.flush();
        }
        catch (IOException ex) {
            WebSocketManager.logger.log(Level.SEVERE, null, ex);
        }
        try {
            this.outputStream.write(binary ? 130 : 129);
            if (messageBytes.length <= 125) {
                this.outputStream.write(messageBytes.length);
            }
            else {
                this.outputStream.write(126);
                final int b1 = messageBytes.length >> 8 & 0xFF;
                final int b2 = messageBytes.length & 0xFF;
                this.outputStream.write(b1);
                this.outputStream.write(b2);
            }
            this.outputStream.write(messageBytes);
            try {
                this.outputStream.flush();
            }
            catch (IOException ex) {
                WebSocketManager.logger.log(Level.SEVERE, null, ex);
            }
        }
        catch (IOException ex) {
            this.close();
        }
    }
    
    public void send(final int data) {
        this.send("" + data);
    }
    
    public void broadcast(final String msg, final Object o) {
        try {
            this.broadcast(msg.getBytes(Elk.charset), o, false);
        }
        catch (UnsupportedEncodingException ex) {
            WebSocketManager.logger.log(Level.SEVERE, null, ex);
        }
    }
    
    public void broadcast(final byte[] data, final Object o) {
        this.broadcast(data, o, true);
    }
    
    public void broadcast(final byte[] data, final Object o, final boolean binary) {
        for (final WebSocketEvent e : Elk.WS_EVENTS.get(o.getClass().getCanonicalName())) {
            if (e != this) {
                e.send(data, binary);
            }
        }
    }
    
    public void send(final byte[] data, final WebSocketGroup group) {
        this.send(data, group, true);
    }
    
    public void send(final byte[] data, final WebSocketGroup group, final boolean binary) {
        group.getMap().keySet().forEach(key -> {
            WebSocketEvent client = (WebSocketEvent) group.getMap().get(key);
            if (client != this) {
                client.send(data, binary);
            }
        });
    }
    
    public void send(final String data, final WebSocketGroup group) {
        try {
            this.send(data.getBytes(Elk.charset), group, false);
        }
        catch (UnsupportedEncodingException ex) {
            WebSocketManager.logger.log(Level.SEVERE, null, ex);
        }
    }
    
    protected abstract void onOpen(final Socket p0);
    
    protected abstract void onMessage(final Socket p0, final byte[] p1);
    
    protected abstract void onClose(final Socket p0);
    
    static {
        WebSocketManager.subscriptions = new ArrayList<WebSocketEvent>();
    }
}
