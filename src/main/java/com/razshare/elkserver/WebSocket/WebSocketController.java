// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.WebSocket;

import java.util.ArrayList;
import com.razshare.elkserver.Elk;

public abstract class WebSocketController extends Elk
{
    public static final WebSocketGroupManager GROUP_MANAGER;
    
    public abstract void onOpen(final WebSocketEvent p0, final ArrayList<String> p1);
    
    public abstract void onMessage(final WebSocketEvent p0, final byte[] p1, final ArrayList<String> p2);
    
    public abstract void onClose(final WebSocketEvent p0, final ArrayList<String> p1);
    
    static {
        GROUP_MANAGER = new WebSocketGroupManager();
    }
}
