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

package org.jahia.services.fields;

import org.jahia.content.ContentObjectKey;
import org.jahia.content.NodeOperationResult;
import org.jahia.engines.EngineMessage;

/**
 * @author Xavier Lawrence
 */
public class WAIValidForActivationResults extends NodeOperationResult {
    private static final long serialVersionUID = -4867045020574408090L;

    /**
     * Constructor for the result.
     * @param languageCode the language for which this result is given
     * @param msg          An EngineMessage to use for internationalization
     * @throws ClassNotFoundException thrown if the object type is not
     *                                recognized by Jahia
     * @see org.jahia.content.ObjectKey
     */
    public WAIValidForActivationResults(ContentObjectKey objectKey,
                                        final String languageCode,
                                        final EngineMessage msg) throws ClassNotFoundException {
        super(objectKey, languageCode, null, msg);
    }

    public String toString() {
        final StringBuffer result = new StringBuffer();
        result.append("WAIValidForActivationResults=[");
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

        if (WAIValidForActivationResults.class == obj.getClass()) {
            final WAIValidForActivationResults tmp = (WAIValidForActivationResults) obj;
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
