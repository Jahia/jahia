/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
