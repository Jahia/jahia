/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.client.util.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.util.DOMUtil;
import org.jahia.ajax.gwt.client.core.JahiaType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.RootPanel;


/**
 * Created by Jahia.
 * User: ktlili
 * Date: 24 sept. 2007
 * Time: 15:04:28
 * To change this template use File | Settings | File Templates.
 */
public class TemplatesDOMUtil extends DOMUtil {

    /**
     * Get a map containing all available jahia types in the current element, the key being the type and
     * the entry being a list of elements.
     *
     * @param parent the element to search into
     * @return a map of (String, ArrayList(RootPanel))
     */
    public static Map<String, List<RootPanel>> getAllJahiaTypedRootPanels(Element parent) {
        List<Element> list = getAllJahiaTypedElementsRec(parent);
        Map<String, List<RootPanel>> elementsByJahiaType = new HashMap<String, List<RootPanel>>();

        for (Element current : list) {
            String jahiaType = DOM.getElementAttribute(current, JahiaType.JAHIA_TYPE);
            String id = DOM.getElementAttribute(current, "id");
            if (jahiaType != null && jahiaType.length() > 0 && id != null && id.length() > 0) {
                if (!elementsByJahiaType.containsKey(jahiaType)) {
                    elementsByJahiaType.put(jahiaType, new ArrayList<RootPanel>());
                }
                elementsByJahiaType.get(jahiaType).add(RootPanel.get(id));
            }
        }
        return elementsByJahiaType;
    }

    /**
     * Recursive method to retrieve all the jahia typed elements.
     *
     * @param parent the element to search into
     * @return a list of jahia typed elements
     */
    public static List<Element> getAllJahiaTypedElementsRec(Element parent) {
        List<Element> list = new ArrayList<Element>();
        int nb = DOM.getChildCount(parent);
        String type = DOM.getElementAttribute(parent, JahiaType.JAHIA_TYPE);
        if (type != null && type.length() > 0) {
            list.add(parent);
        }
        for (int i = 0; i < nb; i++) {
            list.addAll(getAllJahiaTypedElementsRec(DOM.getChild(parent, i)));
        }
        return list;
    }


    public static native int getClientHeight() /*-{
        return $doc.documentElement.clientHeight;
    }-*/;

    public static native int getClientWidth() /*-{
        return $doc.documentElement.clientWidth;
    }-*/;


}