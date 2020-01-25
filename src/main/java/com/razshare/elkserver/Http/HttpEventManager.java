// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import java.io.OutputStream;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.util.Map;
import java.io.File;
import com.razshare.elkserver.Elk;
import java.io.IOException;
import java.util.logging.Level;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.io.DataOutputStream;
import com.razshare.elkserver.EventManager;

public abstract class HttpEventManager extends EventManager
{
    private final DataOutputStream output;
    private boolean defaultHeaders;
    private boolean alive;
    protected final Socket client;
    protected final String content;
    public static final String STATUS_CONTINUE = "100 Continue";
    public static final String STATUS_SWITCHING_PROTOCOLS = "101 Switching Protocols";
    public static final String STATUS_PROCESSING = "102 Processing";
    public static final String STATUS_SUCCESS = "200 OK";
    public static final String STATUS_CREATED = "201 CREATED";
    public static final String STATUS_ACCEPTED = "202 ACCEPTED";
    public static final String STATUS_NON_AUTHORITATIVE_INFORMATION = "203 Non-Authoritative Information";
    public static final String STATUS_NO_CONTENT = "204 No Content";
    public static final String STATUS_RESET_CONTENT = "205 Reset Content";
    public static final String STATUS_PARTIAL_CONTENT = "206 Partial Content";
    public static final String STATUS_MULTI_STATUS = "207 Multi-Status";
    public static final String STATUS_ALREADY_REPORTED = "208 Already Reported";
    public static final String STATUS_IM_USED = "226 IM Used";
    public static final String STATUS_MULTIPLE_CHOICES = "300 Multiple Choices";
    public static final String STATUS_MOVED_PERMANENTLY = "301 Moved Permanently";
    public static final String STATUS_FOUND = "302 Found";
    public static final String STATUS_SEE_OTHER = "303 See Other";
    public static final String STATUS_NOT_MODIFIED = "304 Not Modified";
    public static final String STATUS_USE_PROXY = "305 Use Proxy";
    public static final String STATUS_SWITCH_PROXY = "306 Switch Proxy";
    public static final String STATUS_TEMPORARY_REDIRECT = "307 Temporary Redirect";
    public static final String STATUS_PERMANENT_REDIRECT = "308 Permanent Redirect";
    public static final String STATUS_BAD_REQUEST = "400 Bad Request";
    public static final String STATUS_UNAUTHORIZED = "401 Unauthorized";
    public static final String STATUS_PAYMENT_REQUIRED = "402 Payment Required";
    public static final String STATUS_FORBIDDEN = "403 Forbidden";
    public static final String STATUS_NOT_FOUND = "404 Not Found";
    public static final String STATUS_METHOD_NOT_ALLOWED = "405 Method Not Allowed";
    public static final String STATUS_NOT_ACCEPTABLE = "406 Not Acceptable";
    public static final String STATUS_PROXY_AUTHENTICATION_REQUIRED = "407 Proxy Authentication Required";
    public static final String STATUS_REQUEST_TIMEOUT = "408 Request Timeout";
    public static final String STATUS_CONFLICT = "409 Conflict";
    public static final String STATUS_GONE = "410 Gone";
    public static final String STATUS_LENGTH_REQUIRED = "411 Length Required";
    public static final String STATUS_PRECONDITION_FAILED = "412 Precondition Failed";
    public static final String STATUS_PAYLOAD_TOO_LARGE = "413 Payload Too Large";
    public static final String STATUS_URI_TOO_LONG = "414 URI Too Long";
    public static final String STATUS_UNSUPPORTED_MEDIA_TYPE = "415 Unsupported Media Type";
    public static final String STATUS_RANGE_NOT_SATISFIABLE = "416 Range Not Satisfiable";
    public static final String STATUS_EXPECTATION_FAILED = "417 Expectation Failed";
    public static final String STATUS_IM_A_TEAPOT = "418 I'm a teapot";
    public static final String STATUS_MISDIRECTED_REQUEST = "421 Misdirected Request";
    public static final String STATUS_UNPROCESSABLE_ENTITY = "422 Unprocessable Entity";
    public static final String STATUS_LOCKED = "423 Locked";
    public static final String STATUS_FAILED_DEPENDENCY = "426 Failed Dependency";
    public static final String STATUS_UPGRADE_REQUIRED = "428 Upgrade Required";
    public static final String STATUS_PRECONDITION_REQUIRED = "429 Precondition Required";
    public static final String STATUS_TOO_MANY_REQUESTS = "429 Too Many Requests";
    public static final String STATUS_REQUEST_HEADER_FIELDS_TOO_LARGE = "431 Request Header Fields Too Large";
    public static final String STATUS_UNAVAILABLE_FOR_LEGAL_REASONS = "451 Unavailable For Legal Reasons";
    public static final String STATUS_INTERNAL_SERVER_ERROR = "500 Internal Server Error";
    public static final String STATUS_NOT_IMPLEMENTED = "501 Not Implemented";
    public static final String STATUS_BAD_GATEWAY = "502 Bad Gateway";
    public static final String STATUS_SERVICE_UNAVAILABLE = "503 Service Unavailable";
    public static final String STATUS_GATEWAY_TIMEOUT = "504 Gateway Timeout";
    public static final String STATUS_HTTP_VERSION_NOT_SUPPORTED = "505 HTTP Version Not Supported";
    public static final String STATUS_VARIANT_ALSO_NEGOTATIES = "506 Variant Also Negotiates";
    public static final String STATUS_INSUFFICIENT_STORAGE = "507 Insufficient Storage";
    public static final String STATUS_LOOP_DETECTED = "508 Loop Detected";
    public static final String STATUS_NOT_EXTENDED = "510 Not Extended";
    public static final String STATUS_NETWORK_AUTHENTICATION_REQUIRED = "511 Network Authentication Required";
    private boolean firstMessage;
    
    public HttpEventManager(final DataOutputStream output, final HttpHeader clientHeader, final Socket client, final String content) throws UnsupportedEncodingException {
        super(clientHeader);
        this.defaultHeaders = true;
        this.alive = true;
        this.firstMessage = true;
        this.client = client;
        this.output = output;
        this.content = content;
    }
    
    public void close() {
        try {
            this.client.close();
        }
        catch (IOException ex) {
            HttpEventManager.logger.log(Level.WARNING, null, ex);
        }
    }
    
    public Socket getClient() {
        return this.client;
    }
    
    public void setHeaderField(final String fieldName, final String fieldContent) {
        this.header.set(fieldName, fieldContent);
    }
    
    public void setStatus(final String status) {
        this.setHeaderField("Status", "HTTP/1.1 " + status);
    }
    
    public String getHeaderField(final String fieldName) {
        return this.header.get(fieldName);
    }
    
    public HttpHeader getHeader() {
        return this.header;
    }
    
    public HttpHeader getClientHeader() {
        return this.clientHeader;
    }
    
    public String getMethod() {
        return this.clientHeader.get("Method");
    }
    
    public boolean isAlive() {
        return this.alive;
    }
    
    public boolean execute() throws IOException {
        this.findUserLanguages();
        final File f = new File(Elk.webRoot + this.location);
        this.header.set("Content-Type", Elk.processContentType(this.location));
        if (f.exists()) {
            if (!f.isDirectory()) {
                this.sendFileContents(f);
            }
            else {
                this.header.set("Content-Type", "text/plain");
                this.onControllerRequest(this.location);
            }
        }
        else if (this.location.length() >= 2 && this.location.substring(1, 2).equals("@")) {
            if (this.header.get("Content-Type").equals("")) {
                this.header.set("Content-Type", "text/html");
            }
            this.onControllerRequest(this.location);
        }
        else {
            this.header.set("Content-Type", "text/html");
            try {
                Class.forName(HttpEventManager.httpControllerPackageName + "." + this.location.substring(1).split("[#?&/\\\\]")[0]);
                this.sendFileContents(HttpEventManager.indexFile);
            }
            catch (ClassNotFoundException ex) {
                this.onControllerRequest("/@" + Elk.httpControllerNotFound);
            }
            this.client.close();
        }
        this.client.close();
        return true;
    }
    
    abstract void onControllerRequest(final String p0);
    
    public Map<String, String> getUserLanguages() {
        return this.userLanguages;
    }
    
    public String getUserDefaultLanguage() {
        return this.userLanguages.get("DEFAULT-LANGUAGE");
    }
    
    public String getUserAgent() {
        return this.clientHeader.get("User-Agent");
    }
    
    public void setUserObject(final String name, final Object o) throws IOException {
        this.send("<script>window." + name + "=" + Elk.JSON_PARSER.toJson(o) + ";</script>\n");
    }
    
    public void setUserObject(final String name, final JsonObject o) {
        this.send("<script>window." + name + "=" + o.toString() + ";</script>\n");
    }
    
    public void setUserArray(final String name, final JsonArray a) {
        this.send("<script>window." + name + "=" + a.toString() + ";</script>\n");
    }
    
    public void sendHeaders() {
        this.firstMessage = false;
        try {
            this.output.write((this.header.toString() + "\r\n").getBytes(Elk.charset));
            this.output.flush();
            this.alive = true;
        }
        catch (IOException ex) {
            ex.printStackTrace();
            this.alive = false;
            try {
                this.client.close();
            }
            catch (IOException ex2) {
                HttpEventManager.logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public void send(final byte[] data) {
        if (this.alive) {
            if (this.firstMessage && this.defaultHeaders) {
                this.sendHeaders();
            }
            try {
                this.output.write(data);
                this.output.flush();
                this.alive = true;
            }
            catch (IOException ex) {
                ex.printStackTrace();
                this.alive = false;
                try {
                    this.client.close();
                }
                catch (IOException ex2) {
                    HttpEventManager.logger.log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public void flushHeaders() {
        this.flush();
    }
    
    public void flush() {
        this.sendHeaders();
    }
    
    public void send(final String data) {
        try {
            this.send(data.getBytes(Elk.charset));
        }
        catch (UnsupportedEncodingException ex) {
            HttpEventManager.logger.log(Level.SEVERE, null, ex);
        }
    }
    
    public void send(final int data) {
        this.send("" + data);
    }
    
    public void setContentType(final String type) {
        this.header.set("Content-Type", type);
    }
    
    public void sendFileContents(final String filename) throws IOException {
        this.sendFileContents(new File(Elk.webRoot + filename));
    }
    
    public void disableDefaultHeaders() {
        this.defaultHeaders = false;
    }
    
    public void enableDefaultHeaders() {
        this.defaultHeaders = true;
    }
    
    private void sendFileContents(final File f) {
        FileInputStream fis = null;
        try {
            final int BUFF_SIZE = 65000;
            final byte[] buffer = new byte[BUFF_SIZE];
            fis = new FileInputStream(f);
            final OutputStream os = this.client.getOutputStream();
            if (os != null) {
                if (this.firstMessage && this.defaultHeaders) {
                    this.firstMessage = false;
                    this.header.set("Content-Length", "" + f.length());
                    os.write((this.header.toString() + "\r\n").getBytes());
                }
                int byteRead = 0;
                int counter = 0;
                while ((byteRead = fis.read(buffer)) != -1 && counter < f.length()) {
                    counter += byteRead;
                    os.write(buffer, 0, byteRead);
                }
                os.close();
            }
        }
        catch (FileNotFoundException ex) {
            HttpEventManager.logger.log(Level.INFO, null, ex);
        }
        catch (IOException ex3) {
            System.out.println("Client " + this.client.getInetAddress().toString() + " disconnected before receiving the whole file (" + f.getName() + ")");
        }
        finally {
            try {
                fis.close();
            }
            catch (IOException ex2) {
                HttpEventManager.logger.log(Level.WARNING, null, ex2);
            }
        }
    }
}
