/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
        win = elem.ownerDocument.defaultView;
        return win.pageYOffset;
    }-*/;

    /**
     * This method has been implemented to fix the bug related to QA-9793 / QA-10230
     * It uses the same functions as jQuery method to get the scroll position of the window
     * @param elem  Element
     * @return      the scroll top position of the window that contains the element
     */
    public static native int getScrollLeft(Element elem) /*-{
        win = elem.ownerDocument.defaultView;
        return win.pageXOffset;
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
