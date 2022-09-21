/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.utility.i18n;

import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.utils.i18n.ResourceBundles;
import org.slf4j.Logger;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Used to set the resource bundle to be used in the page by &lt;fmt:message/&gt; tags.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 16:34:03
 */
@SuppressWarnings("serial")
public class SetBundleTag extends AbstractJahiaTag {

    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(SetBundleTag.class);

    private String basename;
    private String var;
    private int scope = PageContext.PAGE_SCOPE;
    private boolean useUiLocale = false;
    private String templateName;

    @Override
    protected void resetState() {
        basename = null;
        useUiLocale = false;
        scope = PageContext.PAGE_SCOPE;
        var = null;
        templateName = null;
        super.resetState();
    }

    @Override
    public int doStartTag() throws JspException {
        // Position localisationContext
        if (basename != null && !"".equals(basename)) {
            RenderContext context = getRenderContext();
            Locale locale = useUiLocale ? getUILocale() : null;
            if (locale == null) {
                try {
                    locale = context != null ? context.getMainResourceLocale() : (Locale) pageContext.getAttribute(Constants.SESSION_LOCALE, PageContext.SESSION_SCOPE);
                } catch (Exception e) {
                    logger.debug(e.getMessage(), e);
                    locale = pageContext.getRequest().getLocale();
                }
            }
            if (locale == null) {
                locale = pageContext.getRequest().getLocale();
            }
            ResourceBundle resourceBundle = null;
            try {
                if (templateName == null) {
                    resourceBundle = ResourceBundles.get(basename, locale);
                } else {
                    resourceBundle = ResourceBundles.get(basename, ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(templateName), locale);
                }
            } catch (MissingResourceException e) {
                logger.warn(e.getMessage(), e);
            }
            LocalizationContext locCtxt = new LocalizationContext(resourceBundle, locale);
            if (var != null) {
                pageContext.setAttribute(var, locCtxt, scope);
            } else {
                Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
            }
        } else {
            logger.warn("Provided 'basename' for the tag is either null or empty");
        }
        return SKIP_BODY;
    }

    public void setBasename(String basename) {
        this.basename = basename;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    public void setUseUILocale(String useUILocale) {
        this.useUiLocale = Boolean.parseBoolean(useUILocale);
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
