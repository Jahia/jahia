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

