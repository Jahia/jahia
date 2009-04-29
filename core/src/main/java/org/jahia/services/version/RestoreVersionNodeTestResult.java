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
package org.jahia.services.version;

import org.jahia.content.NodeOperationResult;
import org.jahia.content.ObjectKey;

/**
 * <p>Title: Contains the result of the test of a restore version on a
 * specific content object.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class RestoreVersionNodeTestResult extends NodeOperationResult {
    private static final long serialVersionUID = 1253569354200131367L;
    
    public RestoreVersionNodeTestResult (ObjectKey nodeKey, String languageCode,
                                         String comment) {
        super(nodeKey, languageCode, comment);
    }
}