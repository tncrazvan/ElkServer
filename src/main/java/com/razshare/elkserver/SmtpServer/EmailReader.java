// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.SmtpServer;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class EmailReader extends SmtpMessageManager
{
    String clientHostName;
    String currentFrameContent;
    String type;
    String boundaryId;
    String subject;
    String sender;
    String line;
    String[] contentType;
    boolean readingFrame;
    boolean readingBody;
    int currentFrame;
    int lastFrame;
    ArrayList<EmailFrame> frames;
    boolean checkHELO;
    boolean checkEHLO;
    boolean checkMAIL_FROM;
    boolean checkDATA;
    boolean checkSUBJECT;
    boolean checkQUIT;
    ArrayList<String> checkRCPT_TO;
    ArrayList<SmtpListener> listeners;
    
    public EmailReader(final SmtpServer server, final Socket client, final ArrayList<SmtpListener> listeners) throws IOException {
        super(server, client);
        this.clientHostName = "";
        this.currentFrameContent = "";
        this.type = "";
        this.boundaryId = "";
        this.subject = "";
        this.sender = "";
        this.contentType = null;
        this.readingFrame = false;
        this.readingBody = false;
        this.currentFrame = 0;
        this.lastFrame = 0;
        this.frames = new ArrayList<EmailFrame>();
        this.checkHELO = false;
        this.checkEHLO = false;
        this.checkMAIL_FROM = false;
        this.checkDATA = false;
        this.checkSUBJECT = false;
        this.checkQUIT = false;
        this.checkRCPT_TO = new ArrayList<String>();
        this.listeners = listeners;
    }
    
    public void parse() throws IOException {
        this.sayReady();
        this.line = this.read();
        while (this.line != null && !this.checkQUIT) {
            if (this.checkDATA) {
                this.readData();
            }
            else if (this.isHelo(this.line) && !this.checkHELO) {
                this.sayOk(this.hostname + ", I'm glad to meet you");
                this.checkHELO = true;
            }
            else if (this.isEhlo(this.line) && !this.checkEHLO) {
                this.checkEHLO = true;
                this.clientHostName = this.jumpOnceAndGetRemaining(this.line);
                this.sayOkExtended(this.hostname + " Hello " + this.clientHostName);
                this.sayOkExtended("SIZE 14680064");
                this.sayOkExtended("PIPELINING");
                this.sayOk("HELP");
            }
            else if (this.isMailFrom(this.line) && !this.checkMAIL_FROM) {
                this.checkMAIL_FROM = true;
                this.sender = this.getMailAddress(this.line);
                this.sayOk("Ok");
            }
            else if (this.isRecipient(this.line)) {
                this.sayOk("Ok");
                this.checkRCPT_TO.add(this.getMailAddress(this.line));
            }
            else if (this.isData(this.line) && !this.checkDATA) {
                this.checkDATA = true;
                this.sayEndDataWith();
            }
            else if (this.isQuit(this.line) && !this.checkQUIT) {
                this.checkQUIT = true;
                this.sayBye();
                this.client.close();
                this.listeners.forEach(listener -> listener.onEmailReceived(new Email(this.subject, this.frames, this.sender, this.checkRCPT_TO)));
            }
            if (!this.checkQUIT) {
                this.line = this.read();
            }
            else {
                if (!this.client.isConnected()) {
                    continue;
                }
                this.client.close();
            }
        }
    }
    
    private void readData() throws IOException {
        if (this.line.length() > 1 && this.line.substring(0, 2).equals("..")) {
            this.line = this.line.substring(1);
        }
        if (this.isNewBoundary(this.line, this.boundaryId)) {
            this.saveFrame();
        }
        else if (this.isLastBoundary(this.line, this.boundaryId)) {
            this.saveLastFrame();
        }
        else if (this.isEndOfData(this.line)) {
            this.closeAndNotifyListeners();
        }
        else if (this.readingBody) {
            this.continueReadingBody();
        }
        else if (this.currentFrame > this.lastFrame || this.readingFrame) {
            this.readNewFrame();
        }
        else {
            this.readCurrentFrameHeaders();
        }
    }
    
    private void readCurrentFrameHeaders() throws IOException {
        if (this.isSubject(this.line) && !this.checkSUBJECT) {
            this.checkSUBJECT = true;
            this.subject = this.getSubject(this.line);
        }
        else if (this.isFrom(this.line)) {
            this.sender = this.getMailAddress(this.line);
        }
        else if (this.isContentType(this.line)) {
            this.contentType = new String[] { this.getContentType(this.line), this.getBoundary(this.line) };
            if (!this.type.trim().equals("multipart/alternative")) {
                this.sayByeAndClose();
            }
        }
    }
    
    private void readNewFrame() {
        this.readingFrame = true;
        if (this.isContentType(this.line)) {
            this.contentType = new String[] { this.getContentType(this.line), this.getCharset(this.line) };
            this.type = this.contentType[0].trim();
        }
    }
    
    private void continueReadingBody() {
        if (this.currentFrame > 0) {
            this.currentFrameContent += "\r\n";
        }
        if (this.isContentType(this.line)) {
            this.contentType = new String[] { this.getContentType(this.line), this.getCharset(this.line) };
        }
        else {
            this.currentFrameContent += this.line;
        }
    }
    
    private void closeAndNotifyListeners() throws IOException {
        this.checkDATA = false;
        this.sayOkAndQueue(12345);
        this.checkQUIT = true;
        this.sayByeAndClose();
        this.listeners.forEach(listener -> listener.onEmailReceived(new Email(this.subject, this.frames, this.sender, this.checkRCPT_TO)));
    }
    
    private void saveLastFrame() {
        this.frames.add(new EmailFrame(this.currentFrameContent, this.contentType[0], this.contentType[1]));
        this.readingFrame = false;
        this.readingBody = false;
    }
    
    private void saveFrame() {
        this.readingBody = true;
        if (this.currentFrame > 0) {
            this.frames.add(new EmailFrame(this.currentFrameContent, this.contentType[0], this.contentType[1]));
        }
        ++this.currentFrame;
    }
}
