// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver;

import javax.net.ssl.TrustManager;
import javax.net.ssl.KeyManager;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.UnrecoverableKeyException;
import java.security.KeyStoreException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLContext;
import com.google.gson.JsonObject;
import com.razshare.elkserver.Http.HttpEventListener;
import java.net.SocketAddress;
import java.net.InetSocketAddress;
import javax.net.ssl.SSLServerSocket;
import java.net.ServerSocket;
import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import com.razshare.elkserver.SmtpServer.SmtpServer;

public abstract class ElkServer extends Elk
{
    private SmtpServer smtpServer;
    private static ElkServer server;
    
    public static ElkServer getServer() {
        return ElkServer.server;
    }
    
    public static void main(final String[] args) throws IOException, NoSuchAlgorithmException {
        class ConsoleWebServer extends ElkServer
        {
            @Override
            public void init() {
            }
        }
        (ElkServer.server = new ConsoleWebServer()).listen(args);
    }
    
    public abstract void init();
    
    public SmtpServer getSmtpServer() {
        return this.smtpServer;
    }
    
    public void listen(final String[] args) throws IOException, NoSuchAlgorithmException {
        final String settingsPath = new File(args[0]).getParent().toString();
        final String logLineSeparator = "\n";
        Settings.parse(args[0]);
        System.out.println(logLineSeparator + "\n###Reading port");
        if (Settings.isset("port")) {
            ElkServer.port = Settings.getInt("port");
        }
        System.out.println("\t>>>port:" + ElkServer.port + " [OK]");
        System.out.println(logLineSeparator + "\n###Reading bindAddress");
        if (Settings.isset("bindAddress")) {
            ElkServer.bindAddress = Settings.getString("bindAddress");
        }
        System.out.println("\t>>>bindAddress:" + ElkServer.bindAddress + " [OK]");
        System.out.println(logLineSeparator + "\n###Reading webRoot");
        if (Settings.isset("webRoot")) {
            ElkServer.webRoot = new File(args[0]).getParent() + "/" + Settings.getString("webRoot");
        }
        else {
            ElkServer.webRoot = new File(args[0]).getParent() + "/" + ElkServer.webRoot;
        }
        System.out.println("\t>>>webRoot:" + ElkServer.webRoot + " [OK]");
        System.out.println(logLineSeparator + "\n###Reading charset");
        if (Settings.isset("charset")) {
            ElkServer.charset = Settings.getString("charset");
        }
        System.out.println("\t>>>charset:" + ElkServer.charset + " [OK]");
        System.out.println(logLineSeparator + "\n###Reading timeout");
        if (Settings.isset("timeout")) {
            ElkServer.timeout = Settings.getInt("timeout");
        }
        System.out.println("\t>>>timeout:" + ElkServer.timeout + " [OK]");
        System.out.println(logLineSeparator + "\n###Reading controllers");
        final JsonObject controllers = Settings.get("controllers").getAsJsonObject();
        System.out.println("\t>>>controllers:[object] [OK]");
        System.out.println(logLineSeparator + "\n###Reading controllers.http");
        ElkServer.httpControllerPackageName = controllers.get("http").getAsString();
        System.out.println("\t>>>controllers.http:" + ElkServer.httpControllerPackageName + " [OK]");
        System.out.println(logLineSeparator + "\n###Reading controllers.websocket");
        ElkServer.wsControllerPackageName = (controllers.has("websocket") ? controllers.get("websocket").getAsString() : controllers.get("ws").getAsString());
        System.out.println("\t>>>controllers.websocket:" + ElkServer.wsControllerPackageName + " [OK]");
        if (Settings.isset("smtp")) {
            System.out.println(logLineSeparator + "\n###Reading smtp");
            final JsonObject smtp = Settings.get("smtp").getAsJsonObject();
            System.out.println("\t>>>controllers:[object] [OK]");
            if (smtp.has("allow")) {
                ElkServer.smtpAllowed = smtp.get("allow").getAsBoolean();
                System.out.println(logLineSeparator + "\t\n###Reading smtp.allow");
                System.out.println("\t\t>>>smtp.allow:" + ElkServer.smtpAllowed);
                if (ElkServer.smtpAllowed) {
                    String smtpBindAddress = ElkServer.bindAddress;
                    if (smtp.has("bindAddress")) {
                        smtpBindAddress = smtp.get("bindAddress").getAsString();
                    }
                    if (smtp.has("hostname")) {
                        this.smtpServer = new SmtpServer(new ServerSocket(), smtpBindAddress, 25, smtp.get("hostname").getAsString());
                        new Thread(this.smtpServer).start();
                        System.out.println("###Smtp server started.");
                    }
                    else {
                        System.err.println("[WARNING] smtp.hostname is not defined. Smtp server won't start. [WARNING]");
                    }
                }
            }
        }
        if (ElkServer.port == 443) {
            System.out.println(logLineSeparator + "\n###Reading tls");
            final JsonObject tls = Settings.get("tls").getAsJsonObject();
            System.out.println(logLineSeparator + "\t\n###Reading tls.certificate");
            final String tls_certificate = tls.get("certificate").getAsString();
            System.out.println("\t\t>>>tls.certificate:" + tls_certificate + " [OK]");
            System.out.println(logLineSeparator + "\t\n###Reading tls.certificateType");
            final String certificate_type = tls.get("certificateType").getAsString();
            System.out.println("\t\t>>>tls.certificate_type:" + certificate_type + " [OK]");
            System.out.println(logLineSeparator + "\t\n###Reading tls.password");
            final String password = tls.get("password").getAsString();
            System.out.println("\t\t>>>tls.password:***[OK]");
            final SSLContext sslContext = this.createSSLContext(settingsPath + "/" + tls_certificate, certificate_type, password);
            final SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
            final SSLServerSocket ssl = (SSLServerSocket)sslServerSocketFactory.createServerSocket();
            ssl.bind(new InetSocketAddress(ElkServer.bindAddress, ElkServer.port));
            this.init();
            System.out.println("\nServer listening...");
            while (ElkServer.listen) {
                new Thread(new HttpEventListener(ssl.accept())).start();
            }
        }
        else {
            final ServerSocket ss = new ServerSocket();
            ss.bind(new InetSocketAddress(ElkServer.bindAddress, ElkServer.port));
            System.out.println("\nServer listening...");
            this.init();
            while (ElkServer.listen) {
                new Thread(new HttpEventListener(ss.accept())).start();
            }
        }
    }
    
    private SSLContext createSSLContext(final String tlsCertificate, final String certificateType, final String tlsPassword) {
        System.setProperty("https.protocols", "TLSv1.1,TLSv1.2");
        try {
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            final KeyStore keyStore = KeyStore.getInstance(certificateType);
            final InputStream is = new FileInputStream(tlsCertificate);
            keyStore.load(is, tlsPassword.toCharArray());
            is.close();
            keyManagerFactory.init(keyStore, tlsPassword.toCharArray());
            final KeyManager[] km = keyManagerFactory.getKeyManagers();
            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            final TrustManager[] tm = trustManagerFactory.getTrustManagers();
            final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(km, tm, null);
            return sslContext;
        }
        catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex3) {
            ex3.printStackTrace();
            return null;
        }
    }
}
