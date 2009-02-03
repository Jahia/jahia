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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.engines.workflow.server.helper;

import org.jahia.ajax.gwt.engines.workflow.client.model.GWTJahiaWorkflowElement;
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