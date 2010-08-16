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

package org.jahia.content;

import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.version.ContentObjectEntryState;

import java.io.Serializable;

/**
 * Abstract implementation for Content Definition
 *
 * @author Khue Nguyen
 */
public abstract class ContentDefinition extends JahiaObject implements Serializable {

    private static final long serialVersionUID = 6086558470748000648L;
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ContentDefinition.class);

    protected ContentDefinition(ObjectKey objectKey) {
        super(objectKey);
    }

    /**
     * No arg constructor required for serialization support.
     */
    protected ContentDefinition() {
    }

    /**
     * This method is reserved for class that derive from this one so that
     * they can update their object key notably when they get an ID assigned
     * from the database when the object is first created.
     * @param objectKey the new object key to set.
     */
    protected void setObjectKey(ObjectKey objectKey) {
        this.objectKey = objectKey;
    }

    /**
     * Return the human readable title of this Content Definition for Content Object using this Definition
     *
     * @param contentObject, the contextual Content Object
     *
     * @param contentObject
     * @param entryState
     * @return
     */
    public abstract String getTitle(ContentObject contentObject,
                                    ContentObjectEntryState entryState)
        throws ClassNotFoundException;

    /**
     * @return a String containing the name of the definition, which must be
     * unique in a site.
     */
    public abstract String getName();

    public ExtendedNodeType getNodeType() {
        return null;
    }

    public String getPrimaryType() {
        return null;
    }
}
