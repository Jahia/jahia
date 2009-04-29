/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util.category;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * User: ktlili
 * Date: 10 oct. 2008
 * Time: 17:21:30
 */
public class CategoryJavaScriptObject extends JavaScriptObject {
    protected CategoryJavaScriptObject() {
    }

    // Typically, methods on overlay types are JSNI
    public final native String getKey() /*-{ return this.key; }-*/;

    public final native String getName()  /*-{ return this.title;  }-*/;

    public final native String getId()  /*-{ return this.id;  }-*/;
    
    public final native String getPath()  /*-{ return this.path;  }-*/;
}
