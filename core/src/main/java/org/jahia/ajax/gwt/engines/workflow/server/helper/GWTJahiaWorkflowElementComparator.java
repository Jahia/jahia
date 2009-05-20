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
package org.jahia.ajax.gwt.engines.workflow.server.helper;

import org.jahia.ajax.gwt.client.data.workflow.GWTJahiaWorkflowElement;

import java.util.Comparator;

/**
 * User: jahia
 * Date: 31 janv. 2008
 * Time: 14:25:11
 */
public class GWTJahiaWorkflowElementComparator<T extends GWTJahiaWorkflowElement> implements Comparator<T> {
    public final static int PATH = 0;
    public final static int LANG = 1;
    public final static int TITLE = 2;
    public final static int STATE = 3;
    private String field;
    private boolean asc;

    public GWTJahiaWorkflowElementComparator(String field, boolean asc) {
        this.field = field;
        this.asc = asc;
    }

    public int compare(T o1, T o2) {
        GWTJahiaWorkflowElement el1 = o1 ;
        GWTJahiaWorkflowElement el2 = o2 ;
        if (!asc) {
            el1 = o2;
            el2 = o1;
        }
        if (field.equals("path")) {
            return el1.getPath().compareTo(el2.getPath()) ;
        } else if (field.equals("title")) {
            return el1.getTitle().compareTo(el2.getTitle()) ;
        } else {
            return el1.toString().compareTo(el2.toString()) ;
        }
    }
}