/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.seo.urlrewrite;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter.ModeCondition;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jahia HTML Tag attribute visitor for applying URL rewriting rules (using {@link UrlRewriteService}).
 * 
 * @author Sergiy Shyrkov
 */
public class UrlRewriteVisitor implements HtmlTagAttributeVisitor {
    private static Logger logger = LoggerFactory.getLogger(UrlRewriteVisitor.class);

    private String[] applyOnModes;

    private UrlRewriteService urlRewriteService;

    private boolean preconditionsMatch(String attrValue, RenderContext context, Resource resource) {
        if (StringUtils.isEmpty(attrValue)) {
            return false;
        }

        if (applyOnModes != null && applyOnModes.length > 0) {
            for (String mode : applyOnModes) {
                if (ModeCondition.matches(context, mode)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

    public void setApplyOnModes(String[] applyOnModes) {
        this.applyOnModes = applyOnModes;
    }

    /**
     * Applies the configured URL rewriting rules to the URL value.
     * 
     * @see org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor#visit(String, org.jahia.services.render.RenderContext, String, String, org.jahia.services.render.Resource)
     */
    public String visit(final String attrValue, RenderContext context, String tagName, String attrName, Resource resource) {
        String value = attrValue;
        if (preconditionsMatch(attrValue, context, resource)) {
            long timer = System.currentTimeMillis();
            try {
                String rewritten = urlRewriteService.rewriteOutbound(attrValue,
                        context.getRequest(), context.getResponse());
                value = rewritten;
            } catch (Exception e) {
                logger.error("Error rewriting URL value " + attrValue + " Skipped rewriting.", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Rewriting URL {} into {} took {} ms", new Object[] { attrValue,
                        value, System.currentTimeMillis() - timer });
            }
        }
        return value;
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }
}
