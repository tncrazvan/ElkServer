// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.Http;

import java.util.Map;
import com.razshare.elkserver.Http.Cookie;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.File;
import com.razshare.elkserver.Elk;
import java.util.ArrayList;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Http.HttpController;

public class Isset extends HttpController
{
    @Override
    public void main(final HttpEvent e, final ArrayList<String> path, final String content) {
    }
    
    @Override
    public void onClose() {
    }
    
    public void file(final HttpEvent e, final ArrayList<String> path, final String content) throws FileNotFoundException, IOException {
        if (path.size() >= 0) {
            final File f = new File(Elk.webRoot + "/" + path.get(0));
            if (f.exists()) {
                e.send(0);
            }
            else {
                e.send(-2);
            }
        }
        else {
            e.send(-1);
        }
    }
    
    public void cookie(final HttpEvent e, final ArrayList<String> path, final String content) {
        if (e.getClientHeader().get("Method").equals("POST")) {
            final Map<String, String> multipart = Elk.readAsMultipartFormData(content);
            if (multipart.containsKey("name")) {
                final String name = multipart.get("name");
                if (e.cookieIsset(name)) {
                    e.send(0);
                }
                else {
                    e.send(-2);
                }
            }
            else {
                e.send(-1);
            }
        }
        else {
            final String jsonCookie = Elk.JSON_PARSER.toJson(new Cookie("Error", "-1"));
            e.send(jsonCookie);
        }
    }
}
