// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import java.util.Base64;
import java.util.Iterator;
import java.util.Date;
import java.util.TimeZone;
import java.time.temporal.TemporalAccessor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.HashMap;
import java.text.SimpleDateFormat;
import java.util.Map;

public class HttpHeader
{
    private final Map<String, String> header;
    private final Map<String, String[]> cookies;
    private final SimpleDateFormat sdf;
    
    public HttpHeader(final boolean createSuccessHeader) {
        this.header = new HashMap<String, String>();
        this.cookies = new HashMap<String, String[]>();
        this.sdf = new SimpleDateFormat("EEE, dd MMM, yyyy HH:mm:ss z", Locale.US);
        if (createSuccessHeader) {
            this.header.put("Status", "HTTP/1.1 200 OK");
            final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
            this.header.put("Date", dtf.format(LocalDateTime.now()));
            this.header.put("Cache-Control", "no-store");
        }
    }
    
    public HttpHeader() {
        this(true);
    }
    
    public String fieldToString(final String key) {
        final String value = this.header.get(key);
        if (key.equals("Resource") || key.equals("Status") || value.equalsIgnoreCase(key)) {
            return value + "\r\n";
        }
        return key + ": " + value + "\r\n";
    }
    
    public String cookieToString(final String key) {
        this.sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        final String[] c = this.cookies.get(key);
        final Date time = (c[3] == null) ? null : new Date(Integer.parseInt(c[3]) * 1000L);
        return c[4] + ": " + key + "=" + c[0] + ((c[1] == null) ? "" : ("; path=" + c[1])) + ((c[2] == null) ? "" : ("; domain=" + c[2])) + ((c[3] == null) ? "" : ("; expires=" + this.sdf.format(time))) + "\r\n";
    }
    
    @Override
    public String toString() {
        String str = "";
        for (final String key : this.header.keySet()) {
            str += this.fieldToString(key);
        }
        for (final String key : this.cookies.keySet()) {
            str += this.cookieToString(key);
        }
        return str;
    }
    
    public boolean isDefined(final String key) {
        return this.header.get(key) != null;
    }
    
    public void set(final String a, final String b) {
        this.header.put(a, b);
    }
    
    public String get(final String key) {
        switch (key) {
            case "Status":
            case "Resource": {
                return this.header.get(key).split(" ")[1].trim();
            }
            case "Method": {
                return this.header.get(key).split(" ")[0].trim();
            }
            default: {
                return this.header.get(key).trim();
            }
        }
    }
    
    public boolean issetCookie(final String key) {
        Iterator it = this.cookies.entrySet().iterator();
        String tmp = "";
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            tmp = (String) pair.getKey();
            if (tmp.trim().equals(key.trim())) {
                return true;
            }
        }
        return false;
    }
    
    public String getCookie(final String key) {
        final Iterator i = this.cookies.entrySet().iterator();
        String tmp = "";
        String[] tmp2 = new String[5];
        while (i.hasNext()) {
            Map.Entry pair = (Map.Entry) i.next();
            tmp = (String) pair.getKey();
            if (tmp.trim().equals(key.trim())) {
                tmp2 = (String[]) pair.getValue();
                return new String(Base64.getDecoder().decode(tmp2[0]));
            }
        }
        return null;
    }
    
    public void setCookie(final String key, final String v, final String path, final String domain, final String expire) {
        final String[] b = { new String(Base64.getEncoder().encode(v.getBytes())), path, domain, expire, "Set-Cookie" };
        this.cookies.put(key.trim(), b);
    }
    
    public void setCookie(final String key, final String v, final String path, final String domain) {
        this.setCookie(key, v, path, domain, null);
    }
    
    public void setCookie(final String key, final String v, final String path) {
        this.setCookie(key, v, path, null, null);
    }
    
    public void setCookie(final String key, final String v) {
        this.setCookie(key, v, "/", null, null);
    }
    
    public Map<String, String> getMap() {
        return this.header;
    }
    
    public static HttpHeader fromString(final String string) {
        final HttpHeader header = new HttpHeader(false);
        final String[] tmp = string.split("\\r\\n");
        final boolean end = false;
        for (int i = 0; i < tmp.length; ++i) {
            if (!tmp[i].equals("")) {
                final String[] item = tmp[i].split(":(?=\\s)");
                if (item.length > 1) {
                    if (item[0].equals("Cookie")) {
                        final String[] c = item[1].split(";");
                        for (int j = 0; j < c.length; ++j) {
                            final String[] cookieInfo = c[j].split("=(?!\\s|\\s|$)");
                            if (cookieInfo.length > 1) {
                                final String[] b = { cookieInfo[1], (cookieInfo.length > 2) ? cookieInfo[2] : null, (cookieInfo.length > 3) ? cookieInfo[3] : null, (cookieInfo.length > 3) ? cookieInfo[3] : null, "Cookie" };
                                header.cookies.put(cookieInfo[0], b);
                            }
                        }
                    }
                    else {
                        header.set(item[0], item[1]);
                    }
                }
                else if (tmp[i].substring(0, 3).equals("GET")) {
                    header.set("Resource", tmp[i]);
                    header.set("Method", "GET");
                }
                else if (tmp[i].substring(0, 4).equals("POST")) {
                    header.set("Resource", tmp[i]);
                    header.set("Method", "POST");
                }
                else {
                    header.set(tmp[i], tmp[i]);
                }
            }
        }
        return header;
    }
}
