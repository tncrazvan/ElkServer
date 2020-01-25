// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.WebSocket;

import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.util.logging.Level;
import com.razshare.elkserver.Elk;
import com.razshare.elkserver.Http.HttpHeader;
import java.net.Socket;
import java.io.BufferedReader;
import com.razshare.elkserver.Http.HttpSession;
import java.util.ArrayList;
import java.lang.reflect.Method;

public class WebSocketEvent extends WebSocketManager
{
    private Method onCloseMethod;
    private Method onOpenMethod;
    private Method onMessageMethod;
    private WebSocketEvent singleton;
    private ArrayList<String> args;
    private Class<?> c;
    private Object x;
    public HttpSession session;
    
    public WebSocketEvent(final BufferedReader reader, final Socket client, final HttpHeader clientHeader) throws IOException, InstantiationException, IllegalAccessException, NoSuchMethodException {
        super(reader, client, clientHeader);
        this.onCloseMethod = null;
        this.onOpenMethod = null;
        this.onMessageMethod = null;
        this.singleton = null;
        this.args = new ArrayList<String>();
        this.c = null;
        this.x = null;
        this.singleton = this;
        final String[] uri = Elk.decodeUrl(this.location).split("/");
        if (uri.length > 1) {
            final String[] classpath = uri[1].split("\\.");
            String classname = Elk.wsControllerPackageName;
            String tmp = "";
            for (int i = 0; i < classpath.length; ++i) {
                tmp = classpath[i].substring(0, 1);
                if (tmp.equals("@")) {
                    classname = classname + "." + classpath[i].substring(1).substring(0, 1).toUpperCase() + classpath[i].substring(2);
                }
                else {
                    classname = classname + "." + classpath[i];
                }
            }
            if (tmp.equals("@")) {
                try {
                    this.c = Class.forName(classname);
                    this.x = this.c.newInstance();
                    if (uri.length > 2) {
                        for (int i = 2; i < uri.length; ++i) {
                            this.args.add(uri[i]);
                        }
                    }
                    this.onOpenMethod = this.x.getClass().getMethod("onOpen", this.getClass(), this.args.getClass());
                    this.onMessageMethod = this.x.getClass().getMethod("onMessage", this.getClass(), byte[].class, this.args.getClass());
                    this.onCloseMethod = this.x.getClass().getMethod("onClose", this.getClass(), this.args.getClass());
                }
                catch (ClassNotFoundException ex) {
                    try {
                        this.c = Class.forName(Elk.wsControllerPackageName + ".ControllerNotFound");
                        this.x = this.c.newInstance();
                        if (uri.length > 2) {
                            for (int j = 2; j < uri.length; ++j) {
                                this.args.add(uri[j]);
                            }
                        }
                        this.onOpenMethod = this.x.getClass().getMethod("onOpen", this.getClass(), this.args.getClass());
                        this.onMessageMethod = this.x.getClass().getMethod("onMessage", this.getClass(), byte[].class, this.args.getClass());
                        this.onCloseMethod = this.x.getClass().getMethod("onClose", this.getClass(), this.args.getClass());
                    }
                    catch (ClassNotFoundException ex3) {
                        WebSocketEvent.logger.log(Level.SEVERE, null, ex);
                    }
                }
            }
            else {
                try {
                    this.c = Class.forName(Elk.wsControllerPackageName + ".ControllerNotFound");
                    this.x = this.c.newInstance();
                    if (uri.length > 2) {
                        for (int i = 2; i < uri.length; ++i) {
                            this.args.add(uri[i]);
                        }
                    }
                    this.onOpenMethod = this.x.getClass().getMethod("onOpen", this.getClass(), this.args.getClass());
                    this.onMessageMethod = this.x.getClass().getMethod("onMessage", this.getClass(), byte[].class, this.args.getClass());
                    this.onCloseMethod = this.x.getClass().getMethod("onClose", this.getClass(), this.args.getClass());
                }
                catch (ClassNotFoundException ex) {
                    WebSocketEvent.logger.log(Level.SEVERE, null, ex);
                }
            }
        }
        else {
            try {
                this.c = Class.forName(Elk.wsControllerPackageName + ".ControllerNotFound");
                this.x = this.c.newInstance();
                if (uri.length > 2) {
                    for (int k = 2; k < uri.length; ++k) {
                        this.args.add(uri[k]);
                    }
                }
                this.onOpenMethod = this.x.getClass().getMethod("onOpen", this.getClass(), this.args.getClass());
                this.onMessageMethod = this.x.getClass().getMethod("onMessage", this.getClass(), byte[].class, this.args.getClass());
                this.onCloseMethod = this.x.getClass().getMethod("onClose", this.getClass(), this.args.getClass());
            }
            catch (ClassNotFoundException ex2) {
                WebSocketEvent.logger.log(Level.SEVERE, null, ex2);
            }
        }
    }
    
    public boolean sessionIsset() {
        return this.issetCookie("sessionId") && HttpSession.isset(this.getCookie("sessionId"));
    }
    
    public void sessionStart() {
        this.session = HttpSession.start(this);
    }
    
    @Override
    protected void onClose(final Socket client) {
        try {
            Elk.WS_EVENTS.get(this.c.getCanonicalName()).remove(this.singleton);
            this.onCloseMethod.invoke(this.x, this.singleton, this.args);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex3) {
            WebSocketEvent.logger.log(Level.SEVERE, null, ex3);
        }
    }
    
    @Override
    protected void onOpen(final Socket client) {
        try {
            if (Elk.WS_EVENTS.get(this.c.getCanonicalName()) == null) {
                final ArrayList<WebSocketEvent> tmp = new ArrayList<WebSocketEvent>();
                tmp.add(this.singleton);
                Elk.WS_EVENTS.put(this.c.getCanonicalName(), tmp);
            }
            else {
                Elk.WS_EVENTS.get(this.c.getCanonicalName()).add(this.singleton);
            }
            this.onOpenMethod.invoke(this.x, this.singleton, this.args);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex3) {
            WebSocketEvent.logger.log(Level.SEVERE, null, ex3);
        }
    }
    
    @Override
    protected void onMessage(final Socket client, final byte[] data) {
        try {
            this.onMessageMethod.invoke(this.x, this.singleton, data, this.args);
        }
        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex3) {
            WebSocketEvent.logger.log(Level.SEVERE, null, ex3);
        }
    }
}
