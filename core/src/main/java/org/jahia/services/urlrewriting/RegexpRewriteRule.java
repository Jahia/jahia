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
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.exceptions.JahiaException;
/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class RegexpRewriteRule extends RewriteRule {

    private RE originalFromRewrittenRegexp;
    private RE rewrittenFromOriginalRegexp;
    private String originalFromRewrittenSubst;
    private String rewrittenFromOriginalSubst;

    public RegexpRewriteRule(Properties originalFromRewrittenProps, Properties rewrittenFromOriginalProps) throws JahiaException {
        super(originalFromRewrittenProps, rewrittenFromOriginalProps);

        try {
            originalFromRewrittenRegexp = new RE(originalFromRewrittenProps.
                                                 getProperty("regexp"));
            originalFromRewrittenSubst = originalFromRewrittenProps.
                                         getProperty("subst");
            rewrittenFromOriginalRegexp = new RE(rewrittenFromOriginalProps.getProperty("regexp"));
            rewrittenFromOriginalSubst = rewrittenFromOriginalProps.
                                         getProperty("subst");

        } catch (RESyntaxException rese) {
            throw new JahiaException("Error while instantiation regpexp",
                                     "Error in URL rewriting",
                                     JahiaException.DATA_ERROR,
                                     JahiaException.ERROR_SEVERITY,
                                     rese);
        }
    }

    public String getOriginalFromRewritten(String rewrittenURL) {
        if (originalFromRewrittenRegexp.match(rewrittenURL)) {
            return originalFromRewrittenRegexp.subst(rewrittenURL, originalFromRewrittenSubst);
        } else {
            return null;
        }
    }

    public String getRewrittenFromOriginal(String originalURL) {
        if (rewrittenFromOriginalRegexp.match(originalURL)) {
            return rewrittenFromOriginalRegexp.subst(originalURL, rewrittenFromOriginalSubst);
        } else {
            return null;
        }
    }
}