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

//
//  HTMLToolBox
//  EV      15.12.2000
//  NK      14.05.2001 	Prefix each popup window's name with the session id
//			to resolve session conflict beetween different sites
//			opened in different explorer
//  NK      22.05.2001 	Container and field update popup's names are composed with their id too !!!
//			This is needed when to close and reopen the update popup with data. ( resolv refresh problem with JahiaOpenWindow() javascript function )
//

package org.jahia.gui;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.ajax.gwt.client.core.JahiaType;
import org.jahia.data.applications.ApplicationBean;
import org.jahia.data.beans.portlets.PortletModeBean;
import org.jahia.data.beans.portlets.PortletWindowBean;
import org.jahia.data.beans.portlets.WindowStateBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRPortletNode;
import org.jahia.bin.Jahia;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Modified and cleaned by Xavier Lawrence
 * MC modified and cleaned Xavier Lawrence
 *
 * @version $Id$
 */
public class HTMLToolBox {

    public static final String IMAGE_DIRECTORY = "/engines/images/actions";

    private static final transient Logger logger = Logger.getLogger(HTMLToolBox.class);

    private static final int JS_WINDOW_WIDTH = 1020;
    private static final int JS_WINDOW_HEIGHT = 730;

    private static String previousSessionID = "";
    private static String previousCleanSessionID = "";

    private final ProcessingContext processingContext;
    private final GuiBean gui;

    /**
     * ID generator for AJAX domIds
     */
    static Random generator = new Random();
    public static final String ID_SEPARATOR = "_--_";
    public static final Map lockIconStore = new ConcurrentHashMap();

    /**
     * constructor
     * EV    15.12.2000
     */
    public HTMLToolBox(final GuiBean gui, final ProcessingContext processingContext) {
        this.gui = gui;
        this.processingContext = processingContext;
    } // end constructor

    /**
     *
     */
    public String getResource(final String resourceBundle,
                              final String resourceName) {
        ResourceBundle res;
        String resValue = null;

        final Locale locale = processingContext.getLocale();
        try {
            res = ResourceBundle.getBundle(resourceBundle, locale);
            resValue = res.getString(resourceName);
        } catch (MissingResourceException mre) {
            logger.warn("Error accessing resource " + resourceName +
                    " in bundle " + resourceBundle + " for locale " +
                    locale + ":" + mre.getMessage());
        }
        return resValue;
    }


    /**
     *
     */
    public void drawWindowStateList(final PortletWindowBean portletWindowBean,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String listCSSClass,
                                    final String currentCSSClass,
                                    final JspWriter out)
            throws IOException {

        out.print("<ul class=\"");
        out.print(listCSSClass);
        out.print("\">\n");
        final Iterator windowStateBeansIter = portletWindowBean.getWindowStateBeans().
                iterator();
        while (windowStateBeansIter.hasNext()) {
            final WindowStateBean curWindowStateBean = (WindowStateBean)
                    windowStateBeansIter.next();
            if (curWindowStateBean.getName().equals(portletWindowBean.
                    getCurrentWindowStateBean().getName())) {
                out.print("<li class=\"");
                out.print(currentCSSClass);
                out.print("\">\n");
            } else {
                out.print("<li>");
            }
            final StringBuffer buff = new StringBuffer();
            buff.append("<a class=\"").append(curWindowStateBean.getName()).
                    append("\" title=\"").append(curWindowStateBean.getName()).
                    append("\"").append("\" href=\"").append(curWindowStateBean.getURL()).
                    append("\">").append("<span>").append(
                    getResource(resourceBundle,
                            "org.jahia.taglibs.html.portlets.windowstates." +
                                    curWindowStateBean.getName() + ".label" +
                                    namePostFix)).append("</span></a>");
            out.print(buff.toString());
            out.println("</li>");
        }
        out.println("</ul>");
    }


}
