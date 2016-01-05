/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.GWTResourceBundle;
import org.jahia.ajax.gwt.client.data.GWTResourceBundleEntry;
import org.jahia.ajax.gwt.client.data.node.GWTJahiaNode;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Helper class for populating {@link GWTResourceBundle} data in the {@link GWTJahiaNode} instance.
 * 
 * @author Sergiy Shyrkov
 */
public final class GWTResourceBundleUtils {

    /**
     * Unicode escaping utility. Code from the ResourceBundle Editor Eclipse plugin (http://eclipse-rbe.sourceforge.net)
     */
    private static final class EscapeUtils {

        private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F' };

        static String convertUnicodeToEncoded(String str) {
            int len = str.length();
            StringBuilder outBuffer = new StringBuilder(len * 2);

            for (int x = 0; x < len; x++) {
                char aChar = str.charAt(x);
                if ((aChar < ' ') || (aChar > '~')) {
                    outBuffer.append('\\');
                    outBuffer.append('u');
                    outBuffer.append(toHex(aChar >> '\f' & 0xF));
                    outBuffer.append(toHex(aChar >> '\b' & 0xF));
                    outBuffer.append(toHex(aChar >> '\004' & 0xF));
                    outBuffer.append(toHex(aChar & 0xF));
                } else {
                    outBuffer.append(aChar);
                }
            }
            return outBuffer.toString();
        }

        private static char toHex(int nibble) {
            char hexChar = HEX_DIGITS[(nibble & 0xF)];
            return Character.toLowerCase(hexChar);
        }
    }

    private static Logger logger = LoggerFactory.getLogger(GWTResourceBundleUtils.class);

    private static String getAsString(String lang, GWTResourceBundle bundle) {
        StringBuilder out = new StringBuilder(512);
        for (GWTResourceBundleEntry entry : bundle.getEntries()) {
            String value = entry.getValue(lang);
            if (StringUtils.isEmpty(value)) {
                // we do not write entries with empty values
                continue;
            }
            out.append(entry.getKey()).append("=")
                    .append(EscapeUtils.convertUnicodeToEncoded(value)).append("\n");
        }

        return out.toString();
    }

    private static String getLanguageCode(String resourceBundleFileName) {
        String name = StringUtils.substringBeforeLast(resourceBundleFileName, ".properties");
        String lang = GWTResourceBundle.DEFAULT_LANG;
        if (name.contains("_")) {
            String[] parts = Patterns.UNDERSCORE.split(name);
            int l = parts.length;
            if (l == 2) {
                lang = parts[1];
            } else if (l == 3) {
                lang = parts[1] + "_" + parts[2];
            } else if (l >= 4) {
                lang = parts[l - 3] + "_" + parts[l - 2] + "_" + parts[l - 1];
            }
        }

        return lang;
    }

    private static boolean isValidKey(String key) {
        return key != null && key.length() > 0 && key.indexOf('<') == -1 && key.indexOf('=') == -1 && key.indexOf('>') == -1;
    }

    public static GWTResourceBundle load(JCRNodeWrapper node, Locale uiLocale) {
        GWTResourceBundle gwtBundle = null;
        long timer = System.currentTimeMillis();
        try {
            boolean isFile = false;
            if (!(isFile = node.isNodeType(Constants.JAHIANT_RESOURCEBUNDLE_FILE))
                    && !node.isNodeType(Constants.JAHIANT_RESOURCEBUNDLE_FOLDER)) {
                return null;
            }

            gwtBundle = new GWTResourceBundle();

            Set<String> languages = new HashSet<String>();
            List<JCRNodeWrapper> rbFileNodes = JCRContentUtils.getChildrenOfType(
                    isFile ? node.getParent() : node, Constants.JAHIANT_RESOURCEBUNDLE_FILE);
            for (JCRNodeWrapper rbFileNode : rbFileNodes) {
                populate(gwtBundle, rbFileNode, languages);
            }
            for (GWTResourceBundleEntry entry : gwtBundle.getEntryMap().values()) {
                for (String l : languages) {
                    if (entry.getValues().get(l) == null) {
                        // add the language to that entry
                        entry.setValue(l, null);
                    }
                }
            }
            // if empty, define a default bundle
            if (gwtBundle.getEntries().isEmpty()) {
                gwtBundle.setName(node.getResolveSite().getTemplatePackage().getId());
                gwtBundle.setValue("<empty>",GWTResourceBundle.DEFAULT_LANG,null);
            }

            // load available languages
            List<GWTJahiaValueDisplayBean> availableLocales = new ArrayList<GWTJahiaValueDisplayBean>();
            for (Locale l : LanguageCodeConverters.getSortedLocaleList(uiLocale)) {
                availableLocales.add(new GWTJahiaValueDisplayBean(l.toString(), StringUtils
                        .capitalize(l.getDisplayName(uiLocale))));
            }
            gwtBundle.setAvailableLanguages(availableLocales);

            if (logger.isDebugEnabled()) {
                logger.debug("Loaded resource bundle for node {} in {} ms", node.getPath(),
                        System.currentTimeMillis() - timer);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return gwtBundle;
    }

    public static void lock(JCRNodeWrapper node) {
        try {
            boolean isFile = false;
            if (!(isFile = node.isNodeType(Constants.JAHIANT_RESOURCEBUNDLE_FILE))
                    && !node.isNodeType(Constants.JAHIANT_RESOURCEBUNDLE_FOLDER)) {
                return;
            }

            final JCRNodeWrapper parent = isFile ? node.getParent() : node;
            List<JCRNodeWrapper> rbFileNodes = JCRContentUtils.getChildrenOfType(
                    parent, Constants.JAHIANT_RESOURCEBUNDLE_FILE);
            for (JCRNodeWrapper rbFileNode : rbFileNodes) {
                rbFileNode.lockAndStoreToken("engine");
            }
            parent.lockAndStoreToken("engine");
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return;
    }

    private static void populate(GWTResourceBundle gwtBundle, JCRNodeWrapper node,
            Set<String> languages) {

        String name = StringUtils.substringBeforeLast(node.getName(), ".properties");
        String lang = GWTResourceBundle.DEFAULT_LANG;
        if (name.contains("_")) {
            lang = getLanguageCode(name);
            if (gwtBundle.getName() == null) {
                name = StringUtils.substringBeforeLast(StringUtils.substringBeforeLast(name, lang),
                        "_");
            }
        }
        if (gwtBundle.getName() == null) {
            gwtBundle.setName(name);
        }
        languages.add(lang);

        InputStream is = null;
        try {
            is = node.getFileContent().downloadFile();
            Properties p = new Properties();
            p.load(is);
            for (Object keyObj : p.keySet()) {
                String key = keyObj.toString();
                if (isValidKey(key)) {
                    gwtBundle.setValue(key, lang, p.getProperty(key));
                }
            }
        } catch (IOException e) {
            logger.error("Error reading content of the " + node.getPath()
                    + " node as a properties file. Cause: " + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static boolean store(GWTJahiaNode gwtNode, GWTResourceBundle bundle,
            JCRSessionWrapper session) throws GWTJahiaServiceException {
        boolean needPermissionReload = false;
        try {
            JCRNodeWrapper node = session.getNode(gwtNode.isFile() ? StringUtils
                    .substringBeforeLast(gwtNode.getPath(), "/") : gwtNode.getPath());

            Map<String, JCRNodeWrapper> nodesByLanguage = new HashMap<String, JCRNodeWrapper>();
            for (JCRNodeWrapper rbFileNode : JCRContentUtils.getChildrenOfType(node,"jnt:propertiesFile")) {
                nodesByLanguage.put(getLanguageCode(rbFileNode.getName()), rbFileNode);
            }

            for (String lang : bundle.getLanguages()) {
                JCRNodeWrapper current = nodesByLanguage.remove(lang);
                InputStream is = null;
                try {
                    is = new ByteArrayInputStream(getAsString(lang, bundle).getBytes("ISO-8859-1"));
                    if (current == null) {
                        // new language
                        logger.debug("Processing new resource bundle for language '{}'", lang);
                        String bundleName;
                        if (StringUtils.equals(lang,GWTResourceBundle.DEFAULT_LANG)) {
                            bundleName = bundle.getName() + ".properties";
                        }  else {
                            bundleName = bundle.getName() + "_" + lang + ".properties";
                        }
                        node.uploadFile(bundleName, is,
                                "text/plain");
                    } else {
                        // updating existing language
                        logger.debug("Processing resource bundle {} for language '{}'",
                                current.getPath(), lang);
                        current.getFileContent().uploadFile(is, "text/plain");
                    }
                } catch (UnsupportedEncodingException e) {
                    throw new IllegalArgumentException(e.getMessage(), e);
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }

            // removing deleted languages if any
            for (Iterator<JCRNodeWrapper> iterator = nodesByLanguage.values().iterator(); iterator
                    .hasNext();) {
                JCRNodeWrapper toDelete = iterator.next();
                logger.debug("Removing resource bundle node {}", toDelete.getPath());
                toDelete.remove();
            }
            ResourceBundle.clearCache();
            NodeTypeRegistry.getInstance().flushLabels();
            needPermissionReload = !node.getResolveSite().getLanguages().containsAll(bundle.getLanguages()); 
            node.getResolveSite().setLanguages(bundle.getLanguages());
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
        return needPermissionReload;
    }

    public static void unlock(JCRNodeWrapper node) {
        try {
            boolean isFile = false;
            if (!(isFile = node.isNodeType(Constants.JAHIANT_RESOURCEBUNDLE_FILE))
                    && !node.isNodeType(Constants.JAHIANT_RESOURCEBUNDLE_FOLDER)) {
                return;
            }

            final JCRNodeWrapper parent = isFile ? node.getParent() : node;
            List<JCRNodeWrapper> rbFileNodes = JCRContentUtils.getChildrenOfType(
                    parent, Constants.JAHIANT_RESOURCEBUNDLE_FILE);
            for (JCRNodeWrapper rbFileNode : rbFileNodes) {
                if (rbFileNode.isLocked()) {
                    rbFileNode.unlock("engine");
                }
            }
            if (parent.isLocked()) {
                parent.unlock("engine");
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return;
    }



    private GWTResourceBundleUtils() {
        super();
    }
}
