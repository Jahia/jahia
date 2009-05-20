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
