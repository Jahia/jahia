/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.render.filter;

import org.apache.commons.lang.StringUtils;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;
import org.jahia.services.seo.urlrewrite.UrlRewriteService;
import org.jahia.services.uicomponents.bean.editmode.EditConfiguration;
import org.jahia.utils.Url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Replaces contextual placeholders in internal links like ##mode## and ##lang## with their actual value.
 *
 * ##mode## is the servlet name followed by the active workspace ( edit/default or render/live )
 * ##lang## is the current language.
 *
 */
public class ContextPlaceholdersReplacer implements HtmlTagAttributeVisitor {

    public static String CURRENT_CONTEXT_PLACEHOLDER;
    public static String WORKSPACE_PLACEHOLDER;
    public static String LANG_PLACEHOLDER;

    static {
        CURRENT_CONTEXT_PLACEHOLDER = "{mode}";
        WORKSPACE_PLACEHOLDER = "{workspace}";
        LANG_PLACEHOLDER = "{lang}";
    }

    private static Pattern CTX_PATTERN = Pattern.compile(CURRENT_CONTEXT_PLACEHOLDER, Pattern.LITERAL);
    private static Pattern WORKSPACE_PATTERN = Pattern.compile(WORKSPACE_PLACEHOLDER, Pattern.LITERAL);
    public static Pattern LANG_PATTERN = Pattern.compile(LANG_PLACEHOLDER, Pattern.LITERAL);
    public static Pattern SERVER_PATTERN = Pattern.compile("\\{server:([a-zA-Z_0-9\\-\\.]+)\\}");

    private UrlRewriteService urlRewriteService;

    public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
        if (value != null) {
            String contextPath = null;
            if(context.isEditMode() && ! context.isContributionMode()){
                contextPath = StringUtils.substringAfterLast(((EditConfiguration)SpringContextSingleton.getBean(context.getEditModeConfigName())).getDefaultUrlMapping(), "/");
            } else if(context.isContributionMode()){
               contextPath = "contribute";
            } else{
               contextPath = "render";
            }
            Matcher serverMatcher = SERVER_PATTERN.matcher(value);
            if (serverMatcher.find()) {
                String serverName = serverMatcher.group(1);
                value = serverMatcher.replaceFirst("");
                if (urlRewriteService != null && urlRewriteService.isSeoRulesEnabled() && context.isLiveMode()) {
                    value = Url.getServer(context.getRequest(), serverName) + value;
                }
            }
            value = LANG_PATTERN.matcher(
                    CTX_PATTERN.matcher(value).replaceAll(contextPath+"/"+resource.getWorkspace())).replaceAll(resource.getLocale().toString());
            value = WORKSPACE_PATTERN.matcher(value).replaceAll(resource.getWorkspace());
        }

        return value;
    }

    public void setUrlRewriteService(UrlRewriteService urlRewriteService) {
        this.urlRewriteService = urlRewriteService;
    }

}