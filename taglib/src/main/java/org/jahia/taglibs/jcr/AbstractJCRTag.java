/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.jcr;

import java.util.Locale;

import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.LanguageCodeConverters;

/**
 * Base tag for the JCR related tags.
 * 
 * @author Sergiy Shyrkov
 */
public class AbstractJCRTag extends AbstractJahiaTag {

    private static final long serialVersionUID = 3559634552167343791L;

    protected String getWorkspace() throws RepositoryException {
        String workspace = null;
        Resource resource = getCurrentResource();
        RenderContext ctx = getRenderContext();
        resource = resource != null ? resource : (ctx != null ? ctx.getMainResource() : null);
        if (resource != null) {
            workspace = resource.getWorkspace();
        }
        return workspace;
    }
    
    protected Locale getLocale() {
        Locale locale = null;
        Resource resource = getCurrentResource();
        RenderContext ctx = getRenderContext();
        resource = resource != null ? resource : (ctx != null ? ctx.getMainResource() : null);
        if (resource != null) {
            locale = resource.getLocale();
        } else {
            locale = LanguageCodeConverters.languageCodeToLocale(getLanguageCode());
        }
        return locale;
    } 
    
    protected JCRSessionWrapper getJCRSession() throws RepositoryException {
        RenderContext ctx = getRenderContext();
        JCRSessionWrapper session = ctx != null ? JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace(), getLocale(),
                ctx.getFallbackLocale()) : JCRSessionFactory.getInstance().getCurrentUserSession(getWorkspace(), getLocale());
        return session;
    }

    @Override
    protected void resetState() {
        super.resetState();
    }

}