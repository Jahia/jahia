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
package org.jahia.ajax.gwt.client.util.nodes;

import com.extjs.gxt.ui.client.store.StoreSorter;
import com.extjs.gxt.ui.client.store.Store;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;

/**
 * User: rfelden
 * Date: 12 déc. 2008 - 11:33:45
 */
public class FileStoreSorter extends StoreSorter<GWTJahiaNode> {

    public FileStoreSorter() {
        super() ;
    }

    public int compare(Store<GWTJahiaNode> store, GWTJahiaNode o1, GWTJahiaNode o2, String property) {
        if (property == null || (property.equals("name") || property.equals("path"))) {
            return o1.compareTo(o2) ;
        } else {
            return super.compare(store, o1, o2, property) ;
        }
    }
}
