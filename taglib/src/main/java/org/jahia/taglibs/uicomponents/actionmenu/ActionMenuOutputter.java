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
package org.jahia.taglibs.uicomponents.actionmenu;

import org.apache.log4j.Logger;
import org.jahia.data.beans.ContentBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

import javax.servlet.jsp.PageContext;

/**
 * Helper class for rendering action menu.
 *
 * @author rfelden
 * @version 11 f�vr. 2008 - 17:50:53
 */
public class ActionMenuOutputter {

    private static final transient Logger logger = Logger.getLogger(ActionMenuOutputter.class);
    public static final String DEFAULT_CSS = "actionMenuWrap";
    public static final String CONTAINER_DEFAULT_CSS = "containerWrap";
    public static final String CONTAINERLIST_DEFAULT_CSS = "containerListWrap";

    // parameters to check if action menu has to be displayed
    private ProcessingContext processingContext;
    private ContentBean contentObject;

    // default attributes
    private PageContext pageContext;
    private String contentObjectName;
    private String objectKey;
    private int actionType = 0;

    // custom attributes
    private String bundleName = null;
    private String namePostFix = null;
    private String labelKey = null;
    private String iconStyle = null ;
    private boolean toolbarView = true ;

    /**
     * Construct a action menu without customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey) {
        this.processingContext = processingContext;
        this.contentObject = contentObject;
        this.pageContext = context;
        this.contentObjectName = objectName;
        this.objectKey = objectKey;
    }

    /**
     * Construct a action menu without customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     * @param actionType        add, edit container, edit container list
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey, int actionType) {
        this(processingContext, context, contentObject, objectName,  objectKey) ;
        this.actionType = actionType ;
    }

    /**
     * Construct a action menu with customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     * @param bundleName        the custom resource bundle to use
     * @param namePostFix       the key to retrieve label extension
     * @param labelKey          the label to add after the action menu icon
     * @param iconStyle         the icon style
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey, String bundleName, String namePostFix, String labelKey, String iconStyle) {
        this(processingContext, context, contentObject, objectName,  objectKey) ;
        this.bundleName = bundleName;
        this.namePostFix = namePostFix;
        this.labelKey = labelKey;
        this.iconStyle = iconStyle ;
    }

    /**
     * Construct a action menu with customized labels.
     *
     * @param processingContext the processing context
     * @param context           the page ontext where it is from
     * @param contentObject     the content object (can be null)
     * @param objectName        the content object name (not really needed, just for understandable html)
     * @param objectKey         the content object key
     * @param actionType        add, edit container, edit container list
     * @param bundleName        the custom resource bundle to use
     * @param namePostFix       the key to retrieve label extension
     * @param labelKey          the label to add after the action menu icon
     * @param iconStyle         the icon style
     */
    public ActionMenuOutputter(ProcessingContext processingContext, PageContext context, ContentBean contentObject, String objectName, String objectKey, int actionType, String bundleName, String namePostFix, String labelKey, String iconStyle) {
        this(processingContext, context, contentObject, objectName,  objectKey, actionType) ;
        this.bundleName = bundleName;
        this.namePostFix = namePostFix;
        this.labelKey = labelKey;
        this.iconStyle = iconStyle ;
    }

     public String getOutput(boolean enableToolbarView) throws ClassNotFoundException, JahiaException {
         this.toolbarView = enableToolbarView ;
         return getOutput() ;
     }

    /**
     * Generate the HTML output given the constructor parameters. The generated div will then be filled by GWT.
     *
     * @return HTML code
     * @throws ClassNotFoundException sthg bad happened
     * @throws org.jahia.exceptions.JahiaException
     *                                sthg bad happened
     */
    public String getOutput() throws ClassNotFoundException, JahiaException {
        return "";
    }

}
