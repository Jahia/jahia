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
package org.jahia.ajax.gwt.client.widget.node;

import org.jahia.ajax.gwt.client.messages.Messages;
import org.jahia.ajax.gwt.client.widget.tripanel.MyStatusBar;

/**
 * User: rfelden
 * Date: 16 janv. 2009 - 15:54:26
 */
public class FilterStatusBar extends MyStatusBar {

    public FilterStatusBar(String filters, String mimeTypes, String nodeTypes) {
        // display filters to inform user
        StringBuilder disp = new StringBuilder() ;
        if (filters != null && filters.length()>0) {
            disp.append(Messages.getResource("fm_filters")).append(" : ").append(filters) ;
        }
        if (mimeTypes != null && mimeTypes.length()>0) {
            if (disp.length() > 0) {
                disp.append(" - ") ;
            }
            disp.append(Messages.getResource("fm_mimes")).append(" : ").append(mimeTypes) ;
        }
        if (nodeTypes != null && nodeTypes.length()>0) {
            if (disp.length() > 0) {
                disp.append(" - ") ;
            }
            disp.append(Messages.getResource("fm_nodes")).append(" : ").append(nodeTypes) ;
        }
        setMessage(disp.toString());
    }

}
