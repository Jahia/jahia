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
//
//



package org.jahia.services.containers;

import org.jahia.data.fields.JahiaField;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.events.JahiaEventListener;
import org.jahia.exceptions.*;
import org.jahia.services.fields.ContentField;

/**
 * Listener for fields change events.
 * Used mainly for cache synchronization
 *
 * You must access this Singleton through JahiaListenersRegistry
 *
 * @see JahiaListenersRegistry
 * @author Khue Nguyen <a href="mailto:khue@jahia.org">khue@jahia.org</a>
 */
public class FieldsChangeEventListener extends JahiaEventListener
{

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (FieldsChangeEventListener.class);

    public static final String FIELD_ADDED = "fieldAdded";
    public static final String FIELD_UPDATED = "fieldUpdated";
    public static final String FIELD_DELETED = "fieldDeleted";

    public FieldsChangeEventListener() throws JahiaException {
    }

    //--------------------------------------------------------------------------
    /**
     * triggered when Jahia adds a field
     *
     * @param je the associated JahiaEvent
     */
    public void fieldAdded( JahiaEvent je ) {
        JahiaField theField    = (JahiaField) je.getObject();
        if ( theField != null ){
            notifyChange(theField.getID());
        }
    }

    //--------------------------------------------------------------------------
    /**
     * triggered when Jahia updates a field
     *
     * @param je the associated JahiaEvent
     */
    public void fieldUpdated( JahiaEvent je ) {
        JahiaField theField    = (JahiaField) je.getObject();
        if ( theField != null ){
            notifyChange(theField.getID());
        }
    }

    //--------------------------------------------------------------------------
    /**
     * triggered when Jahia deletes a field
     *
     * @param je the associated JahiaEvent
     */
    public void fieldDeleted( JahiaEvent je ) {
        JahiaField theField    = (JahiaField) je.getObject();
        if ( theField != null ){
            notifyChange(theField.getID());
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Update related caches :
     * - field IDs by container cache
     *
     * @param fieldId
     */
    public  void notifyChange( int fieldId  ) {
        try {
            ContentField contentField = ContentField.getField(fieldId);
            notifyChange(contentField);
        } catch ( JahiaException je ){
            logger.debug("Exception looking for field " + fieldId, je);
        }
    }

    //--------------------------------------------------------------------------
    /**
     * Update related caches :
     * - field IDs by container cache
     *
     * @param ContentField contentField
     */
    public  void notifyChange( ContentField contentField  ) {

        if ( contentField == null ){
            return;
        }
        ContentContainerTools.getInstance()
            .invalidateFieldIDsByContainerFromCache(contentField.getContainerID());
    }

}
