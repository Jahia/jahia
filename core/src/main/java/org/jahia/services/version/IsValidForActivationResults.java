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

package org.jahia.services.version;

import org.jahia.content.ContentObjectKey;
import org.jahia.content.NodeOperationResult;
import org.jahia.engines.EngineMessage;

/**
 * <p>Title: A node result for an activation test.</p>
 * <p>Description:
 * This object is created when an error is triggered during the check for
 * activation process.
 * </p>
 * <p>Copyright: MAP (Jahia Solutions S�rl 2002)</p>
 * <p>Company: Jahia Solutions</p>
 * @author MAP
 * @version 1.0
 */
public class IsValidForActivationResults extends NodeOperationResult {
    private static final long serialVersionUID = 6153441986599456048L;

    /**
     * Constructor for the result.
     * @param objectType the objects type. Here only supported object types
     * are allowed. See the org.jahia.content.ObjectKey class and it's
     * descendents for more information.
     * @param objectID the identifier within the type, again refer to the
     * ObjectKey class for more information.
     * @param languageCode the language for which this result is given
     * @param msg An EngineMessage to use for internationalization
     * @throws ClassNotFoundException thrown if the object type is not
     * recognized by Jahia
     * @see org.jahia.content.ObjectKey
     */
    public IsValidForActivationResults(ContentObjectKey objectKey,
            String languageCode, EngineMessage msg) throws ClassNotFoundException {
        super(objectKey, languageCode, null, msg);
    }
    
    public String toString() {
        final StringBuffer result = new StringBuffer();
        result.append("IsValidForActivationResult=[");
        result.append("objectType=");
        result.append(getObjectType());
        result.append(",objectID=");
        result.append(getObjectID());
        result.append(",languageCode=");
        result.append(getLanguageCode());
        result.append(", comment=");
        result.append(getComment());
        result.append(", msg=");
        result.append(msg);
        result.append("]");
        return result.toString();
    }

    public boolean equals(final Object obj) {
        if (this == obj) return true;

        if (IsValidForActivationResults.class == obj.getClass()) {
            final IsValidForActivationResults tmp = (IsValidForActivationResults) obj;
            final boolean interim = tmp.getObjectType().equals(getObjectType()) && tmp.getObjectID() == getObjectID();
            if (interim) {
                final String tmpComment = tmp.getComment() == null ? "" : tmp.getComment();
                final EngineMessage tmpMsg = tmp.getMsg() == null ? new EngineMessage() : tmp.getMsg();
                return tmpComment.equals(getComment()) || tmpMsg.equals(getMsg());
            }
        }
        return false;
    }
}
