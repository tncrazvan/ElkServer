// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Controller.Http;

import java.util.ArrayList;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Http.HttpController;

public class ControllerNotFound extends HttpController
{
    @Override
    public void main(final HttpEvent e, final ArrayList<String> path, final String content) {
        e.setStatus("404 Not Found");
        try {
            e.send("Page not found");
        }
        catch (Exception ex) {
            System.err.println("\n\nException on location: " + e.getClientHeader().get("Resource"));
            ex.printStackTrace();
        }
    }
    
    @Override
    public void onClose() {
    }
}
