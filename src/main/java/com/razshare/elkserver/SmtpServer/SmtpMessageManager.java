// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.SmtpServer;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Date;
import com.razshare.elkserver.Elk;
import java.io.IOException;
import java.io.Reader;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintWriter;

public abstract class SmtpMessageManager
{
    protected final PrintWriter sockout;
    protected final BufferedReader sockin;
    protected final String hostname;
    protected final Socket client;
    private String boundary;
    DateFormat datePattern;
    
    public SmtpMessageManager(final SmtpServer server, final Socket client) throws IOException {
        this.datePattern = new SimpleDateFormat("E, d M Y H:m:s Z");
        this.sockout = new PrintWriter(client.getOutputStream(), true);
        this.sockin = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.hostname = server.getHostname();
        this.client = client;
    }
    
    protected String read() throws IOException {
        final String tmp = this.sockin.readLine();
        System.out.println("Remote:" + tmp);
        return tmp;
    }
    
    protected void setMultipartBoundaryId(final String id) {
        this.boundary = id;
    }
    
    protected boolean isNewBoundary(final String line, final String id) {
        return Elk.matchRegex(line, "(?<=^--)" + id);
    }
    
    protected boolean isLastBoundary(final String line, final String id) {
        return Elk.matchRegex(line, "(?<=^--)" + id + "(?=--$)");
    }
    
    protected boolean isReady(final String line) {
        return Elk.matchRegex(line, "^220");
    }
    
    protected boolean isOk(final String line) {
        return Elk.matchRegex(line, "^250");
    }
    
    protected boolean isOkExtended(final String line) {
        return Elk.matchRegex(line, "^250-.+");
    }
    
    protected boolean isOkSize(final String line) {
        return Elk.matchRegex(line, "^250-SIZE [0-9]+");
    }
    
    protected boolean isOkPipelining(final String line) {
        return Elk.matchRegex(line, "^250-PIPELINING");
    }
    
    protected boolean isOkHelp(final String line) {
        return Elk.matchRegex(line, "^250 HELP");
    }
    
    protected boolean isEndDataWith(final String line) {
        return Elk.matchRegex(line, "^354");
    }
    
    protected String getEndDataWithValue(final String line) {
        return Elk.extractRegexGroup(line, "(?<=\\s)[A-z0-9\\<\\>\\.]+", -1);
    }
    
    protected boolean isHelo(final String line) {
        return Elk.matchRegex(line, "^HELO");
    }
    
    protected boolean isEhlo(final String line) {
        return Elk.matchRegex(line, "^EHLO");
    }
    
    protected boolean isMailFrom(final String line) {
        return Elk.matchRegex(line, "^MAIL FROM:");
    }
    
    protected String getMailAddress(final String line) {
        return Elk.extractRegex(line, "(?<=\\z)[A-z0-9!#$%&'*+\\-\\/=?^_`{|}~.]+@[A-z0-9\\-.]+(?=\\>)");
    }
    
    protected boolean isRecipient(final String line) {
        return Elk.matchRegex(line, "^RCPT TO:");
    }
    
    protected boolean isData(final String line) {
        return Elk.matchRegex(line, "^DATA");
    }
    
    protected boolean isEndOfData(final String line) {
        return Elk.matchRegex(line, "^\\.$");
    }
    
    protected boolean isQuit(final String line) {
        return Elk.matchRegex(line, "^QUIT");
    }
    
    protected boolean isContentType(final String line) {
        return Elk.matchRegex(line, "^Content-Type");
    }
    
    protected String getContentType(final String line) {
        return Elk.extractRegex(line, "(?<=^Content-Type:\\s)[A-z0-9]+\\/[A-z0-9]+");
    }
    
    protected String getCharset(final String line) {
        return Elk.extractRegex(line, "(?<=charset=\\\")[A-z0-9\\-\\s]+(?=\\\")");
    }
    
    protected boolean isSubject(final String line) {
        return Elk.matchRegex(line, "^Subject");
    }
    
    protected String getSubject(final String line) {
        return Elk.extractRegex(line, "(?<=^Subject:).*");
    }
    
    protected String getBoundary(final String line) {
        return Elk.extractRegex(line, "(?<=boundary\\=\\\")[A-z0-9]+(?=\\\")");
    }
    
    protected boolean isFrom(final String line) {
        return Elk.matchRegex(line, "^From");
    }
    
    protected String getNickname(final String line) {
        return Elk.extractRegex(line, "(?<=From:).+(?=\\<)");
    }
    
    protected String jumpOnceAndGetRemaining(final String line) {
        return Elk.extractRegex(line, "(?<=\\s).+");
    }
    
    protected void say(final int code, final String extra) {
        this.say(code + extra);
    }
    
    protected void say(final int code) {
        this.say(code, "");
    }
    
    protected void say(final String value) {
        System.out.println("Local:" + value);
        this.sockout.println(value);
    }
    
    protected void sayReady() {
        this.say(220, " " + this.hostname + " ESMTP Postfix");
    }
    
    protected void sayHelo() {
        this.say("HELO " + this.hostname);
    }
    
    protected void sayEhlo() {
        this.say("EHLO " + this.hostname);
    }
    
    protected void sayMailFrom(final String address) {
        this.say("MAIL FROM:<" + address + ">");
    }
    
    protected void sayRecipient(final String address) {
        this.say("RCPT TO:<" + address + ">");
    }
    
    protected void sayData() {
        this.say("DATA");
    }
    
    protected void sayDataFrom(final String address) {
        this.say("From: " + address.split("@")[0] + " <" + address + ">");
    }
    
    protected void sayDataSubject(final String subject) {
        this.say("Subject: " + subject);
    }
    
    protected void sayDataDate(final long unixTime) {
        this.say("Date: " + this.datePattern.format(new Date(unixTime)));
    }
    
    protected void sayDataTo(final String[] recipients) {
        String tmp = "To: ";
        for (int i = 0; i < recipients.length; ++i) {
            tmp += recipients[i];
            if (i < recipients.length - 1) {
                tmp += ", ";
            }
        }
        this.say(tmp);
    }
    
    protected void sayDataContentType() {
        this.say("Content-Type: multipart/alternative; boundary=\"" + this.boundary + "\"");
    }
    
    protected void sayDataFrames(final ArrayList<EmailFrame> frames) {
        for (final EmailFrame frame : frames) {
            this.say("Content-Type: " + frame.getContentTye() + "; charset=\"" + frame.getCharset() + "\"");
            this.sayNothing();
            this.say(frame.toString());
        }
        this.sayEndBoundary();
    }
    
    protected void sayNothing() {
        this.say("");
    }
    
    protected void sayQuit() {
        this.say("QUIT");
    }
    
    protected void sayQuitAndClose() throws IOException {
        this.sayQuit();
        this.client.close();
    }
    
    protected void sayNewBoundary() {
        this.say("--" + this.boundary);
    }
    
    protected void sayEndBoundary() {
        this.say("--" + this.boundary + "--");
    }
    
    protected void sayOkExtended(final String params) {
        this.say(250, "-" + params);
    }
    
    protected void sayOk(final String params) {
        this.say(250, " " + params);
    }
    
    protected void sayOk() {
        this.sayOk("");
    }
    
    protected void sayEndDataWith() {
        this.say(354, " End data with <CR><LF>.<CR><LF>");
    }
    
    protected void sayOkAndQueue(final int index) {
        this.say(250, " Ok: queued as " + index);
    }
    
    protected void sayBye() {
        this.say(221, " Bye");
    }
    
    protected void sayByeAndClose() throws IOException {
        if (this.client.isConnected()) {
            this.sayBye();
            this.client.close();
        }
    }
}
