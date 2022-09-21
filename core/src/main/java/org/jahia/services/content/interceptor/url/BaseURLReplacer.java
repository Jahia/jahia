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
package org.jahia.services.content.interceptor.url;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.services.content.*;
import org.jahia.services.content.interceptor.URLInterceptor;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.utils.WebUtils;
import org.jahia.utils.i18n.JahiaLocaleContextHolder;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

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
                throw new PropertyConstraintViolationException(node, Messages.getInternal("label.error.invalidlink",
                        JahiaLocaleContextHolder.getLocale(), "Invalid link") + pathPart, definition.isInternationalized() ? locale :
                        null,definition);
            }
            pathPart = m.group(5);
            isCmsContext = true;
        } else {
            return originalValue;
        }

        String pathTmp = StringUtils.substringBeforeLast("/" + WebUtils.urlDecode(pathPart), "?");
        pathTmp = StringUtils.substringBeforeLast(pathTmp, "#");
        final String path = pathTmp;

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
                    throw new PropertyConstraintViolationException(node, Messages.getInternal("label.error.invalidlink",
                            JahiaLocaleContextHolder.getLocale(), "Invalid link") + path, definition.isInternationalized() ?
                            locale : null,
                            definition);
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
