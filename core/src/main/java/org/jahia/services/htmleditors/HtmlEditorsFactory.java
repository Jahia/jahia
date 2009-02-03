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

//
package org.jahia.services.htmleditors;

import java.util.List;

import org.jahia.exceptions.JahiaException;

/**
 * Html Editors Factory Interface
 *
 * @author Khue Nguyen
 */
public interface HtmlEditorsFactory
{

	/**
	 * Returns a List of all Html Editors registered in the System
	 *
	 * @return all Html Editors registered in the system
     * @throws JahiaException
	 */
    public abstract List getEditors() throws JahiaException;

    /**
     * Returns a List of all Html Editor CSS registered in the System
     *
     * @return all Html Editor CSS registered in the system
     * @throws JahiaException
     */
    public abstract List getCSSs() throws JahiaException;

    /**
     * Returns an Editor looking at it id
     *
     * @param id the Editor identifier
     * @return an Editor looking at it id
     * @throws JahiaException
     */
    public abstract HtmlEditor getEditor(String id) throws JahiaException;

    /**
     * Returns an CSS looking at it id
     *
     * @param id the CSS identifier
     * @return an CSS looking at it id
     * @throws JahiaException
     */
    public abstract HtmlEditorCSS getCSS(String id) throws JahiaException;
}

