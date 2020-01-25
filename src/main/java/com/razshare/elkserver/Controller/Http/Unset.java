// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.Http;

import java.util.Map;
import com.razshare.elkserver.Http.Cookie;
import com.razshare.elkserver.Elk;
import java.util.ArrayList;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Http.HttpController;

public class Unset extends HttpController
{
    @Override
    public void main(final HttpEvent e, final ArrayList<String> path, final String content) {
    }
    
    @Override
    public void onClose() {
    }
    
    public void cookie(final HttpEvent e, final ArrayList<String> path, final String content) {
        final Map<String, String> multipart = Elk.readAsMultipartFormData(content);
        if (e.getMethod().equals("POST")) {
            if (multipart.containsKey("name") && multipart.containsKey("domain") && multipart.containsKey("path")) {
                final String name = multipart.get("name");
                if (e.cookieIsset(name)) {
                    e.unsetCookie(name, multipart.get("path"), multipart.get("domain"));
                    e.send(0);
                }
                else {
                    e.send(0);
                }
            }
            else {
                e.setStatus("404 Not Found");
                e.flushHeaders();
            }
        }
        else {
            final String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
}
