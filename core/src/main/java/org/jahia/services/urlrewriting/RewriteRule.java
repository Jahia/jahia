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
 package org.jahia.services.urlrewriting;

import java.util.Properties;
import org.jahia.exceptions.JahiaException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public abstract class RewriteRule {

    protected Properties originalFromRewrittenProps;
    protected Properties rewrittenFromOriginalProps;

    public RewriteRule(Properties originalFromRewrittenProps, Properties rewrittenFromOriginalProps) throws JahiaException {
        this.originalFromRewrittenProps = originalFromRewrittenProps;
        this.rewrittenFromOriginalProps = rewrittenFromOriginalProps;
    }

    public abstract String getOriginalFromRewritten(String rewrittenURL);

    public abstract String getRewrittenFromOriginal(String originalURL);

}