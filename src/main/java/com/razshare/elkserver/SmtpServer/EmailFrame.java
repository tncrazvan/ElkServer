// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.SmtpServer;

public class EmailFrame
{
    private final String message;
    private final String[] contentType;
    
    public EmailFrame(final String message, final String contentType, final String charset) {
        this.message = message;
        this.contentType = new String[] { contentType, charset };
    }
    
    public String getContentTye() {
        return this.contentType[0];
    }
    
    public String getCharset() {
        return this.contentType[1];
    }
    
    @Override
    public String toString() {
        return this.message;
    }
}
