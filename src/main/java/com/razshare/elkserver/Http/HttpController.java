// 
// Decompiled by Procyon v0.5.36
// 

package com.razshare.elkserver.Http;

import java.util.ArrayList;
import com.razshare.elkserver.Elk;

public abstract class HttpController extends Elk
{
    public abstract void main(final HttpEvent p0, final ArrayList<String> p1, final String p2);
    
    public abstract void onClose();
}
