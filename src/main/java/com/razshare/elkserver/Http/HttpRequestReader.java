// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import java.util.logging.Level;
import java.net.SocketTimeoutException;
import java.io.EOFException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.io.Writer;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import javax.net.ssl.SSLSocket;
import java.net.Socket;
import com.razshare.elkserver.Elk;

public abstract class HttpRequestReader extends Elk implements Runnable
{
    protected Socket client;
    protected SSLSocket secureClient;
    protected BufferedReader reader;
    protected BufferedWriter writer;
    protected final DataOutputStream output;
    protected final DataInputStream input;
    private String outputString;
    
    public HttpRequestReader(final Socket client) throws NoSuchAlgorithmException, IOException {
        this.client = null;
        this.secureClient = null;
        this.reader = null;
        this.writer = null;
        this.outputString = "";
        this.client = client;
        this.reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        this.output = new DataOutputStream(client.getOutputStream());
        this.input = new DataInputStream(client.getInputStream());
    }
    
    @Override
    public void run() {
        try {
            byte[] chain = { 0, 0, 0, 0 };
            boolean keepReading = true;
            boolean EOFException = false;
            while (keepReading) {
                try {
                    chain[3] = chain[2];
                    chain[2] = chain[1];
                    chain[1] = chain[0];
                    chain[0] = this.input.readByte();
                    this.outputString += (char)chain[0];
                    if ((char)chain[3] != '\r' || (char)chain[2] != '\n' || (char)chain[1] != '\r' || (char)chain[0] != '\n') {
                        continue;
                    }
                    keepReading = false;
                }
                catch (EOFException ex2) {
                    keepReading = false;
                    EOFException = true;
                }
            }
            final HttpHeader clientHeader = HttpHeader.fromString(this.outputString);
            this.outputString = "";
            if ((clientHeader.get("Method").equals("POST") || HttpRequestReader.port == 25) && !EOFException) {
                int chunkSize = 0;
                if (clientHeader.isDefined("Content-Length")) {
                    chunkSize = Integer.parseInt(clientHeader.get("Content-Length"));
                }
                if (chunkSize > 0) {
                    chain = new byte[chunkSize];
                    this.input.readFully(chain);
                    this.outputString = new String(chain, HttpRequestReader.charset);
                }
                else {
                    int offset = 0;
                    chain = new byte[65536];
                    try {
                        while (this.input.read(chain) > 0) {
                            if (offset < 65536) {
                                ++offset;
                            }
                            else {
                                this.outputString = new String(chain, HttpRequestReader.charset);
                                offset = 0;
                                chain = new byte[65536];
                            }
                        }
                    }
                    catch (SocketTimeoutException e) {
                        this.outputString = new String(chain, HttpRequestReader.charset);
                    }
                }
            }
            this.onRequest(clientHeader, this.outputString);
        }
        catch (IOException ex) {
            try {
                this.client.close();
            }
            catch (IOException ex3) {
                HttpRequestReader.logger.log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public abstract void onRequest(final HttpHeader p0, final String p1);
}
