/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

import java.util.regex.Pattern;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.HtmlTagAttributeTraverser.HtmlTagAttributeVisitor;

/**
 * Replaces contextual placeholders in internal links like ##mode## and ##lang## with their actual value.
 *
 * ##mode## is the servlet name followed by the active workspace ( edit/default or render/live )
 * ##lang## is the current language.
 *
 */
public class ContextPlaceholdersReplacer implements HtmlTagAttributeVisitor {

    public static String CURRENT_CONTEXT_PLACEHOLDER = "{mode}";
    public static String WORKSPACE_PLACEHOLDER = "{workspace}";
    public static String LANG_PLACEHOLDER = "{lang}";
    
    private static Pattern CTX_PATTERN = Pattern.compile(CURRENT_CONTEXT_PLACEHOLDER, Pattern.LITERAL);
    private static Pattern WORKSPACE_PATTERN = Pattern.compile(WORKSPACE_PLACEHOLDER, Pattern.LITERAL);
    private static Pattern LANG_PATTERN = Pattern.compile(LANG_PLACEHOLDER, Pattern.LITERAL);
    
    public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
        if (value != null) {
            String contextPath = null;
            if(context.isEditMode()){
               contextPath = "edit";
            } else if(context.isContributionMode()){
               contextPath = "contribute";
            } else{
               contextPath = "render";
            }
            value = LANG_PATTERN.matcher(
                    CTX_PATTERN.matcher(value).replaceAll(contextPath+"/"+resource.getWorkspace())).replaceAll(resource.getLocale().toString());
            value = WORKSPACE_PATTERN.matcher(value).replaceAll(resource.getWorkspace());
        }
        
        return value;
    }
    
}