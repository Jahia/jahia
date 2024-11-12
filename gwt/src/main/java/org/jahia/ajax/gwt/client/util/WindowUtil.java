/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.util;

import com.extjs.gxt.ui.client.util.Point;
import com.google.gwt.user.client.Element;

/**
 * User: rfelden
 * Date: 1 oct. 2008 - 11:25:07
 */
public class WindowUtil {

    public static native void close()/*-{
        $wnd.close();
    }-*/;

    /**
     * This method has been implemented to fix the bug related to QA-9793
     * It is a copy of the jQuery method to get the absolute position of an element
     * @param elem  Element
     * @return      the absolute top position of the element
     */
    public static native int getAbsoluteTop(Element elem) /*-{
        if (!elem.getClientRects().length) {
            return 0;
        }

        rect = elem.getBoundingClientRect();
        win = elem.ownerDocument.defaultView;

        return rect.top + win.pageYOffset;
    }-*/;

    /**
     * This method has been implemented to fix the bug related to QA-9793
     * It is a copy of the jQuery method to get the absolute position of an element
     * @param elem  Element
     * @return      the absolute left position of the element
     */
    public static native int getAbsoluteLeft(Element elem) /*-{
        if (!elem.getClientRects().length) {
            return 0;
        }

        rect = elem.getBoundingClientRect();
        win = elem.ownerDocument.defaultView;

        return rect.left + win.pageXOffset;
    }-*/;

    /**
     * This method has been implemented to fix the bug related to QA-9793 / QA-10230
     * It uses the same functions as jQuery method to get the scroll position of the window
     * @param elem  Element
     * @return      the scroll top position of the window that contains the element
     */
    public static native int getScrollTop(Element elem) /*-{
        if (elem) {
            win = elem.ownerDocument.defaultView;
            return win.pageYOffset;
        }
        return 0;
    }-*/;

    /**
     * This method has been implemented to fix the bug related to QA-9793 / QA-10230
     * It uses the same functions as jQuery method to get the scroll position of the window
     * @param elem  Element
     * @return      the scroll top position of the window that contains the element
     */
    public static native int getScrollLeft(Element elem) /*-{
        if (elem) {
            win = elem.ownerDocument.defaultView;
            return win.pageXOffset;
        }
        return 0;
    }-*/;

    /**
     * Uses methods of this utility class to calculate the Point of the specified element.
     *
     * @param elem the element to calculate Point data for
     * @return the Point of the specified element
     */
    public static Point getXY(Element elem) {
        return new Point(getAbsoluteLeft(elem), getAbsoluteTop(elem));
    }

}
