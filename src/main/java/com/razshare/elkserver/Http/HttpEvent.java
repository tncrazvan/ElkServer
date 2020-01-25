// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.util.logging.Level;
import com.razshare.elkserver.Elk;
import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.io.DataOutputStream;

public class HttpEvent extends HttpEventManager
{
    private final HttpEvent singleton;
    public HttpSession session;
    
    public HttpEvent(final DataOutputStream output, final HttpHeader clientHeader, final Socket client, final String content) throws UnsupportedEncodingException {
        super(output, clientHeader, client, content);
        this.singleton = this;
    }
    
    public boolean sessionIsset() {
        return this.issetCookie("sessionId") && HttpSession.isset(this.getCookie("sessionId"));
    }
    
    public void sessionStart() {
        this.session = HttpSession.start(this);
    }
    
    @Override
    void onControllerRequest(final String location) {
        final ArrayList<String> args = new ArrayList<String>();
        final String[] uri = Elk.decodeUrl(location).split("/");
        if (uri.length > 1) {
            final String[] classpath = uri[1].split("\\.");
            String classname = Elk.httpControllerPackageName;
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
            try {
                Class.forName(classname);
                if (!tmp.equals("@")) {
                    this.setContentType("text/html");
                    this.sendFileContents(Elk.indexFile);
                    this.client.close();
                }
                else {
                    final Class<?> c = Class.forName(classname);
                    final Object x = c.newInstance();
                    Method m;
                    if (uri.length > 2) {
                        if (uri.length > 3) {
                            for (int i = 3; i < uri.length; ++i) {
                                args.add(uri[i]);
                            }
                            m = x.getClass().getDeclaredMethod(uri[2], this.getClass(), args.getClass(), this.content.getClass());
                        }
                        else {
                            m = x.getClass().getDeclaredMethod(uri[2], this.getClass(), args.getClass(), this.content.getClass());
                        }
                    }
                    else {
                        m = x.getClass().getDeclaredMethod("main", this.getClass(), args.getClass(), this.content.getClass());
                    }
                    final Method onCloseMethod = x.getClass().getDeclaredMethod("onClose", (Class<?>[])new Class[0]);
                    try {
                        m.invoke(x, this.singleton, args, this.content);
                        onCloseMethod.invoke(x, new Object[0]);
                        this.client.close();
                    }
                    catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IOException ex13) {
                        HttpEvent.logger.log(Level.SEVERE, null, ex13);
                        try {
                            onCloseMethod.invoke(x, new Object[0]);
                            this.client.close();
                        }
                        catch (IOException | InvocationTargetException ex14) {
                            HttpEvent.logger.log(Level.SEVERE, null, ex14);
                        }
                    }
                }
            }
            catch (ClassNotFoundException ex4) {
                try {
                    final Class<?> c = Class.forName(Elk.httpControllerPackageName + "." + Elk.httpControllerNotFound);
                    final Object x = c.newInstance();
                    final Method m = x.getClass().getDeclaredMethod("main", this.getClass(), args.getClass(), this.content.getClass());
                    final Method onCloseMethod = x.getClass().getDeclaredMethod("onClose", (Class<?>[])new Class[0]);
                    m.invoke(x, this.singleton, args, this.content);
                    onCloseMethod.invoke(x, new Object[0]);
                    this.client.close();
                }
                catch (ClassNotFoundException | IOException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException ex15) {
                    HttpEvent.logger.log(Level.SEVERE, null, ex15);
                    try {
                        this.client.close();
                    }
                    catch (IOException ex6) {
                        HttpEvent.logger.log(Level.SEVERE, null, ex6);
                    }
                }
                catch (InstantiationException ex7) {
                    HttpEvent.logger.log(Level.SEVERE, null, ex7);
                }
            }
            catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | IOException ex16) {
                HttpEvent.logger.log(Level.SEVERE, null, ex16);
                try {
                    this.client.close();
                }
                catch (IOException ex8) {
                    HttpEvent.logger.log(Level.SEVERE, null, ex8);
                }
            }
        }
        else {
            try {
                this.setContentType("text/html");
                this.sendFileContents(Elk.indexFile);
            }
            catch (IOException ex5) {
                HttpEvent.logger.log(Level.SEVERE, null, ex5);
            }
            try {
                this.client.close();
            }
            catch (IOException ex5) {
                HttpEvent.logger.log(Level.SEVERE, null, ex5);
            }
        }
    }
}
