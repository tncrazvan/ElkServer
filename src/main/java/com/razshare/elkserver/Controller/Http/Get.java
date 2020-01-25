// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.Http;

import java.util.Map;
import com.razshare.elkserver.Http.Cookie;
import com.razshare.elkserver.WebSocket.WebSocketGroup;
import java.util.Iterator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.razshare.elkserver.Controller.WebSocket.WebSocketGroupApplicationProgramInterface;
import com.google.gson.JsonArray;
import java.io.IOException;
import java.io.FileNotFoundException;
import com.razshare.elkserver.Elk;
import java.util.ArrayList;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Http.HttpController;

public class Get extends HttpController
{
    @Override
    public void main(final HttpEvent e, final ArrayList<String> path, final String content) {
    }
    
    @Override
    public void onClose() {
    }
    
    public void file(final HttpEvent e, final ArrayList<String> path, final String content) throws FileNotFoundException, IOException {
        e.setContentType(Elk.getContentType(path.get(0)));
        e.sendFileContents("/" + (path.get(0).equals("") ? path.get(1) : path.get(0)));
    }
    
    public void allWebSocketGroups(final HttpEvent e, final ArrayList<String> path, final String content) {
        final JsonArray arr = new JsonArray();
        for (final String key : WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.getAllGroups().keySet()) {
            final WebSocketGroup group = WebSocketGroupApplicationProgramInterface.GROUP_MANAGER.getGroup(key);
            if (group.getVisibility() == 1) {
                final JsonObject o = new JsonObject();
                o.addProperty("name", group.getGroupName());
                o.addProperty("id", key);
                arr.add(o);
            }
        }
        e.send(arr.toString());
    }
    
    public void cookie(final HttpEvent e, final ArrayList<String> path, final String content) {
        final Map<String, String> multipart = Elk.readAsMultipartFormData(content);
        if (e.getMethod().equals("POST")) {
            if (multipart.containsKey("name")) {
                final String name = multipart.get("name");
                if (e.cookieIsset(name)) {
                    e.setContentType("application/json");
                    final String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Cookie", e.getCookie(name)));
                    e.send(jsonCookie);
                }
                else {
                    e.setContentType("text/plain");
                    e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                    e.flush();
                }
            }
            else {
                e.setContentType("text/plain");
                e.setHeaderField("Status", "HTTP/1.1 404 Not Found");
                e.flush();
            }
        }
        else {
            final String jsonCookie2 = Elk.JSON_PARSER.toJson(new com.razshare.elkserver.Http.Cookie("Error", "-1"));
            e.send(jsonCookie2);
        }
    }
    
    class Cookie
    {
        String type;
        String value;
        
        public Cookie(final String type, final String value) {
            this.type = type;
            this.value = value;
        }
    }
}
