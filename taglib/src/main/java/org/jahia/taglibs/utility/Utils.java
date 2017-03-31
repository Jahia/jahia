/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.utility;

import java.util.Iterator;

import javax.servlet.jsp.PageContext;

import org.jahia.services.render.RenderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public final static int TO_MIN = Integer.MIN_VALUE;
    public final static int TO_MAX = Integer.MAX_VALUE;    
    
    private final static String SEPARATOR = "_";
    
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    /**
     * Method buildUniqueName :  builds a unique name for a field, thanks to the name
     * of the container which contains it
     *
     * @param parentName : the name of the parent tag (a container)
     * @param tagName    : the name of the current tag (a container or a field)
     * @return the new name for the current tag, used by Jahia to retrieve him
     */
    public static String buildUniqueName(final String parentName, final String tagName) {

        final StringBuilder buffer = new StringBuilder();
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
    public static int enumSize(final Iterator<?> enumeration) {
        int i = 0;
        while (enumeration.hasNext()) {
            i++;
            enumeration.next();
        }
        return i;
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


    public static RenderContext getRenderContext(PageContext pageContext) {
        return (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
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
    public static String getShortClassName(final Class<?> theClass) {

        final String fullName = theClass.getName();
        final int lastDot = fullName.lastIndexOf(".");
        if (lastDot == -1) {
            logger.error("The class name contains no dot.");
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
            final StringBuilder tmp = new StringBuilder();
            tmp.append(link);
            // insert after "src='"
            tmp.insert(pos + 5, contextPath + "/");
            return tmp.toString();
        }
        // no image in link : remains the same
        return link;
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

        StringBuilder result = new StringBuilder();
        int i = str.indexOf(token);
        while (i != -1) {
            result.append(str.substring(0, i)).append(replaceValue);
            str = str.substring(i + token.length(), str.length());
            i = str.indexOf(token);
        }
        result.append(str);
        return result.toString();
    }

    /**
     * Initializes an instance of this class.
     */
    private Utils() {
        super();
    }

}
