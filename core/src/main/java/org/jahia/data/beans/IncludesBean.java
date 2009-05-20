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
package org.jahia.data.beans;

import org.jahia.params.ProcessingContext;
import org.jahia.hibernate.manager.SpringContextSingleton;

/**
 * Proxy class for resolving templates and Web resources path considering
 * template set inheritance introduced with the Jahia Include Tag.
 * 
 * @author Sergiy Shyrkov
 */
public class IncludesBean {

    private TemplatePathResolverBean templatePathResolver;

    private WebPathResolverBean webPathResolver;

    IncludesBean(final ProcessingContext processingContext) {
        TemplatePathResolverFactory factory = (TemplatePathResolverFactory) SpringContextSingleton.getInstance().getContext().getBean("TemplatePathResolverFactory");
        templatePathResolver = factory.getTemplatePathResolver(processingContext);
        webPathResolver = factory.getWebPathResolver(processingContext);
    }

    public TemplatePathResolverBean getTemplatePath() {
        return templatePathResolver;
    }

    public WebPathResolverBean getWebPath() {
        return webPathResolver;
    }
}
