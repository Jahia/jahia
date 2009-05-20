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
