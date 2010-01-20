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

package org.jahia.taglibs.query;

import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.Source;
import javax.servlet.jsp.JspTagException;

import org.jahia.taglibs.AbstractJahiaTag;

/**
 * Superclass for tags, which are nested inside {@link QueryDefinitionTag}.
 * 
 * @author Sergiy Shyrkov
 */
class QueryDefinitionDependentTag extends AbstractJahiaTag {

    private static final long serialVersionUID = 1651349341403605347L;
    private String selectorName;

    /**
     * Returns an instance of the parent {@link QueryDefinitionTag} or throws an
     * exception if it is not found.
     * 
     * @return an instance of the parent {@link QueryDefinitionTag} or throws an
     *         exception if it is not found
     * @throws JspTagException in case the parent {@link QueryDefinitionTag}
     *             cannot be found.
     */
    protected final QueryDefinitionTag getQueryDefinitionTag() throws JspTagException {
        QueryDefinitionTag tag = (QueryDefinitionTag) findAncestorWithClass(this, QueryDefinitionTag.class);
        if (tag == null) {
            throw new JspTagException("The tag " + this.getClass().getName() + " must be nested inside "
                    + QueryDefinitionTag.class.getName() + " tag.");
        }

        return tag;
    }

    public String getSelectorName() {
        if (selectorName == null) {
            Source source;
            try {
                source = getQueryDefinitionTag().getSource();
                if (source != null) {
                    if (source instanceof Selector) {
                        selectorName = ((Selector)source).getSelectorName();
                    } else {
                        throw new IllegalAccessError("Need to specify the selector name because the query contains more than one selector.");
                    }
                }
            } catch (JspTagException e) {
                throw new IllegalArgumentException("Unable to detect the selectorName value, as no parent " + QueryDefinitionTag.class.getName() + " was found");
            }
        }
        
        return selectorName;
    }

    public void setSelectorName(String selectorName) {
        this.selectorName = selectorName;
    }
}
