/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
    //    public static final Map resourceBundleStore = new ConcurrentHashMap();
    public static final Map lockIconStore = new ConcurrentHashMap();

    /**
     * constructor
     * EV    15.12.2000
     */
    public HTMLToolBox(final GuiBean gui, final ProcessingContext processingContext) {
        this.gui = gui;
        this.processingContext = processingContext;
    } // end constructor

    public HTMLToolBox(final GuiBean gui) {
        this.gui = gui;
        this.processingContext = gui.params();
    }

    protected HTMLToolBox() {
        this.gui = null;
        this.processingContext = null;
    }

    /**
     * Utility method to clean session ID to be used in javascript code mostly
     * Basically what it does is removed all non alpha numeric characters in the
     * session ID. This uses a unicode method so it WILL allow unicode allowed
     * alpha numeric character which might NOT work with Javascript flawlessly.
     * <p/>
     * This method also uses a very simple "cache" system that allows us not
     * to have to parse the whole string if called repeteadly with the same
     * session ID.
     *
     * @param sessionID the session ID to test for non alpha numeric characters
     * @return the "cleaned" session ID.
     */
    public static String cleanSessionID(final String sessionID) {
        if (sessionID.equals(previousSessionID)) {
            return previousCleanSessionID;
        }
        final StringBuffer result = new StringBuffer();
        for (int i = 0; i < sessionID.length(); i++) {
            final char curChar = sessionID.charAt(i);
            if (Character.isLetterOrDigit(curChar)) {
                result.append(curChar);
            }
        }
        previousSessionID = sessionID;
        previousCleanSessionID = result.toString();
        return result.toString();
    }

    //---------------------------------------------------------------- drawLogin

    /**
     * returns the URL allowing to open the login popup window
     *
     * @return
     * @throws JahiaException
     */
    public String drawLoginLauncher() throws JahiaException {
        // BEGIN [for CAS authentication]
        final StringBuffer buff = new StringBuffer();
        if (Jahia.usesSso()) {
            String loginUrl = Jahia.getSsoValve().getRedirectUrl(processingContext);
            if (loginUrl != null) {
                return buff.append("window.location = '").append(loginUrl).append("'").toString();
            }
        }
        // END [for CAS authentication]
        final String popupLoginURL = gui.drawPopupLoginUrl();
        return (popupLoginURL.equals("")) ? "" :
                buff.append("OpenJahiaWindow('").append(popupLoginURL).
                        append("','Login',450,500)").toString();
    }

    /**
     * @param destinationPageID
     * @return
     * @throws JahiaException
     */
    public String drawLoginLauncher(final int destinationPageID) throws JahiaException {
        // BEGIN [for CAS authentication]
        final StringBuffer buff = new StringBuffer();
        if (Jahia.usesSso()) {
            String loginUrl = Jahia.getSsoValve().getRedirectUrl(processingContext);
            if (loginUrl != null) {
                return buff.append("window.location = '").append(loginUrl).append("'").toString();
            }
        }
        // END [for CAS authentication]
        final String popupLoginURL = gui.drawPopupLoginUrl(destinationPageID);
        return (popupLoginURL.equals("")) ? "" :
                buff.append("OpenJahiaWindow('").append(popupLoginURL).
                        append("','Login',450,500)").toString();
    }

    //--------------------------------------------------------------- drawLogout

    /**
     * returns the URL allowing to logout
     *
     * @return
     * @throws JahiaException
     */
    public String drawLogoutLauncher() throws JahiaException {
        return gui.drawLogoutUrl();
    }

    /**
     * @param destinationPageID
     * @return
     * @throws JahiaException
     */
    public String drawLogoutLauncher(final int destinationPageID) throws JahiaException {
        return gui.drawLogoutUrl();
    }

    /**
     * drawLauncher
     * EV    15.12.2000
     */
    private String drawLauncher(final String url, final String windowName) {
        final StringBuffer html = new StringBuffer();
        // if url = "", this means that the engine didn't grant authorisation to render
        if (!url.equals("")) {
            html.append("OpenJahiaScrollableWindow('");
            html.append(url);
            html.append("','").append(windowName).append("',").append(JS_WINDOW_WIDTH).
                    append(",").append(JS_WINDOW_HEIGHT).append(")");
        }
        return html.toString();
    } // end drawLauncher

    /**
     * returns the URL of the administration page
     */
    // MJ 21.03.2001
    public String drawAdministrationLauncher() throws JahiaException {
        return gui.drawAdministrationLauncher();
    }

    public String drawMySettingsLauncher() throws JahiaException {
        return gui.drawMySettingsUrl();
    }

    public String drawMySettingsLauncher(Object params) throws JahiaException {
        return gui.drawMySettingsUrl(params);
    }

    public String drawNewUserRegistrationLauncher() throws JahiaException {
        return gui.drawNewUserRegistrationUrl();
    }


    /**
     * Constructs the javascript function call for the AJAX action menu.
     */
    protected String buildAjaxCall(final String objectType,
                                   final int objectKey,
                                   final int definitionID,
                                   final int parentID,
                                   final int pageID,
                                   final String domID,
                                   final String lang,
                                   final int contextualContainerListId) {
        final StringBuffer buff = new StringBuffer();
        buff.append("javascript:getActionMenu('");
        buff.append(((ParamBean) processingContext).getRequest().getContextPath()).append("', '");
        buff.append(objectType).append("', ");
        buff.append(objectKey).append(", ");
        buff.append(definitionID).append(", ");
        buff.append(parentID).append(", ");
        buff.append(pageID).append(", '");
        buff.append(domID).append("', '");
        buff.append(lang).append("', ");
        ;
        buff.append(contextualContainerListId).append(")");
        return buff.toString();
    }

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
    public String getURLImageContext() {
        return processingContext.getContextPath() + IMAGE_DIRECTORY;
    }

    protected String buildUniqueContentID(final String typeName,
                                          final int id,
                                          final int definitionID,
                                          final int parentID,
                                          final int pageID,
                                          final String lockIcon,
                                          final boolean useFieldSet,
                                          final String resourceBundle,
                                          final String namePostFix) {
        //just use the String's hashcode instead of real value
        Integer lockIconKey = (lockIcon != null) ? (new Integer(lockIcon.hashCode())) : null;
        if (lockIcon != null && !lockIconStore.containsKey(lockIconKey)) {
            lockIconStore.put(lockIconKey, lockIcon);
        }

//        Integer resourceBundleKey = (resourceBundle != null) ? (new Integer(resourceBundle.hashCode())) : null;
//        if (resourceBundle != null && !resourceBundleStore.containsKey(resourceBundleKey)) {
//            resourceBundleStore.put(resourceBundleKey, resourceBundle);
//        }

        final StringBuffer result = new StringBuffer();
        try {
            result.append(typeName)
                    .append(ID_SEPARATOR)
                    .append(id)
                    .append(ID_SEPARATOR)
                    .append(definitionID)
                    .append(ID_SEPARATOR)
                    .append(parentID)
                    .append(ID_SEPARATOR)
                    .append(pageID)
                    .append(ID_SEPARATOR)
                    .append(lockIconKey)
                    .append(ID_SEPARATOR)
                    .append(useFieldSet)
                    .append(ID_SEPARATOR)
                    .append(new String(new Base64().encode(resourceBundle.getBytes("UTF-8"))))
                    .append(ID_SEPARATOR)
                    .append(namePostFix)
                    .append(ID_SEPARATOR)
                    .append(generator.nextInt());
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }

        if (logger.isDebugEnabled()) logger.debug("menuId is : " + result.toString());

        return result.toString();
    }

    /**
     *
     */
    /*public String buildUniqueContentID(final ContentBean bean, ProcessingContext processingContext) {
        final ContentBean parent = bean.getParent();
        final int parentID = (parent == null) ? 0 : parent.getID();
        return buildUniqueContentID(bean.getBeanType(), bean.getID(),
                bean.getDefinitionID(), parentID, processingContext.getPageID());
    }*/

    /**
     *
     */
    public void drawPortletModeList(final PortletWindowBean portletWindowBean,
                                    final String namePostFix,
                                    final String resourceBundle,
                                    final String listCSSClass,
                                    final String currentCSSClass,
                                    final JspWriter out)
            throws IOException {
        final List portletModeBeansIterList = portletWindowBean.getPortletModeBeans();
        // draw mode links only if there is more than 1 mode
        if (portletModeBeansIterList.size() < 2) {
            return;
        }

        out.print("<ul class=\"");
        out.print(listCSSClass);
        out.print("\">\n");
        final Iterator portletModeBeansIter = portletWindowBean.getPortletModeBeans().iterator();
        while (portletModeBeansIter.hasNext()) {
            final PortletModeBean curPortletModeBean = (PortletModeBean)
                    portletModeBeansIter.next();
            if (curPortletModeBean.getName().equals(portletWindowBean.
                    getCurrentPortletModeBean().getName())) {
                out.print("<li class=\"");
                out.print(currentCSSClass);
                out.print("\">\n");
            } else {
                out.print("<li>");
            }
            final StringBuffer buff = new StringBuffer();
            buff.append("<a class=\"").append(curPortletModeBean.getName()).
                    append("\" title=\"").append(curPortletModeBean.getName()).
                    append("\" href=\"").append(curPortletModeBean.getURL()).
                    append("\">").append("<span>").append(getResource(resourceBundle,
                    "org.jahia.taglibs.html.portlets.portletmodes." +
                            curPortletModeBean.getName() + ".label" +
                            namePostFix)).append("</span></a>");
            out.print(buff.toString());
            out.println("</li>");
        }
        out.println("</ul>");
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


    /**
     * draw mashup node
     *
     * @param jcrPortletNode
     * @param ajaxRendering
     * @return
     */
    public void drawMashup(JCRPortletNode jcrPortletNode, boolean ajaxRendering, int windowId, final JspWriter out) throws JahiaException, IOException {
        if (!(processingContext instanceof ParamBean)) {
            logger.error("ProcessingContext is not instanceof ParamBean. Mashup can't be rendered");
            return;
        }
        ParamBean jParam = (ParamBean) processingContext;

        boolean computedAjaxRendering = ajaxRendering && processingContext.settings().isPortletAJAXRenderingActivated();

        String appID = null;
        try {
            appID = jcrPortletNode.getUUID();
        } catch (RepositoryException e) {
            throw new JahiaException("Error rendering mashup", "Error rendering mashup",
                    JahiaException.APPLICATION_ERROR, JahiaException.ERROR_SEVERITY, e);
        }

        logger.debug("Dispatching to portlet for appID=" + appID + "...");

        String portletOutput = "";
        if (computedAjaxRendering) {
            portletOutput = "<div id=\"" + windowId + "\" windowID=\"" + windowId +
                    "\" entryPointInstanceID=\"" + appID + "\" " +
                    JahiaType.JAHIA_TYPE + "=\"" + JahiaType.PORTLET_RENDER +
                    "\" pathInfo=\"" + processingContext.getPathInfo() +
                    "\" queryString=\"" + processingContext.getQueryString() +
                    "\"></div>";
        } else {
            portletOutput = ServicesRegistry.getInstance().getApplicationsDispatchService().getAppOutput(windowId, appID, jParam);
        }

        // remove <html> tags that can break the page
        if (portletOutput != null) {
            try {
                portletOutput = (new RE("</?html>", RE.MATCH_CASEINDEPENDENT)).subst(portletOutput, "");
            } catch (RESyntaxException e) {
                logger.debug(".getValue, exception : " + e.toString());
            }
        } else {
            portletOutput = "";
        }
        out.print(portletOutput);
    }
}
