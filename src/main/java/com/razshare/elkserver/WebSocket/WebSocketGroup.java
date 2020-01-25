// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.WebSocket;

import com.razshare.elkserver.Elk;
import java.util.HashMap;
import com.razshare.elkserver.Http.HttpSession;
import java.util.Map;

public class WebSocketGroup
{
    public static final int PRIVATE = 0;
    public static final int PUBLIC = 1;
    private final String key;
    private final Map<String, WebSocketEvent> events;
    private WebSocketEvent master;
    private int visibility;
    private String name;
    
    public WebSocketGroup(final HttpSession session) {
        this.events = new HashMap<String, WebSocketEvent>();
        this.master = null;
        this.visibility = 0;
        this.key = Elk.getBCryptString(session.getSessionId());
    }
    
    public void setGroupName(final String name) {
        this.name = name;
    }
    
    public String getGroupName() {
        return this.name;
    }
    
    public void setVisibility(final int v) {
        this.visibility = v;
    }
    
    public int getVisibility() {
        return this.visibility;
    }
    
    public void addClient(final WebSocketEvent e) {
        e.sessionStart();
        this.events.put(e.session.getSessionId(), e);
    }
    
    public WebSocketEvent removeClient(final WebSocketEvent e) {
        if (this.matchCreator(e)) {
            this.master = null;
        }
        return this.events.remove(e.session.getSessionId());
    }
    
    public boolean clientExists(final WebSocketEvent e) {
        return this.events.containsKey(e.session.getSessionId());
    }
    
    public Map<String, WebSocketEvent> getMap() {
        return this.events;
    }
    
    public String getKey() {
        return this.key;
    }
    
    public WebSocketEvent getGroupMaster() {
        return this.master;
    }
    
    public boolean groupMasterIsset() {
        return this.master != null;
    }
    
    public void setGroupMaster(final WebSocketEvent e) {
        this.master = e;
    }
    
    public void unsetGroupMaster() {
        this.master = null;
    }
    
    public boolean matchCreator(final WebSocketEvent e) {
        return Elk.validateBCryptString(e.session.getSessionId(), this.key);
    }
}
