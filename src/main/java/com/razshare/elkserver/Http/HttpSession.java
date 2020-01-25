// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import com.razshare.elkserver.WebSocket.WebSocketEvent;
import com.razshare.elkserver.Elk;
import java.util.HashMap;
import java.util.Map;

public class HttpSession
{
    private static final Map<String, HttpSession> LIST;
    private final String id;
    private final Map<String, Object> STORAGE;
    
    private HttpSession(final HttpEvent e) {
        this.STORAGE = new HashMap<String, Object>();
        e.setCookie("sessionId", this.id = Elk.getSha1String(e.getClient().getInetAddress().toString() + "," + e.getClient().getPort() + "," + Math.random()), "/");
    }
    
    private HttpSession(final WebSocketEvent e) {
        this.STORAGE = new HashMap<String, Object>();
        e.setCookie("sessionId", this.id = Elk.getSha1String(e.getClient().getInetAddress().toString() + "," + e.getClient().getPort() + "," + Math.random()), "/");
    }
    
    public static HttpSession start(final HttpEvent e) {
        if (e.issetCookie("sessionId")) {
            final String sessionId = e.getCookie("sessionId");
            if (HttpSession.LIST.containsKey(sessionId)) {
                return HttpSession.LIST.get(sessionId);
            }
        }
        final HttpSession session = new HttpSession(e);
        set(session);
        return session;
    }
    
    public static HttpSession start(final WebSocketEvent e) {
        if (e.issetCookie("sessionId")) {
            final String sessionId = e.getCookie("sessionId");
            if (HttpSession.LIST.containsKey(sessionId)) {
                return HttpSession.LIST.get(sessionId);
            }
        }
        final HttpSession session = new HttpSession(e);
        set(session);
        return session;
    }
    
    public static HttpSession get(final String sessionId) {
        return HttpSession.LIST.get(sessionId);
    }
    
    public static void set(final HttpSession session) {
        HttpSession.LIST.put(session.getSessionId(), session);
    }
    
    public static boolean isset(final String sessionId) {
        return HttpSession.LIST.containsKey(sessionId);
    }
    
    public static void unset(final HttpSession session) {
        HttpSession.LIST.remove(session.getSessionId());
    }
    
    public String getSessionId() {
        return this.id;
    }
    
    public void setProperty(final String key, final Object o) {
        this.STORAGE.put(key, o);
    }
    
    public void unsetProperty(final String key) {
        this.STORAGE.remove(key);
    }
    
    public boolean issetProperty(final String key) {
        return this.STORAGE.containsKey(key);
    }
    
    public Object getProperty(final String key) {
        return this.STORAGE.get(key);
    }
    
    static {
        LIST = new HashMap<String, HttpSession>();
    }
}
