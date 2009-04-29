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
package org.jahia.ajax.gwt.client.util.templates;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jahia.ajax.gwt.client.util.DOMUtil;
import org.jahia.ajax.gwt.client.widget.template.WidgetElement;
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
    public static List<WidgetElement> getWidgetElement(String wrapperId, String jahiaType) {
        List<WidgetElement> wEles = new ArrayList<WidgetElement>();

        // get wrapper element <div id="..."> </div>
        Element wrapperEle = DOM.getElementById(wrapperId);
        // all sub. elements are WidgeElement
        int index = 1;
        if (wrapperEle != null) {
            // set title
            List<Element> list = getAllJahiaTypedElementsRec(wrapperEle);
            for (Element currentChild : list) {
                try {
                    String jahiaTypeAttr = DOM.getElementAttribute(currentChild, JahiaType.JAHIA_TYPE);
                    if (jahiaTypeAttr != null && jahiaTypeAttr.equalsIgnoreCase(jahiaType)) {
                        // convert to ui element
                        WidgetElement we = new WidgetElement(currentChild);

                        // get title
                        String title = DOM.getElementAttribute(currentChild, "title");
                        if (title == null || title.equalsIgnoreCase("")) {
                            title = "Component [" + index + "]";
                        }

                        //set id
                        String id = DOM.getElementAttribute(currentChild, "id");
                        if (id == null || id.equalsIgnoreCase("")) {
                            id = "id_" + jahiaType + "_" + index;
                        }
                        we.setId(id);
                        we.setHeader(title);
                        wEles.add(we);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // get next sibling
                index++;

            }
        }
        return wEles;
    }

    public static void makeBodyChildrenInvisible() {
        Element bodyElement = RootPanel.getBodyElement();
        WidgetElement bodyElementWidget = new WidgetElement(bodyElement);
        bodyElementWidget.addStyleName("invisible");
        bodyElementWidget.setVisible(true);
    }

    public static void makeBodyChildrenVisible() {
        Element bodyElement = RootPanel.getBodyElement();
        WidgetElement bodyElementWidget = new WidgetElement(bodyElement);
        bodyElementWidget.removeStyleName("invisible");
        bodyElementWidget.setVisible(true);
    }

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
     * Get a list of elements ids matching the given jahia type.
     *
     * @param parent    the element to search into
     * @param jahiaType the jahia type to look for
     * @return a list of string (ids) for each element
     */
    public static List<String> getElementsIdsByJahiaType(Element parent, String jahiaType) {
        List<String> list = new ArrayList<String>();
        int nb = DOM.getChildCount(parent);
        String type = DOM.getElementAttribute(parent, JahiaType.JAHIA_TYPE);
        if (type != null && type.equals(jahiaType)) {
            String id = DOM.getElementAttribute(parent, "id");
            if (id != null) {
                list.add(id);
            } else {
                Log.error("found element with jahiaType[" + jahiaType + "] without id ---> Ignore element");
            }
        }
        for (int i = 0; i < nb; i++) {
            list.addAll(getElementsIdsByJahiaType(DOM.getChild(parent, i), jahiaType));
        }

        return list;
    }

    /**
     * Recursive method to retrieve all the jahia typed elements.
     *
     * @param parent the element to search into
     * @return a list of jahia typed elements
     */
    private static List<Element> getAllJahiaTypedElementsRec(Element parent) {
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

    /**
     * Replace dynamically a gwt script
     *
     * @param modulePath
     */
    public static void moduleInjection(String modulePath) {
        Element script = DOM.getElementById("jahia-gwt");
        if (script != null) {
            Log.debug("script element found");
            DOM.setElementAttribute(script, "src", modulePath);
        }
    }


    public static native int getClientHeight() /*-{
        return $doc.documentElement.clientHeight;
    }-*/;

    public static native int getClientWidth() /*-{
        return $doc.documentElement.clientWidth;
    }-*/;


}