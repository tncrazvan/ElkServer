// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.WebSocket;

import java.util.HashMap;
import java.util.Map;

public class WebSocketGroupManager
{
    private final Map<String, WebSocketGroup> groups;
    
    public WebSocketGroupManager() {
        this.groups = new HashMap<String, WebSocketGroup>();
    }
    
    public void addGroup(final WebSocketGroup group) {
        this.groups.put(group.getKey(), group);
    }
    
    public WebSocketGroup removeGroup(final WebSocketGroup group) {
        return this.groups.remove(group.getKey());
    }
    
    public boolean groupExists(final String key) {
        return this.groups.containsKey(key);
    }
    
    public WebSocketGroup getGroup(final String key) {
        return this.groups.get(key);
    }
    
    public Map<String, WebSocketGroup> getAllGroups() {
        return this.groups;
    }
}
