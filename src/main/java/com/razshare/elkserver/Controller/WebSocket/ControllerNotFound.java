// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.WebSocket;

import java.util.ArrayList;
import com.razshare.elkserver.WebSocket.WebSocketEvent;
import com.razshare.elkserver.WebSocket.WebSocketController;

public class ControllerNotFound extends WebSocketController
{
    @Override
    public void onOpen(final WebSocketEvent e, final ArrayList<String> args) {
        e.send("Elk server error: Controller not found");
        e.close();
    }
    
    @Override
    public void onMessage(final WebSocketEvent e, final byte[] data, final ArrayList<String> args) {
    }
    
    @Override
    public void onClose(final WebSocketEvent e, final ArrayList<String> args) {
    }
}
