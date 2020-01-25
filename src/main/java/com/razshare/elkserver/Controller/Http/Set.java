// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.Http;

import java.util.Map;
import com.razshare.elkserver.Http.Cookie;
import com.razshare.elkserver.Elk;
import com.google.gson.JsonObject;
import com.razshare.elkserver.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import com.razshare.elkserver.WebSocket.WebSocketGroup;
import com.razshare.elkserver.Http.HttpSession;
import com.razshare.elkserver.Settings;
import java.util.ArrayList;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Http.HttpController;

public class Set extends HttpController
{
    private boolean readAsBase64;
    private boolean writeAsBase64;
    private static final String GROUPS_NOT_ALLOWED = "WebSocket groups are not allowd.";
    private static final String GROUPS_POLICY_NOT_DEFINED = "WebSocket groups policy is not defined by the server therefore by default it is disabled.";
    
    public Set() {
        this.readAsBase64 = false;
        this.writeAsBase64 = false;
    }
    
    @Override
    public void main(final HttpEvent e, final ArrayList<String> path, final String content) {
    }
    
    @Override
    public void onClose() {
    }
    
    public void readAsBase64(final boolean val) {
        this.readAsBase64 = val;
    }
    
    public void writeAsBase64(final boolean val) {
        this.writeAsBase64 = val;
    }
    
    public void webSocketGroup(final HttpEvent e, final ArrayList<String> path, final String content) {
        if (Settings.isset("groups")) {
            final JsonObject groups = Settings.get("groups").getAsJsonObject();
            if (groups.has("allow")) {
                if (groups.get("allow").getAsBoolean()) {
                    final HttpSession session = HttpSession.start(e);
                    final WebSocketGroup group = new WebSocketGroup(session);
                    if (e.issetUrlQuery("visibility")) {
                        group.setVisibility(Integer.parseInt(e.getUrlQuery("visibility")));
                    }
                    if (e.issetUrlQuery("name")) {
                        group.setGroupName(e.getUrlQuery("name"));
                    }
                    WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.addGroup(group);
                    e.send(group.getKey());
                }
                else {
                    e.setStatus("404 Not Found");
                    e.send("WebSocket groups are not allowd.");
                }
            }
            else {
                e.setStatus("404 Not Found");
                e.send("WebSocket groups are not allowd.");
            }
        }
        else {
            e.setStatus("404 Not Found");
            e.send("WebSocket groups policy is not defined by the server therefore by default it is disabled.");
        }
    }
    
    public void cookie(final HttpEvent e, final ArrayList<String> path, final String content) {
        if (e.getMethod().equals("POST")) {
            final Map<String, String> multipart = Elk.readAsMultipartFormData(content);
            if (multipart.containsKey("name") && multipart.containsKey("value") && multipart.containsKey("path") && multipart.containsKey("domain") && multipart.containsKey("expire")) {
                e.setContentType("application/json");
                final String name = multipart.get("name");
                final String value = multipart.get("value");
                final String cpath = multipart.get("path");
                final String domain = multipart.get("domain");
                final String expire = multipart.get("expire");
                e.setCookie(name, value, cpath, domain, expire);
                final JsonObject cookie = new JsonObject();
                cookie.addProperty("type", "cookie");
                cookie.addProperty("value", value);
                e.send(cookie.toString());
            }
            else {
                final String jsonCookie = Set.JSON_PARSER.toJson(new Cookie("Error", "-1"));
                e.send(jsonCookie);
            }
        }
        else {
            final String jsonCookie2 = Set.JSON_PARSER.toJson(new Cookie("Error", "-2"));
            e.send(jsonCookie2);
        }
    }
}
