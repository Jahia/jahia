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