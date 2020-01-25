// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.WebSocket;

import com.google.gson.JsonObject;
import com.razshare.elkserver.Settings;
import java.util.ArrayList;
import com.razshare.elkserver.WebSocket.WebSocketEvent;
import com.razshare.elkserver.WebSocket.WebSocketGroup;
import com.razshare.elkserver.WebSocket.WebSocketController;

public class WebSocketGroupApplicationProgramInterface extends WebSocketController
{
    private String groupName;
    private WebSocketGroup group;
    
    @Override
    public void onOpen(final WebSocketEvent e, final ArrayList<String> get_data) {
        if (Settings.isset("groups")) {
            final JsonObject groups = Settings.get("groups").getAsJsonObject();
            if (groups.has("allow")) {
                if (groups.get("allow").getAsBoolean()) {
                    if (e.issetUrlQuery("join")) {
                        this.groupName = e.getUrlQuery("join");
                        if (WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.groupExists(this.groupName)) {
                            this.group = WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.getGroup(this.groupName);
                            if (this.group.getVisibility() == 1) {
                                this.group.addClient(e);
                            }
                            else {
                                e.close();
                            }
                        }
                    }
                }
                else {
                    e.close();
                }
            }
            else {
                e.close();
            }
        }
        else {
            e.close();
        }
    }
    
    @Override
    public void onMessage(final WebSocketEvent e, final byte[] data, final ArrayList<String> get_data) {
        e.send(data, this.group, false);
    }
    
    @Override
    public void onClose(final WebSocketEvent e, final ArrayList<String> get_data) {
        if (this.group.clientExists(e)) {
            this.group.removeClient(e);
        }
        if (WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.getGroup(this.groupName).getMap().size() <= 0) {
            WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.removeGroup(this.group);
            this.group = null;
            System.out.println("removing group from memory");
        }
    }
}
