// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.spec.InvalidKeySpecException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.Arrays;
import java.io.File;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;
import com.razshare.elkserver.Http.HttpEvent;
import com.razshare.elkserver.Controller.Http.Isset;
import com.razshare.elkserver.Controller.Http.Set;
import com.razshare.elkserver.Controller.Http.Get;
import com.razshare.elkserver.Controller.Http.ControllerNotFound;
import java.util.Base64;
import com.google.gson.JsonParser;
import com.google.gson.Gson;
import java.util.Date;
import java.util.Calendar;
import com.google.gson.JsonObject;
import com.razshare.elkserver.WebSocket.WebSocketEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class Elk
{
    protected static boolean listen;
    protected static boolean groupsAllowed;
    protected static boolean smtpAllowed;
    protected static int port;
    protected static int timeout;
    protected static String webRoot;
    protected static String charset;
    protected static String bindAddress;
    protected static String httpControllerPackageName;
    protected static String wsControllerPackageName;
    protected static String httpControllerNotFound;
    protected static String wsControllerNotFound;
    protected static final Logger logger;
    protected static final Map<String, ArrayList<WebSocketEvent>> WS_EVENTS;
    protected static final int cookieTtl = 3600;
    protected static final int wsGroupMaxClient = 10;
    protected static final int wsMtu = 65536;
    protected static final int httpMtu = 65536;
    protected static final int cacheMaxAge = 31536000;
    protected static String wsAcceptKey;
    protected static JsonObject mainSettings;
    public static final Calendar calendar;
    protected static String indexFile;
    protected static final Date date;
    protected static final Gson JSON_PARSER;
    protected static final JsonParser JSONPARSER;
    protected static boolean running;
    protected static final Base64.Encoder BASE64_ENCODER;
    protected static final Base64.Decoder BASE64_DECODER;
    private static final String patternLeftStart = "<\\s*(?=script)";
    private static final String patternLeftEnd = "<\\s*\\/\\s*(?=script)";
    private static final String patternRightEnd = "(?<=&lt;\\/script)>";
    private static final String patternRightStart1 = "(?<=\\&lt\\;script)\\s*>";
    private static final String patternRightStart2 = "(?<=\\&lt\\;script).*\\s*>";
    private static ControllerNotFound httpcnf;
    protected static com.razshare.elkserver.Controller.WebSocket.ControllerNotFound webSocketNotFound;
    protected static Get get;
    protected static Set set;
    protected static Isset isset;
    private static final char[] MULTIPART_CHARS;
    
    protected static void httpNotFound(final HttpEvent e, final ArrayList<String> path, final String content) {
        Elk.httpcnf.main(e, path, content);
    }
    
    public static Map<String, String> readAsMultipartFormData(final String content) {
        final Map<String, String> object = new HashMap<String, String>();
        final String[] lines = content.split("\r\n");
        String currentLabel = null;
        String currentValue = "";
        final Pattern pattern1 = Pattern.compile("^Content-Disposition");
        final Pattern pattern2 = Pattern.compile("(?<=name\\=\\\").*?(?=\\\")");
        final boolean next = false;
        final boolean skippedBlank = false;
        for (int i = 0; i < lines.length; ++i) {
            Matcher matcher = pattern1.matcher(lines[i]);
            if (matcher.find()) {
                matcher = pattern2.matcher(lines[i]);
                if (matcher.find() && currentLabel == null) {
                    currentLabel = matcher.group();
                    i += 2;
                    currentValue = lines[i];
                    object.put(currentLabel, currentValue);
                    currentLabel = null;
                }
            }
        }
        return object;
    }
    
    public static String generateMultipartBoundary() {
        final StringBuilder buffer = new StringBuilder();
        final Random rand = new Random();
        for (int count = rand.nextInt(11) + 30, i = 0; i < count; ++i) {
            buffer.append(Elk.MULTIPART_CHARS[rand.nextInt(Elk.MULTIPART_CHARS.length)]);
        }
        return buffer.toString();
    }
    
    public static String capitalize(String value) {
        value = value.toLowerCase();
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }
    
    public static String escapeJavaScript(final String js) {
        return js.replaceAll("<\\s*(?=script)", "&lt;").replaceAll("<\\s*\\/\\s*(?=script)", "&lt;/").replaceAll("(?<=&lt;\\/script)>", "&gt;").replaceAll("(?<=\\&lt\\;script)\\s*>", "&gt;").replaceAll("(?<=\\&lt\\;script).*\\s*>", "&gt;");
    }
    
    public static long time() {
        return System.currentTimeMillis() / 1000L;
    }
    
    public static void rmdir(final File directory) {
        final File[] files = directory.listFiles();
        if (files != null) {
            for (final File f : files) {
                if (f.isDirectory()) {
                    rmdir(f);
                }
                else {
                    f.delete();
                }
            }
        }
        directory.delete();
    }
    
    public static byte[] trim(final byte[] bytes) {
        int i;
        for (i = bytes.length - 1; i >= 0 && bytes[i] == 0; --i) {}
        return Arrays.copyOf(bytes, i + 1);
    }
    
    public static String atob(final String value) {
        try {
            return new String(Elk.BASE64_DECODER.decode(value.getBytes(Elk.charset)), Elk.charset);
        }
        catch (UnsupportedEncodingException ex) {
            Elk.logger.log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    public static byte[] atobByte(final String value) {
        try {
            return Elk.BASE64_DECODER.decode(value.getBytes(Elk.charset));
        }
        catch (UnsupportedEncodingException ex) {
            Elk.logger.log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    public static byte[] atobByte(final byte[] value) {
        return Elk.BASE64_DECODER.decode(value);
    }
    
    public static String btoa(final String value) {
        try {
            return new String(Elk.BASE64_ENCODER.encode(value.getBytes(Elk.charset)), Elk.charset);
        }
        catch (UnsupportedEncodingException ex) {
            Elk.logger.log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    public static byte[] btoaByte(final String value) {
        try {
            return Elk.BASE64_ENCODER.encode(value.getBytes(Elk.charset));
        }
        catch (UnsupportedEncodingException ex) {
            Elk.logger.log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    public static byte[] btoaByte(final byte[] value) {
        return Elk.BASE64_ENCODER.encode(value);
    }
    
    public static String getSha1String(final String str) {
        try {
            final MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(str.getBytes("UTF-8"));
            return new BigInteger(1, crypt.digest()).toString(16);
        }
        catch (UnsupportedEncodingException | NoSuchAlgorithmException ex3) {
            Elk.logger.log(Level.WARNING, null, ex3);
            return null;
        }
    }
    
    public static byte[] getSha1Bytes(final String input) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return MessageDigest.getInstance("SHA-1").digest(input.getBytes("UTF-8"));
    }
    
    public static String decodeUrl(String data) {
        try {
            data = data.replaceAll("%(?![0-9a-fA-F]{2})", "%25");
            data = data.replaceAll("\\+", "%2B");
            data = URLDecoder.decode(data, "utf-8");
        }
        catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return data;
    }
    
    public static String getContentType(final String location) {
        return processContentType(location);
    }
    
    public static String processContentType(final String location) {
        String tmp_type = "";
        final String[] tmp_type2 = location.split("/");
        if (tmp_type2.length > 0) {
            final String[] tmp_type3 = tmp_type2[tmp_type2.length - 1].split("\\.");
            if (tmp_type3.length > 1) {
                tmp_type = tmp_type3[tmp_type3.length - 1];
            }
            else {
                tmp_type = "";
            }
        }
        else {
            tmp_type = "";
        }
        final String s = tmp_type;
        switch (s) {
            case "html": {
                return "text/html";
            }
            case "css": {
                return "text/css";
            }
            case "csv": {
                return "text/csv";
            }
            case "ics": {
                return "text/calendar";
            }
            case "txt": {
                return "text/plain";
            }
            case "ttf": {
                return "font/ttf";
            }
            case "woff": {
                return "font/woff";
            }
            case "woff2": {
                return "font/woff2";
            }
            case "aac": {
                return "audio/aac";
            }
            case "mid":
            case "midi": {
                return "audio/midi";
            }
            case "oga": {
                return "audio/og";
            }
            case "wav": {
                return "audio/x-wav";
            }
            case "weba": {
                return "audio/webm";
            }
            case "ico": {
                return "image/x-icon";
            }
            case "jpeg":
            case "jpg": {
                return "image/jpeg";
            }
            case "png": {
                return "image/png";
            }
            case "gif": {
                return "image/gif";
            }
            case "bmp": {
                return "image/bmp";
            }
            case "svg": {
                return "image/svg+xml";
            }
            case "tif":
            case "tiff": {
                return "image/tiff";
            }
            case "webp": {
                return "image/webp";
            }
            case "avi": {
                return "video/x-msvideo";
            }
            case "mp4": {
                return "video/mp4";
            }
            case "mpeg": {
                return "video/mpeg";
            }
            case "ogv": {
                return "video/ogg";
            }
            case "webm": {
                return "video/webm";
            }
            case "3gp": {
                return "video/3gpp";
            }
            case "3g2": {
                return "video/3gpp2";
            }
            case "jpgv": {
                return "video/jpg";
            }
            case "abw": {
                return "application/x-abiword";
            }
            case "arc": {
                return "application/octet-stream";
            }
            case "azw": {
                return "application/vnd.amazon.ebook";
            }
            case "bin": {
                return "application/octet-stream";
            }
            case "bz": {
                return "application/x-bzip";
            }
            case "bz2": {
                return "application/x-bzip2";
            }
            case "csh": {
                return "application/x-csh";
            }
            case "doc": {
                return "application/msword";
            }
            case "epub": {
                return "application/epub+zip";
            }
            case "jar": {
                return "application/java-archive";
            }
            case "js": {
                return "application/javascript";
            }
            case "json": {
                return "application/json";
            }
            case "mpkg": {
                return "application/vnd.apple.installer+xml";
            }
            case "odp": {
                return "application/vnd.oasis.opendocument.presentation";
            }
            case "ods": {
                return "application/vnd.oasis.opendocument.spreadsheet";
            }
            case "odt": {
                return "application/vnd.oasis.opendocument.text";
            }
            case "ogx": {
                return "application/ogg";
            }
            case "pdf": {
                return "application/pdf";
            }
            case "ppt": {
                return "application/vnd.ms-powerpoint";
            }
            case "rar": {
                return "application/x-rar-compressed";
            }
            case "rtf": {
                return "application/rtf";
            }
            case "sh": {
                return "application/x-sh";
            }
            case "swf": {
                return "application/x-shockwave-flash";
            }
            case "tar": {
                return "application/x-tar";
            }
            case "vsd": {
                return "application/vnd.visio";
            }
            case "xhtml": {
                return "application/xhtml+xml";
            }
            case "xls": {
                return "application/vnd.ms-excel";
            }
            case "xml": {
                return "application/xml";
            }
            case "xul": {
                return "application/vnd.mozilla.xul+xml";
            }
            case "zip": {
                return "application/zip";
            }
            case "7z": {
                return "application/x-7z-compressed";
            }
            case "apk": {
                return "application/vnd.android.package-archive";
            }
            default: {
                return "";
            }
        }
    }
    
    public static byte[] subBytes(final byte[] source, final int srcBegin) {
        return subBytes(source, srcBegin, source.length);
    }
    
    public static byte[] subBytes(final byte[] source, final int srcBegin, final int srcEnd) {
        final byte[] destination = new byte[srcEnd - srcBegin];
        getBytes(source, srcBegin, srcEnd, destination, 0);
        return destination;
    }
    
    public static void getBytes(final byte[] source, final int srcBegin, final int srcEnd, final byte[] destination, final int dstBegin) {
        System.arraycopy(source, srcBegin, destination, dstBegin, srcEnd - srcBegin);
    }
    
    public static boolean byteArrayIsEmpty(final byte[] array) {
        int sum = 0;
        for (final byte b : array) {
            sum |= b;
        }
        return sum == 0;
    }
    
    public static String getSha512String(final String value, final String salt) {
        String result = null;
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-512");
            md.update(salt.getBytes(Elk.charset));
            final byte[] bytes = md.digest(result.getBytes(Elk.charset));
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; ++i) {
                sb.append(Integer.toString((bytes[i] & 0xFF) + 256, 16).substring(1));
            }
            result = sb.toString();
        }
        catch (NoSuchAlgorithmException ex2) {}
        catch (UnsupportedEncodingException ex) {
            Elk.logger.log(Level.WARNING, null, ex);
        }
        return result;
    }
    
    public static byte[] getSha512Bytes(final String value, final String salt) {
        try {
            return getSha512String(value, salt).getBytes(Elk.charset);
        }
        catch (UnsupportedEncodingException ex) {
            Elk.logger.log(Level.WARNING, null, ex);
            return null;
        }
    }
    
    public static String getBCryptString(final String value) {
        try {
            return BCrypt.generateStorngPasswordHash(value);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException ex3) {
            Elk.logger.log(Level.WARNING, null, ex3);
            return null;
        }
    }
    
    public static boolean validateBCryptString(final String originalString, final String cryptoString) {
        try {
            return BCrypt.validatePassword(originalString, cryptoString);
        }
        catch (NoSuchAlgorithmException | InvalidKeySpecException ex3) {
            Elk.logger.log(Level.WARNING, null, ex3);
            return false;
        }
    }
    
    public static boolean matchRegex(final String subject, final String regex) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(subject);
        return matcher.find();
    }
    
    public static String extractRegexGroup(final String subject, final String regex, int n) {
        final Pattern pattern = Pattern.compile(regex);
        final Matcher matcher = pattern.matcher(subject);
        if (matcher.find()) {
            if (n < 0) {
                n += matcher.groupCount();
            }
            return matcher.group(n);
        }
        return null;
    }
    
    public static String extractRegex(final String subject, final String regex) {
        return extractRegexGroup(subject, regex, 0);
    }
    
    static {
        Elk.listen = true;
        Elk.groupsAllowed = false;
        Elk.smtpAllowed = false;
        Elk.port = 80;
        Elk.timeout = 3000;
        Elk.webRoot = "www/";
        Elk.charset = "UTF-8";
        Elk.bindAddress = "::";
        Elk.httpControllerPackageName = "com.razshare.elkserver.Controller.Http";
        Elk.wsControllerPackageName = "com.razshare.elkserver.Controller.WebSocket";
        Elk.httpControllerNotFound = "ControllerNotFound";
        Elk.wsControllerNotFound = "ControllerNotFound";
        logger = Logger.getLogger(Elk.class.getName());
        WS_EVENTS = new HashMap<String, ArrayList<WebSocketEvent>>();
        Elk.wsAcceptKey = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        calendar = Calendar.getInstance();
        Elk.indexFile = "/index.html";
        date = new Date();
        JSON_PARSER = new Gson();
        JSONPARSER = new JsonParser();
        Elk.running = false;
        BASE64_ENCODER = Base64.getEncoder();
        BASE64_DECODER = Base64.getDecoder();
        Elk.httpcnf = new ControllerNotFound();
        Elk.webSocketNotFound = new com.razshare.elkserver.Controller.WebSocket.ControllerNotFound();
        Elk.get = new Get();
        Elk.set = new Set();
        Elk.isset = new Isset();
        MULTIPART_CHARS = "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
    }
    
    static class BCrypt
    {
        public static String generateStorngPasswordHash(final String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final int iterations = 1000;
            final char[] chars = password.toCharArray();
            final byte[] salt = getSalt();
            final PBEKeySpec spec = new PBEKeySpec(chars, salt, iterations, 512);
            final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final byte[] hash = skf.generateSecret(spec).getEncoded();
            return iterations + ":" + toHex(salt) + ":" + toHex(hash);
        }
        
        public static byte[] getSalt() throws NoSuchAlgorithmException {
            final SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            final byte[] salt = new byte[16];
            sr.nextBytes(salt);
            return salt;
        }
        
        public static String toHex(final byte[] array) throws NoSuchAlgorithmException {
            final BigInteger bi = new BigInteger(1, array);
            final String hex = bi.toString(16);
            final int paddingLength = array.length * 2 - hex.length();
            if (paddingLength > 0) {
                return String.format("%0" + paddingLength + "d", 0) + hex;
            }
            return hex;
        }
        
        public static boolean validatePassword(final String originalPassword, final String storedPassword) throws NoSuchAlgorithmException, InvalidKeySpecException {
            final String[] parts = storedPassword.split(":");
            final int iterations = Integer.parseInt(parts[0]);
            final byte[] salt = fromHex(parts[1]);
            final byte[] hash = fromHex(parts[2]);
            final PBEKeySpec spec = new PBEKeySpec(originalPassword.toCharArray(), salt, iterations, hash.length * 8);
            final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            final byte[] testHash = skf.generateSecret(spec).getEncoded();
            int diff = hash.length ^ testHash.length;
            for (int i = 0; i < hash.length && i < testHash.length; ++i) {
                diff |= (hash[i] ^ testHash[i]);
            }
            return diff == 0;
        }
        
        public static byte[] fromHex(final String hex) throws NoSuchAlgorithmException {
            final byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < bytes.length; ++i) {
                bytes[i] = (byte)Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
            }
            return bytes;
        }
    }
}
