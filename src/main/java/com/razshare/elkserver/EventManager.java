// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import com.razshare.elkserver.Http.HttpHeader;

public abstract class EventManager extends Elk
{
    protected final HttpHeader clientHeader;
    protected final Map<String, String> queryString;
    protected final String location;
    protected final Map<String, String> userLanguages;
    protected final HttpHeader header;
    
    public EventManager(final HttpHeader clientHeader) throws UnsupportedEncodingException {
        this.queryString = new HashMap<String, String>();
        this.userLanguages = new HashMap<String, String>();
        this.header = new HttpHeader();
        this.clientHeader = clientHeader;
        final String[] parts = URLDecoder.decode(clientHeader.get("Resource"), EventManager.charset).split("\\?");
        this.location = parts[0];
        if (parts.length > 1) {
            final String[] split;
            final String[] tmp = split = parts[1].split("\\&");
            for (final String part : split) {
                final String[] object = part.split("=", 2);
                if (object.length > 1) {
                    this.queryString.put(object[0].trim(), object[1]);
                }
                else {
                    this.queryString.put(object[0].trim(), "");
                }
            }
        }
    }
    
    public boolean issetUrlQuery(final String key) {
        return this.queryString.containsKey(key);
    }
    
    public String getUrlQuery(final String key) {
        return this.queryString.get(key);
    }
    
    protected void findUserLanguages() {
        if (this.clientHeader.get("Accept-Language") == null) {
            this.userLanguages.put("unknown", "unknown");
        }
        else {
            String[] tmp = new String[2];
            final String[] languages = this.clientHeader.get("Accept-Language").split(",");
            this.userLanguages.put("DEFAULT-LANGUAGE", languages[0]);
            for (int i = 1; i < languages.length; ++i) {
                tmp = languages[i].split(";");
                this.userLanguages.put(tmp[0], tmp[1]);
            }
        }
    }
    
    public void unsetCookie(final String key, final String path, final String domain) {
        this.header.setCookie(key, "deleted", path, domain, "0");
    }
    
    public void unsetCookie(final String key, final String path) {
        this.unsetCookie(key, path, this.clientHeader.get("Host"));
    }
    
    public void unsetCookie(final String key) {
        this.unsetCookie(key, "/", this.clientHeader.get("Host"));
    }
    
    public void setCookie(final String name, final String value, final String path, final String domain, final String expire) {
        this.header.setCookie(name, value, path, domain, expire);
    }
    
    public void setCookie(final String name, final String value, final String path, final String domain) {
        this.header.setCookie(name, value, path, domain);
    }
    
    public void setCookie(final String name, final String value, final String path) {
        this.header.setCookie(name, value, path);
    }
    
    public void setCookie(final String name, final String value) {
        this.header.setCookie(name, value);
    }
    
    public String getCookie(final String name) {
        return this.clientHeader.getCookie(name);
    }
    
    public boolean issetCookie(final String key) {
        return this.clientHeader.issetCookie(key);
    }
    
    public boolean cookieIsset(final String key) {
        return this.issetCookie(key);
    }
}
