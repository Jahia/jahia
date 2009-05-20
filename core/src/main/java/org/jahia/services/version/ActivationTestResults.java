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

import org.jahia.content.TreeOperationResult;

/**
 * <p>Title: Activation test results</p>
 * <p>Description: This class is used to signal warnings if an activation
 * wouldn't complete in the case where we couldn't validate through the full
 * hierarchy due to flags that prohibit that, such as dependencies on sub-
 * content that hasn't been validated, or missing language entries for
 * mandatory languages.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author Serge Huber
 * @version 1.0
 */

public class ActivationTestResults extends TreeOperationResult {
    private static final long serialVersionUID = 3851832795337429744L;

    public ActivationTestResults() {
    }

    public ActivationTestResults(int initialStatus) {
        super(initialStatus);
    }

}