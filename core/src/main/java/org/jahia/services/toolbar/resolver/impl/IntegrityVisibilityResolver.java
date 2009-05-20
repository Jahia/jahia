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
package org.jahia.services.toolbar.resolver.impl;

import org.jahia.data.JahiaData;
import org.jahia.params.ProcessingContext;
import org.jahia.services.toolbar.resolver.VisibilityResolver;

/**
 * Shows/hides the "Integrity checks" button depending on the URL integrity and
 * WAI compliance settings for the current virtual site.
 * 
 * @author Sergiy Shyrkov
 */
public class IntegrityVisibilityResolver implements VisibilityResolver {

    /*
     * (non-Javadoc)
     * @see
     * org.jahia.services.toolbar.resolver.VisibilityResolver#isVisible(org.
     * jahia.data.JahiaData, java.lang.String)
     */
    public boolean isVisible(JahiaData data, String type) {
        return ProcessingContext.EDIT.equals(data.getProcessingContext()
                .getOperationMode())
                && (data.getProcessingContext().getSite()
                        .isURLIntegrityCheckEnabled() || data
                        .getProcessingContext().getSite()
                        .isWAIComplianceCheckEnabled());
    }

}
