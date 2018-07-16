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
package org.jahia.services.content.interceptor.url;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.services.content.*;
import org.jahia.services.content.interceptor.URLInterceptor;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;

/**
 * Base URL replacer
 */
public class BaseURLReplacer implements URLReplacer {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(BaseURLReplacer.class);

    private URLInterceptor urlInterceptor;

    /** {@inheritDoc}. */
    @Override
    public boolean canHandle(String tagName, String attrName) {
        return true;
    }

    /** {@inheritDoc}. */
    @Override
    public String replaceRefsByPlaceholders(String originalValue, Map<String, Long> newRefs, Map<String, Long> oldRefs, String workspace, Locale locale, JCRNodeWrapper node, ExtendedPropertyDefinition definition) throws RepositoryException {
        if (logger.isDebugEnabled()) {
            logger.debug("Before replaceRefsByPlaceholders : " + originalValue);
        }

        String pathPart = originalValue;
        final boolean isCmsContext;
        if (pathPart.startsWith(urlInterceptor.getDmsContext())) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(StringUtils.substringAfter(pathPart, urlInterceptor.getDmsContext()), "/");
            isCmsContext = false;
        } else if (pathPart.startsWith(urlInterceptor.getCmsContext())) {
            // Remove CMS context part
            Matcher m = urlInterceptor.getCmsPattern().matcher(pathPart);
            if (!m.matches()) {
                throw new PropertyConstraintViolationException(node, Messages.getInternal("label.error.invalidlink", LocaleContextHolder.getLocale(), "Invalid link") + pathPart, definition.isInternationalized() ? locale : null,definition);
            }
            pathPart = m.group(5);
            isCmsContext = true;
        } else {
            return originalValue;
        }

        final String path = StringUtils.substringBeforeLast("/" + WebUtils.urlDecode(pathPart), "?");

        return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String value = originalValue;
                String ext = null;
                String tpl = null;
                JCRNodeWrapper reference;
                try {
                    String currentPath = path;
                    if (isCmsContext) {
                        while (true) {
                            int i = currentPath.lastIndexOf('.');
                            if (i > currentPath.lastIndexOf('/')) {
                                if (ext == null) {
                                    ext = currentPath.substring(i + 1);
                                } else if (tpl == null) {
                                    tpl = currentPath.substring(i + 1);
                                } else {
                                    tpl = currentPath.substring(i + 1) + "." + tpl;
                                }
                                currentPath = currentPath.substring(0, i);
                            } else {
                                throw new PathNotFoundException("not found in " + path);
                            }
                            try {
                                reference = session.getNode(JCRContentUtils.escapeNodePath(currentPath));
                                break;
                            } catch (PathNotFoundException e) {
                                // continue
                            }
                        }
                        value = URLInterceptor.CMS_CONTEXT_PLACEHOLDER + StringUtils.substringAfter(value, urlInterceptor.getCmsContext());
                    } else {
                        // retrieve path
                        while (true) {
                            if (StringUtils.contains(currentPath, '/')) {
                                currentPath = StringUtils.substringAfter(currentPath, "/");
                            } else {
                                throw new PathNotFoundException("not found in " + path);
                            }
                            try {
                                reference = session.getNode(JCRContentUtils.escapeNodePath("/" + currentPath));
                                break;
                            } catch (PathNotFoundException e) {
                                // continue
                            }
                        }
                        value = URLInterceptor.DOC_CONTEXT_PLACEHOLDER + StringUtils.substringAfter(value, urlInterceptor.getDmsContext());
                    }
                } catch (PathNotFoundException e) {
                    throw new PropertyConstraintViolationException(node, Messages.getInternal("label.error.invalidlink", LocaleContextHolder.getLocale(), "Invalid link") + path, definition.isInternationalized() ? locale : null, definition);
                }
                String id = reference.getIdentifier();
                if (!newRefs.containsKey(id)) {
                    if (oldRefs.containsKey(id)) {
                        newRefs.put(id, oldRefs.get(id));
                    } else {
                        Long max = Math.max(oldRefs.isEmpty() ? 0 : Collections.max(oldRefs.values()), newRefs.isEmpty() ? 0 : Collections.max(newRefs.values()));
                        newRefs.put(id, max + 1);
                    }
                }
                Long index = newRefs.get(id);
                String link = "/##ref:link" + index + "##";
                if (tpl != null) {
                    link += "." + tpl;
                }
                if (ext != null) {
                    link += "." + ext;
                }
                value = WebUtils.urlDecode(value).replace(path, link);
                if (logger.isDebugEnabled()) {
                    logger.debug("After replaceRefsByPlaceholders : " + value);
                }
                return value;
            }
        });
    }

    /** {@inheritDoc}. */
    @Override
    public String replacePlaceholdersByRefs(String originalValue, Map<Long, String> refs, String workspaceName, Locale locale, JCRNodeWrapper parent) throws RepositoryException {
        String pathPart = originalValue;
        if (logger.isDebugEnabled()) {
            logger.debug("Before replacePlaceholdersByRefs : " + originalValue);
        }
        final boolean isCmsContext;

        if (pathPart.startsWith(URLInterceptor.DOC_CONTEXT_PLACEHOLDER)) {
            // Remove DOC context part
            pathPart = StringUtils.substringAfter(StringUtils.substringAfter(pathPart, URLInterceptor.DOC_CONTEXT_PLACEHOLDER), "/");
            isCmsContext = false;
        } else if (pathPart.startsWith(URLInterceptor.CMS_CONTEXT_PLACEHOLDER)) {
            // Remove CMS context part
            Matcher m = urlInterceptor.getCmsPatternWithContextPlaceholder().matcher(pathPart);
            if (!m.matches()) {
                logger.error("Cannot match URL : " + pathPart);
                return originalValue;
            }
            pathPart = m.group(5);
            isCmsContext = true;
        } else {
            return originalValue;
        }

        final String path = "/" + pathPart;
        return JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(null, workspaceName, null, new JCRCallback<String>() {
            public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                String value = originalValue;
                try {
                    Matcher matcher = urlInterceptor.getRefPattern().matcher(path);
                    if (!matcher.matches()) {
                        logger.error("Cannot match value, should contain ##ref : " + path);
                        return originalValue;
                    }
                    String id = matcher.group(1);
                    String ext = matcher.group(2);
                    String uuid = refs.get(new Long(id));
                    String nodePath = null;

                    JCRNodeWrapper node = null;
                    if (!StringUtils.isEmpty(uuid)) {
                        try {
                            node = session.getNodeByUUID(uuid);
                        } catch (ItemNotFoundException infe) {
                            // Warning is logged below (also if uuid is empty)
                        }
                    }
                    if (node == null) {
                        logger.warn("Cannot find referenced item : " + parent.getPath() + " -> " + path + " -> " + uuid);
                        return "#";
                    }
                    nodePath = Text.escapePath(node.getPath());
                    value = originalValue.replace(path, nodePath + ext);
                    if (isCmsContext) {
                        value = URLInterceptor.CMS_CONTEXT_PLACEHOLDER_PATTERN.matcher(value).replaceAll(urlInterceptor.getCmsContext());
                    } else {
                        value = URLInterceptor.DOC_CONTEXT_PLACEHOLDER_PATTERN.matcher(value).replaceAll(urlInterceptor.getDmsContext());
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("After replacePlaceholdersByRefs : " + value);
                    }
                } catch (Exception e) {
                    logger.error("Exception when transforming placeholder for " + parent.getPath() + " -> " + path, e);
                }
                return value;
            }
        });
    }

    public URLInterceptor getUrlInterceptor() {
        return urlInterceptor;
    }

    public void setUrlInterceptor(URLInterceptor urlInterceptor) {
        this.urlInterceptor = urlInterceptor;
    }
}
