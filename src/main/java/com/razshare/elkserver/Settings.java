// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver;

import com.google.gson.JsonElement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import com.google.gson.JsonObject;

public class Settings
{
    private static JsonObject info;
    
    public static void parse(final String settings) throws IOException {
        Settings.info = Elk.JSONPARSER.parse(new String(Files.readAllBytes(Paths.get(settings, new String[0])))).getAsJsonObject();
    }
    
    public static JsonElement get(final String membername) {
        return Settings.info.get(membername);
    }
    
    public static String getString(final String membername) {
        return get(membername).getAsString();
    }
    
    public static int getInt(final String membername) {
        return get(membername).getAsInt();
    }
    
    public static boolean isset(final String key) {
        return Settings.info.has(key);
    }
    
    static {
        Settings.info = null;
    }
}
