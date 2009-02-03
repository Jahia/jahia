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

package org.jahia.taglibs.utility;

import java.util.Iterator;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.data.beans.JahiaBean;
import org.jahia.gui.GuiBean;
import org.jahia.params.ProcessingContext;
import org.jahia.utils.JahiaConsole;

/**
 * Class Utils :  provides miscellaneous methods used by the taglibs
 *
 * @author Jerome Tamiotti
 *         <p/>
 *         jsp:tag name="99999999999" body-content="empty"
 *         description="99999999999999.
 *         <p/>
 *         <p><attriInfo>
 *         </attriInfo>"
 */
public class Utils {

    private static String SEPARATOR = "_";

    /**
     * Method buildUniqueName :  builds a unique name for a field, thanks to the name
     * of the container which contains it
     *
     * @param parentName : the name of the parent tag (a container)
     * @param tagName    : the name of the current tag (a container or a field)
     * @return the new name for the current tag, used by Jahia to retrieve him
     */
    public static String buildUniqueName(final String parentName, final String tagName) {

        final StringBuffer buffer = new StringBuffer();
        buffer.append(parentName);
        buffer.append(SEPARATOR);
        buffer.append(tagName);
        return buffer.toString();
    }

    /**
     * Method enumSize : returns the size of the given enumeration
     *
     * @param enumeration
     * @return its size
     */
    public static int enumSize(final Iterator enumeration) {
        int i = 0;
        while (enumeration.hasNext()) {
            i++;
            enumeration.next();
        }
        return i;
    }

    /**
     * Returns an {@link JahiaBean} instance with current Jahia data.
     *
     * @param pageContext current page context object
     * @return an {@link JahiaBean} instance with current Jahia data
     */
    public static JahiaBean getJahiaBean(PageContext pageContext) {
        return getJahiaBean(pageContext, false);
    }

    /**
     * Returns an {@link JahiaBean} instance with current Jahia data.
     *
     * @param pageContext      current page context object
     * @param createIfNotFound will create the bean if it is not found
     * @return an {@link JahiaBean} instance with current Jahia data
     */
    public static JahiaBean getJahiaBean(PageContext pageContext, boolean createIfNotFound) {
        JahiaBean bean = (JahiaBean) pageContext.getAttribute("jahia", PageContext.REQUEST_SCOPE);
        if (bean == null) {
            bean = (JahiaBean) pageContext.getAttribute("currentJahia", PageContext.REQUEST_SCOPE);
            if(createIfNotFound) {
                ProcessingContext ctx = getProcessingContext(pageContext);
                if (ctx != null) {
                    bean = new JahiaBean(ctx);
                    pageContext.setAttribute("jahia", bean, PageContext.REQUEST_SCOPE);
                    pageContext.setAttribute("currentJahia", bean, PageContext.REQUEST_SCOPE);
                }
            }
        }
        return bean;
    }

    /**
     * Returns current {@link JahiaData} instance.
     *
     * @param pageContext current page context object
     * @return current {@link JahiaData} instance
     */
    public static JahiaData getJahiaData(PageContext pageContext) {
        return getJahiaData(pageContext.getRequest());
    }


    /**
     * Returns current {@link JahiaData} instance.
     *
     * @param processingContext current processing context object
     * @return current {@link JahiaData} instance
     */
    public static JahiaData getJahiaData(ProcessingContext processingContext) {
        return (JahiaData) processingContext
                .getAttribute("org.jahia.data.JahiaData");
    }


    /**
     * Returns current {@link JahiaData} instance.
     *
     * @param request current request object
     * @return current {@link JahiaData} instance
     */
    public static JahiaData getJahiaData(ServletRequest request) {
        return (JahiaData) request.getAttribute("org.jahia.data.JahiaData");
    }


    /**
     * Method getParentName :  returns the name of the parent, that means the complete name
     * without the last container name
     *
     * @param completeName : the complete path
     * @param tagName      : the name to cut
     * @return the name for the parent tag
     */
    public static String getParentName(final String completeName, final String tagName) {

        if (completeName.endsWith(SEPARATOR + tagName)) {
            // checks if the completeName ends with the tagName
            final int last = completeName.length() - tagName.length() - 1; // -1 for '_'
            return completeName.substring(0, last);

        } else if (completeName.equals(tagName)) {
            // or if it IS the same
            return "";
        }
        // nothing to cut
        return completeName;
    }


    /**
     * Returns current {@link ProcessingContext} instance.
     *
     * @param pageContext current page context
     * @return current {@link ProcessingContext} instance
     */
    public static ProcessingContext getProcessingContext(
            PageContext pageContext) {
        JahiaBean jBean = getJahiaBean(pageContext);
        ProcessingContext ctx = jBean != null ? jBean.getProcessingContext()
                : (ProcessingContext) pageContext
                .getAttribute("org.jahia.params.ParamBean",
                        PageContext.REQUEST_SCOPE);
        return ctx != null ? ctx : Jahia.getThreadParamBean();
    }

    /**
     * Method getShortClassName : from a fully-qualified class name,
     * returns only the name of the class
     * E.g. : full class name = "java.lang.String"
     * returns "String"
     *
     * @param theClass the class whose name is wanted
     * @return the short name
     */
    public static String getShortClassName(final Class theClass) {

        final String fullName = theClass.getName();
        final int lastDot = fullName.lastIndexOf(".");
        if (lastDot == -1) {
            JahiaConsole.println("Utils: getShortClassName ", "The class name contains no dot.");
            return fullName;
        }
        return fullName.substring(lastDot + 1);
    }


    /**
     * Method insertContextPath : insert the URL of the context in the link
     * Used for accessing images :
     * E.g. : title = "<img src='images/icon.gif'>"
     * will be changed in : "<img src='$contextPath/images/icon.gif'>"
     * where $contextPath is read in the request
     *
     * @param contextPath the String to be parsed
     * @param link        the link to change
     * @return the new link
     */
    public static String insertContextPath(final String contextPath, final String link) {

        final int pos = link.indexOf("src=");
        if (pos != -1) {
            final StringBuffer tmp = new StringBuffer();
            tmp.append(link);
            // insert after "src='"
            tmp.insert(pos + 5, contextPath + "/");
            return tmp.toString();
        }
        // no image in link : remains the same
        return link;
    }

    /**
     * Method isBrowserVersion : tests the version of the browser thanks to the guiBean method
     *
     * @param gui     the GuiBean used to test
     * @param version the version to compare to
     */
    public static boolean isBrowserVersion(final HttpServletRequest req,
                                           final GuiBean gui,
                                           final String version) {

        final String versionUp = version.toUpperCase();
        if (versionUp.equals("NS")) {
            return gui.isNS();
        }
        if (versionUp.equals("NS4")) {
            return gui.isNS4();
        }
        if (versionUp.equals("NS6")) {
            return gui.isNS6();
        }
        if (versionUp.equals("IE")) {
            return gui.isIE();
        }
        if (versionUp.equals("IE4")) {
            return gui.isIE4();
        }
        if (versionUp.equals("IE5")) {
            return gui.isIE5();
        }
        if (versionUp.equals("IE6")) {
            return gui.isIE6();
        }
        return false;
    }

    /**
     * Check if an int value is contained in an int array.
     *
     * @param array an Integer array
     * @param value an int value
     * @return true if contained, false otherwise
     */
    public static boolean isContainedInArray(int[] array, int value) {
        for (int n : array) {
            if (n == value) {
                return true;
            }
        }
        return false;
    }

    /**
     * Method replace : replaces a token in a string with another given String
     *
     * @param str          the String to be parsed
     * @param token        the token to be found and replaced
     * @param replaceValue the value to insert where token is found
     * @return the new String
     */
    public static String replace(String str, final String token,
                                 final String replaceValue) {

        String result = "";
        int i = str.indexOf(token);
        while (i != -1) {
            result += str.substring(0, i) + replaceValue;
            str = str.substring(i + token.length(), str.length());
            i = str.indexOf(token);
        }
        return result + str;
    }

    /**
     * Initializes an instance of this class.
     */
    private Utils() {
        super();
    }

}
