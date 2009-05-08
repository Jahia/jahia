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
package org.jahia.taglibs.utility.i18n;

import org.apache.log4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.params.ProcessingContext;
import org.jahia.taglibs.utility.Utils;
import org.jahia.utils.i18n.JahiaResourceBundle;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.jstl.core.Config;
import javax.servlet.jsp.jstl.fmt.LocalizationContext;
import javax.servlet.jsp.tagext.TagSupport;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Used to set the resource bundle to be used in the page by &lt;fmt:message/&gt; tags.
 * User: rincevent
 * Date: 26 févr. 2009
 * Time: 16:34:03
 */
@SuppressWarnings("serial")
public class SetBundleTag extends TagSupport {
    
    private static final transient Logger logger = Logger.getLogger(SetBundleTag.class);
    
    private String basename;
    private String var;
    private int scope;

    public SetBundleTag() {
        super();
        init();
    }

    private void init() {
        basename = null;
        scope = PageContext.PAGE_SCOPE;
    }

    @Override
    public int doStartTag() throws JspException {
        // Position localisationContext
        if (basename != null && !"".equals(basename)) {
            ProcessingContext context = null;
            try {
                context = Utils.getProcessingContext(pageContext, true);
            } catch (JahiaBadRequestException e) {
                logger.debug(e.getMessage(), e);
            }
            final Locale locale = context != null ? context.getLocale() : pageContext.getRequest().getLocale();
            ResourceBundle resourceBundle = new JahiaResourceBundle(basename,
                    locale,
                    context != null && context.getSite() != null ? context
                            .getSite().getTemplatePackageName() : null);
            LocalizationContext locCtxt = new LocalizationContext(resourceBundle, locale);
            if (var != null) {
                pageContext.setAttribute(var, locCtxt, scope);
            } else {
                Config.set(pageContext, Config.FMT_LOCALIZATION_CONTEXT, locCtxt, scope);
            }
        } else {
            logger.warn("Provided 'basename' for the tag is either null or empty");
        }
        return EVAL_BODY_INCLUDE;
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
}
