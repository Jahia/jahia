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
//  JahiaBigTextField
//  YG      17.07.2001

package org.jahia.data.fields;

import au.id.jericho.lib.html.Attribute;
import au.id.jericho.lib.html.Attributes;
import au.id.jericho.lib.html.OutputDocument;
import au.id.jericho.lib.html.Source;
import au.id.jericho.lib.html.SourceFormatter;
import au.id.jericho.lib.html.StartTag;
import au.id.jericho.lib.html.Tag;

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.extractor.HTMLTextExtractor;
import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.bin.Jahia;
import org.jahia.data.FormDataManager;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.operations.valves.SkeletonParseAndStoreValve;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.fields.ContentBigTextField;
import org.jahia.services.fields.ContentFieldTools;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.PageProperty;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.version.ContentObjectEntryState;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.EntrySaveRequest;
import org.jahia.utils.FileUtils;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.TextHtml;

import javax.jcr.RepositoryException;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JahiaBigTextField extends JahiaField implements
        JahiaAllowApplyChangeToAllLangField {

    private static final long serialVersionUID = -3021494722875802228L;

    private static final Logger logger = Logger.getLogger(JahiaBigTextField.class);

    public static final String URL_MARKER = "###";

    private static final Pattern FILE_PATTERN = Pattern.compile("^(/[a-z]*)?(/((repository/default)|(files))(/.*))");
    
    // Holds a set of 'pid' as Integer objects
    private final Set<Integer> internalLinks = new HashSet<Integer>();
    private final Set<String> files = new HashSet<String>();
    private final Set<String> wrongURLKeys = new HashSet<String>();

    /**
     * constructor
     */
    public JahiaBigTextField(final Integer ID,
                             final Integer jahiaID,
                             final Integer pageID,
                             final Integer ctnid,
                             final Integer fieldDefID,
                             final Integer fieldType,
                             final Integer connectType,
                             final String fieldValue,
                             final Integer rank,
                             final Integer aclID,
                             final Integer versionID,
                             final Integer versionStatus,
                             final String languageCode) {
        super(ID, jahiaID, pageID, ctnid, fieldDefID, fieldType, connectType,
                fieldValue, rank, aclID, versionID, versionStatus, languageCode);
    } // end constructor

    public void load(final int loadFlag, final ProcessingContext jParams,
                     final EntryLoadRequest loadRequest) throws JahiaException {

        if (logger.isDebugEnabled()) {
            logger.debug("Loading big text field: " + getID());
        }

        final ContentBigTextField contentBigTextField = (ContentBigTextField)
                ContentBigTextField.getField(getID());

        String val;

        if (this.getWorkflowState() >
                ContentObjectEntryState.WORKFLOW_STATE_ACTIVE
                && this.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE) {
            ContentObjectEntryState entryState =
                    new ContentObjectEntryState(ContentObjectEntryState.
                            WORKFLOW_STATE_START_STAGING,
                            0, this.getLanguageCode());
            val = contentBigTextField.getValue(entryState);
        } else {
            val = contentBigTextField.getValue(jParams, loadRequest);
        }

        this.setRawValue(val);
        if (logger.isDebugEnabled()) {
            logger.debug("RawValue: " + this.getRawValue());
        }

        if (val != null && val.indexOf(URL_MARKER) > 0) {
            val = rewriteURLs(val, jParams);
            if (logger.isDebugEnabled()) {
                logger.debug("Rewritten URLs: " + val);
            }
        }

        this.setValue(FormDataManager.htmlEncode(val));
        if (logger.isDebugEnabled()) {
            logger.debug("Value: " + this.getValue());
        }

        // populate variables internalLinks and wrongURLKeys
        cleanUpHardCodedLinks(getValue(), jParams, LanguageCodeConverters
                .languageCodeToLocale(getLanguageCode()), null);
        
        if (logger.isDebugEnabled()) {
            logger.debug("Populated variables: links = " + internalLinks +
                    ", wrongKeys = " + wrongURLKeys);
        }
    }

    /**
     *
     */
    public boolean save(final ProcessingContext jParams) throws JahiaException {
        if (logger.isDebugEnabled()) {
            logger.debug("Save Big Text: " + getID() + ", Value: " + getValue());
        }
        ContentBigTextField contentBigTextField = (ContentBigTextField)ContentBigTextField.getField(getID());
        boolean isNew = false;
        if (contentBigTextField == null) {
            contentBigTextField = (ContentBigTextField) ContentFieldTools.getInstance().createContentFieldInstance(0, getJahiaID(), getPageID(), getctnid(),
                    getFieldDefID(), getType(), getConnectType(), getAclID(), new ArrayList<ContentObjectEntryState>(), new HashMap<ContentObjectEntryState, String>());
            contentBigTextField.setMetadataOwnerObjectKey(getMetadataOwnerObjectKey());
            isNew = true;
        }

        final SessionState sessionState = Jahia.getThreadParamBean().getSessionState();
        final String sessionAttribute = new StringBuffer().append("ContentFieldBigTextValues_").append(this.getID()).
                append("_").append(this.getFieldDefID()).toString();

        if (getRawValue() != null && getRawValue().equals(contentBigTextField.getValue(jParams))) {
            sessionState.removeAttribute(sessionAttribute);
            return true;
        }

        jParams.getSessionState().setAttribute("FireContainerUpdated", "true");
        if (logger.isDebugEnabled())
            logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");

        String value = getValue();
        if (value != null && !value.equals(JahiaField.NULL_STRING_MARKER)) {
            value = cleanUpHardCodedLinks(value, jParams, jParams.getLocale(), sessionAttribute);
        }
        setRawValue(value);
        sessionState.removeAttribute(sessionAttribute);

        final EntrySaveRequest saveRequest = new EntrySaveRequest(jParams.getUser(), this.getLanguageCode(), isNew);
        final String defaultValue = getDefinition().getDefaultValue();
        contentBigTextField.setText(getRawValue(), saveRequest, defaultValue);

        if (getID() == 0) {
            setID(contentBigTextField.getID());
        }

        // first we remove all the existing references for this object. Pages and files
        JahiaFieldXRefManager fieldLinkManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        fieldLinkManager.deleteReferencesForField(contentBigTextField.getID(), languageCode, EntryLoadRequest.STAGING_WORKFLOW_STATE);
        Set<Integer> pageRefs = getInternalLinks();
        for (Integer pageRef : pageRefs) {
            String target = JahiaFieldXRefManager.PAGE+pageRef;
            fieldLinkManager.createFieldReference(contentBigTextField.getID(), jahiaID, languageCode, EntryLoadRequest.STAGING_WORKFLOW_STATE, target);
        }
        Set<String> files = getFiles();
        for (String path : files) {
            JCRNodeWrapper file = JCRStoreService.getInstance().getFileNode(path,jParams.getUser());
            if (file.isValid()) {
                String target; 
                try {
                    target = JahiaFieldXRefManager.FILE + file.getProvider().getKey() + ":" + file.getUUID();
                } catch (RepositoryException e) {
                    target = JahiaFieldXRefManager.FILE + path;
                }
                fieldLinkManager.createFieldReference(contentBigTextField.getID(), jahiaID, languageCode, EntryLoadRequest.STAGING_WORKFLOW_STATE, target);
            }
        }
        
        return true;
    }

    public String getEngineName() {
        return "org.jahia.engines.shared.BigText_Field";
    }

    public String getFieldContent4Ranking() {
        String fieldInfo = this.getValue();
        fieldInfo = JahiaTools.html2text(fieldInfo);
        try {
            fieldInfo = (new RE("<(.*?)>")).subst(fieldInfo, "");
        } catch (RESyntaxException re) {
            logger.error(re.toString(), re);
        } catch (Exception t) {
            logger.error(t.toString(), t);
        }

        if (fieldInfo.length() > 30) {
            fieldInfo = fieldInfo.substring(0, 30) + " ...";
        }

        return fieldInfo;
    }

    public String getIconNameOff() {
        return "bigtext";
    }

    public String getIconNameOn() {
        return "bigtext_on";
    }

    /**
     * Is this kind of field shared (i.e. not one version for each language,
     * but one version for every language)
     */
    public boolean isShared() {
        return false;
    }

    /**
     * Copy the internal value of current language to another language.
     * Must be implemented by conctrete field for specific implementation.
     *
     * @param aField A same field in another language
     */
    public void copyValueInAnotherLanguage(final JahiaField aField,
                                           final ProcessingContext jParams) throws JahiaException {
        if (aField == null) {
            return;
        }
        aField.setValue(this.getValue());
        aField.setRawValue(this.getRawValue());
        aField.setObject(this.getObject());
    }

    /**
     * Returns the set of 'pids' contained in this BigText field
     */
    public Set<Integer> getInternalLinks() {
        return internalLinks;
    }

    public Set<String> getFiles() {
        return files;
    }

    /**
     * Returns the set of invalid URLKeys present in this BigText field
     */
    public Set<String> getWrongURLKeys() {
        return wrongURLKeys;
    }

    /**
     * Replaces the URL marker in all the internal Jahia links and generates
     * a valid and usable URL for each marked links
     *
     * @param content The content of the BigText
     * @return a String with valid and usable URLs.
     */
    public static String rewriteURLs(String content, final ProcessingContext processingContext) {
        if (processingContext.getContextPath() == null) return content;

        if (logger.isDebugEnabled()) {
            logger.debug("rewriteURLs... " + content);
        }

        try {
            if (content == null || content.length() == 0) {
                return "";
            }

            if (content.indexOf(URL_MARKER) < 0) { // nothing to rewrite in terms of URLs...
                return content;
            }
            Source source = new Source(content);
            OutputDocument document = new OutputDocument(source);
            List<StartTag> linkTags = source.findAllStartTags(Tag.A);
            for (StartTag startTag : linkTags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute href = attributes.get("href");
                restoreURL(processingContext, document, href);
            }
            linkTags = source.findAllStartTags(Tag.IMG);
            for (StartTag startTag : linkTags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute href = attributes.get("src");
                restoreURL(processingContext, document, href);
            }
            linkTags = source.findAllStartTags(Tag.PARAM);
            for (StartTag startTag : linkTags) {
                final Attributes attributes = startTag.getAttributes();
                final Attribute href = attributes.get("value");
                restoreURL(processingContext, document, href);
            }
            return document.toString();
        } catch (Exception e) {
            logger.error("Error while rewriting the URLs !", e);
            return null;
        }
    }

    /**
     * @param content           The content of the BigText
     * @param processingContext The context of the current request
     * @param code              The current language code
     * @param sessionAttribute  Name of session attribute to check for value
     * @return an Array of String. Position[0] contains the RawValue and position[1] the
     *         value that should be used when displaying the data
     */
    private String cleanUpHardCodedLinks(
            final String content,
            final ProcessingContext processingContext,
            final Locale code,
            final String sessionAttribute) {
        logger.debug("cleanUpHardCodedLinks...");
        final SessionState sessionState = Jahia.getThreadParamBean().getSessionState();
        final String values = sessionAttribute !=  null ? (String) sessionState.getAttribute(sessionAttribute) : null;
        if (values != null) {
            return values;
        }
        internalLinks.clear();
        files.clear();
        wrongURLKeys.clear();
        String cleanContent = cleanHtml(content);
        Source source = new Source((new SourceFormatter(new Source(cleanContent))).toString());
        OutputDocument document = new OutputDocument(source);
        List<StartTag> linkTags = source.findAllStartTags(Tag.A);
        for (StartTag startTag : linkTags) {
            final Attributes attributes = startTag.getAttributes();
            final Attribute href = attributes.get("href");
            if(href!=null)
            cleanURL(processingContext, code, document, href);
        }
        linkTags = source.findAllStartTags(Tag.IMG);
        for (StartTag startTag : linkTags) {
            final Attributes attributes = startTag.getAttributes();
            final Attribute href = attributes.get("src");
            if(href != null)
            cleanURL(processingContext, code, document, href);
        }
        linkTags = source.findAllStartTags(Tag.PARAM);
        for (StartTag startTag : linkTags) {
            final Attributes attributes = startTag.getAttributes();
            final Attribute href = attributes.get("value");
            if(href!=null)
            cleanURL(processingContext, code, document, href);
        }
        return document.toString();
    }



    /**
     * Tries to find a matching pid value from a possible URL key contained
     * in the given link.
     *
     * @return The pid value or -1 if not found or something went wrong
     */
    protected int getPidFromUrlKey (final String link,
                                    final String context, JahiaSite jahiaSite) {
        if (logger.isDebugEnabled()) {
            logger.debug("getPidFromUrlKey: " + link);
        }
        if (link == null || link.length() == 0) {
            return -1;
        }

        if (!link.startsWith(context) && !link.startsWith(URL_MARKER)
                || link.startsWith(URL_MARKER + JahiaFieldXRefManager.FILE) || link.startsWith(context + "/files"))
            return -1;

        String urlKey;
        try {
            urlKey = URLDecoder.decode(StringUtils.substringAfterLast(link, "/"), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            urlKey = StringUtils.substringAfterLast(link, "/");
        }

        urlKey = StringUtils.substringBefore(StringUtils.substringBefore(
                urlKey, "?"), "#");

        if (logger.isDebugEnabled()) {
            logger.debug("urlKey: " + urlKey);
        }

        if (urlKey.length() == 0 || Character.isDigit(urlKey.charAt(0))) return -1;

        try {
            List<PageProperty> pageProperties = null;
            final ServicesRegistry registry = ServicesRegistry.getInstance();
            JahiaSite siteByKey = jahiaSite;
            if (siteByKey == null && link.indexOf("/site") >= 0) {
                String siteKey = link.substring(link.indexOf("/site/") + 6).split("/")[0];
                siteByKey = registry.getJahiaSitesService().getSiteByKey(siteKey);
            }
            int siteID = -1;
            if (siteByKey != null) {
                siteID = siteByKey.getID();
            }
            if (siteID > -1) {
                pageProperties = registry.getJahiaPageService().getPagePropertiesByValueAndSiteID(urlKey, siteID);
            }

            if (pageProperties == null) {
                pageProperties = registry.getJahiaPageService().getPagePropertiesByValue(urlKey);
            }
            if (pageProperties.size() == 1) {
                final PageProperty pageProperty = (PageProperty) pageProperties.get(0);
                if (pageProperty.getName().equals(PageProperty.PAGE_URL_KEY_PROPNAME)) {
                    return pageProperty.getPageID();
                }
            }
        } catch (Exception e) {
            logger.warn("Error while looking URL page key " + urlKey, e);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("wrongURLKeys added: " + urlKey);
        }
        wrongURLKeys.add(urlKey);

        return -1;
    }

    /**
     * Returns true if the given String object only contains numbers
     */
    protected static boolean hasOnlyDigits(final String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     *
     */
    protected static String getPID(final String s) {
        if (logger.isDebugEnabled()) {
            logger.debug("Getting PID from: " + s);
        }
        final StringBuffer buff = new StringBuffer();
        String value;

        final JahiaSite currentSite = Jahia.getThreadParamBean().getSite();
        final String currentHostName = currentSite.getServerName();

        final int httpIndex = s.indexOf("http://");
        if (httpIndex > -1) {
            String host = s.substring(httpIndex + 7);
            host = host.substring(0, host.indexOf("/"));
            if (!host.startsWith(currentHostName)) {
                return "-1";
            }
        }

        final int index;
        if ((index = s.indexOf("/pid/")) > -1) {
            value = s.substring(index + 5);

        } else if (s.startsWith("/")) {
            value = s.substring(1);

        } else {
            value = s;
        }

        final int index2 = value.indexOf("?");
        if (index2 > -1) {
            value = value.substring(0, index2);
        }

        if (hasOnlyDigits(value)) {
            return value;
        }

        for (int i = 0; i < value.length(); i++) {
            final char car = value.charAt(i);
            if (Character.isDigit(car)) {
                buff.append(car);
            } else {
                break;
            }
        }

        if (buff.length() == 0) return "-1";

        return buff.toString();
    }

    public String toString() {
        return new StringBuilder().append(getClass().getName()).append(": ID = ").
                append(getID()).append(", Value = ").
                append(getValue()).append(", RawValue = ").
                append(getRawValue()).toString();
    }

    private static String cleanHtml (String bodyContent) {
        // Try to remove all cache/(o|b).*/ and also jessionid
        String cleanBodyContent = bodyContent.replaceAll("cache/(o|b)[a-z]*/", "");
        cleanBodyContent = cleanBodyContent.replaceAll(SkeletonParseAndStoreValve.SESSION_ID_REGEXP, "$1");
        return cleanBodyContent;
    }

    private void cleanURL (ProcessingContext processingContext, Locale code, OutputDocument document, Attribute href) {
        String originalHrefValue = href.getValue();
        String hrefValue = originalHrefValue;
        final String hrefValueLowerCase = hrefValue.toLowerCase();
        if (!hrefValueLowerCase.startsWith("http") && !hrefValueLowerCase.startsWith("javascript")
            && !hrefValueLowerCase.startsWith("mailto") && !hrefValueLowerCase.startsWith("ftp")
            && !hrefValueLowerCase.startsWith("news") && !hrefValueLowerCase.startsWith("wais")
            && !hrefValueLowerCase.startsWith("gopher") && !hrefValueLowerCase.startsWith("#")) {
            // This is an internal link
            hrefValue = handleCurrentServerPath(processingContext, code, hrefValue, null);
        } else if (hrefValueLowerCase.startsWith("http")) {
            // This is an absolute URL
            try {
                URL targetURL = new URL(hrefValue);
                // First try to find if it is a current site url
                String host = targetURL.getHost();
                if (host.equals(processingContext.getSite().getServerName())) {
                    // This is a local site URL
                    hrefValue = handleCurrentServerPath(processingContext, code, targetURL.getPath(), null);
                } else {
                    try {
                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByServerName(host);
                        if (site != null) {
                            // This is a same server site so handle it
                            hrefValue = handleCurrentServerPath(processingContext, code, targetURL.getPath(), site.getSiteKey());
                        }
                    } catch (JahiaException e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            } catch (MalformedURLException e) {
                logger.warn(e.getMessage(), e);
            }
        }
        if (hrefValue != null && !originalHrefValue.equals(hrefValue)) {
            document.replace(href.getValueSegment(), hrefValue);
        }
    }

    private String handleCurrentServerPath (ProcessingContext processingContext, Locale code, String hrefValue, String siteKey) {
        // Is it a file or is it a page ?
        final Matcher matcher = FILE_PATTERN.matcher(hrefValue);
        if (!matcher.matches()) {
            // This is a page
            String pageId = StringUtils.substringAfterLast(hrefValue, "/");
            String suffix = null;
            if (pageId.contains("?")) {
                suffix = "?" + StringUtils.substringAfter(pageId, "?");
                pageId = StringUtils.substringBefore(pageId, "?");
            } else if (pageId.contains("#")) {
                suffix = "#" + StringUtils.substringAfter(pageId, "#");
                pageId = StringUtils.substringBefore(pageId, "#");
            } 
 
            
            // This is a url key is there a site in the url to force appending it ?
            String site = siteKey;
            if (hrefValue.indexOf("/site/") > 0) {
                int i = hrefValue.indexOf("/site/") + 6;
                site = hrefValue.substring(i, hrefValue.indexOf("/", i));
                if (site.equals(processingContext.getSiteKey())) {
                    site = null;
                }
            }
            String language = code.toString();
            if (hrefValue.indexOf("/lang/") > 0) {
                int i = hrefValue.indexOf("/lang/") + 6;
                language = hrefValue.substring(i, hrefValue.indexOf("/", i));
            }
            try {
                int pid = Integer.parseInt(pageId);
                internalLinks.add(pid);
                // This is a pid
                hrefValue = URL_MARKER + JahiaFieldXRefManager.PAGE + "/lang/" + language + "/pid/" + pid + (suffix != null ? suffix : "");
            } catch (NumberFormatException e) {
                // This is not a pid this is a url key
                try {
                    JahiaSite jahiaSite = (site != null) ? ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(site) : processingContext.getSite();
                    int urlKey = getPidFromUrlKey(hrefValue, processingContext.getContextPath(), jahiaSite);
                    if (urlKey > 0) {
                        internalLinks.add(urlKey);
                        hrefValue = URL_MARKER + JahiaFieldXRefManager.PAGE +(site != null ? "/site/" + site : "") + "/lang/" + language + "/pid/" + urlKey + (suffix != null ? suffix : "");
                    } else {
                        // Todo find a way of uses tuckey rewriter rules to decode URL
                    }
                } catch (JahiaException e1) {
                    logger.warn(e.getMessage(), e);
                }
            }
        } else {
            try {
                final String path = URLDecoder.decode(matcher.group(6), "UTF-8");
                hrefValue = URL_MARKER + JahiaFieldXRefManager.FILE +path;
                files.add(path);
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return hrefValue;
    }

    private static void restoreURL (ProcessingContext processingContext, OutputDocument document, Attribute href) throws JahiaException {
        if (href == null) {
            return;
        }
        String originalHrefValue = href.getValue();
        String hrefValue = originalHrefValue;
        if (hrefValue.contains(URL_MARKER)) {
            if (hrefValue.startsWith(URL_MARKER + JahiaFieldXRefManager.PAGE)) {
                String[] values = hrefValue.split("/");
                String type = values[values.length-2];
                String pageId = values[values.length-1];
                if ("pid".equals(type)) {
                    String suffix = null;
                    if (pageId.contains("?")) {
                        suffix = "?" + StringUtils.substringAfter(pageId, "?");
                        pageId = StringUtils.substringBefore(pageId, "?");
                    } else if (pageId.contains("#")) {
                        suffix = "#" + StringUtils.substringAfter(pageId, "#");
                        pageId = StringUtils.substringBefore(pageId, "#");
                    } 
                    int pid = Integer.valueOf(pageId);
                    // This is a url key is there a site in the url to force appending it ?
                    String site = null;
                    if (hrefValue.indexOf("/site/") > 0) {
                        int i = hrefValue.indexOf("/site/") + 6;
                        site = hrefValue.substring(i, hrefValue.indexOf("/", i));
                        if (site.equals(processingContext.getSiteKey())) {
                            site = null;
                        }
                    }
                    JahiaSite jahiaSite = (site != null) ? ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(site) : processingContext.getSite();
                    String language = null;
                    if (hrefValue.indexOf("/lang/") > 0) {
                        int i = hrefValue.indexOf("/lang/") + 6;
                        language = hrefValue.substring(i, hrefValue.indexOf("/", i));
                    }

                    if (site == null) {
                        // Check pid is on same site
                        ContentPage page = null;
                        try {
                            page = ContentPage.getPage(pid);
                        } catch (JahiaPageNotFoundException ex) {
                            // non-existing page
                        }
                        if (page != null && page.getSiteID() != jahiaSite.getID()) {
                            jahiaSite = page.getSite();
                            site = jahiaSite.getSiteKey();
                        }
                    }
                    hrefValue = getSiteURL(jahiaSite, pid, false, language, site != null, processingContext) + (suffix != null ? suffix : "");
                } else if ("ref".equals(type)) {
                    hrefValue = href.getValue();
                } else {
                    hrefValue = "";
                }
            } else if (hrefValue.startsWith(URL_MARKER + JahiaFieldXRefManager.FILE)) {
                // This is a file
                try {
                    String path = URLDecoder.decode(hrefValue.substring((URL_MARKER + JahiaFieldXRefManager.FILE).length()), "UTF-8");
                    final JCRNodeWrapper node = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, processingContext.getUser());
                    if (!node.isValid()) {
                        logger.warn("Unable to retrieve a node for the path: " + path);
                    }
                    hrefValue = node.getUrl();
                } catch (UnsupportedEncodingException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (hrefValue != null && !originalHrefValue.equals(hrefValue)) {
                document.replace(href.getValueSegment(), hrefValue);
            }
        }
    }

    private static String getSiteURL (final JahiaSite theSite, final int pageID,
                                     final boolean withSessionID, String languageCode,
                                     boolean forceServerNameInURL, ProcessingContext processingContext) throws JahiaException {

        final String siteServerName = theSite.getServerName();
        String sessionIDStr = null;

        final StringBuilder newSiteURL = new StringBuilder(64);
        if (forceServerNameInURL) {
            newSiteURL.append(processingContext.getScheme()).append("://");
        }

        if (!forceServerNameInURL) {            
            newSiteURL.append(processingContext.getContextPath());
            newSiteURL.append(Jahia.getServletPath());
        } else {
            // let's construct an URL by deconstruct our current URL and inserting
            // the site id key as a parameter
            newSiteURL.append(siteServerName);

            int siteURLPortOverride = processingContext.settings().getSiteURLPortOverride();
            if (siteURLPortOverride > 0) {
                if (siteURLPortOverride != 80) {
                    newSiteURL.append(":");
                    newSiteURL.append(siteURLPortOverride);
                }
            } else if (processingContext.getServerPort() != 80) {
                newSiteURL.append(":");
                newSiteURL.append(processingContext.getServerPort());
            }
            newSiteURL.append(processingContext.getContextPath());
            newSiteURL.append(Jahia.getServletPath());
        }

        if (!processingContext.getOperationMode().equals(ProcessingContext.NORMAL)) {
            newSiteURL.append("/" + ProcessingContext.OPERATION_MODE_PARAMETER + "/").append(processingContext.getOperationMode());
        }

        if (languageCode != null) {
            newSiteURL.append("/" + ProcessingContext.LANGUAGE_CODE + "/").append(languageCode);
        }

        if (pageID != -1) {
            ContentPage page = null;
            try {
                page = ContentPage.getPage(pageID);
            } catch (JahiaPageNotFoundException e) {
                // non-existing page
            }
            if (page != null) {
                newSiteURL.append(processingContext.getPageURLKeyPart(pageID));
                newSiteURL.append(processingContext.getPageURLPart(pageID));
            } else {
                newSiteURL.append(
                        "/" + ProcessingContext.PAGE_ID_PARAMETER + "/")
                        .append(pageID);
            }
        }

        if (withSessionID) {
            String serverURL = processingContext.encodeURL(newSiteURL.toString());
            if (sessionIDStr != null) {
                if (serverURL.indexOf("jsessionid") == -1) {
                    serverURL += sessionIDStr;
                }
            }
            return serverURL;
        } else {
            return newSiteURL.toString();
        }
    }
    
    /**
     * Returns an array of values for the given language Code.
     * By Default, return the field values in the field current language code.
     *
     * @param languageCode
     * @return
     * @throws JahiaException
     */
    public String[] getValuesForSearch(String languageCode, ProcessingContext context, boolean expand) throws JahiaException {

        String[] values = this.getValues();
        if (values == null || values.length == 0) {
            values = EMPTY_STRING_ARRAY;
        }
        for (int i = 0; i < values.length; i++) {
            try {
                values[i] = FileUtils.readerToString((new HTMLTextExtractor()).extractText(
                        new ByteArrayInputStream(values[i].getBytes("UTF-8")), "text/html", "UTF-8"));
            } catch (Exception e) {
                values[i] = TextHtml.html2text(values[i]);
            } 
        }
        return values;
    } 
}
