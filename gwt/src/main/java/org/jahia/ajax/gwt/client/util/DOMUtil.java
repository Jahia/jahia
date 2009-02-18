/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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