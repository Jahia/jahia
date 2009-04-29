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
package org.jahia.ajax.gwt.client.util;

import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 24 sept. 2007
 * Time: 15:04:28
 * To change this template use File | Settings | File Templates.
 */
public class DOMUtil {

    public static void setVisible(String id, boolean visible) {
        Element wrapperEle = DOM.getElementById(id);
        setVisibility(wrapperEle, visible);
    }

    private static void setVisibility(Element wrapperEle, boolean visible) {
        String value = "display:none";
        if (visible) {
            value = "display:block";
        }
        DOM.setElementAttribute(wrapperEle, "style", value);
    }

    public static void setId(Widget w, String idValue) {
        if (w != null) {
            setId(w.getElement(), idValue);
        }
    }

    public static void setStyleAttribute(Widget w, String styleAttributeName, String value) {
        if (w != null) {
            DOM.setStyleAttribute(w.getElement(), styleAttributeName, value);
        }
    }

    public static void setId(Element ele, String idValue) {
        if (ele != null) {
            DOM.setElementAttribute(ele, "id", idValue);
        }
    }

    public static String getId(Element ele) {
        if (ele != null) {
            return DOM.getElementAttribute(ele, "id");
        }
        return null;
    }

    public static String getRootAttr(Panel panel, String name) {
        return DOM.getElementAttribute(panel.getElement(), name);
    }
}