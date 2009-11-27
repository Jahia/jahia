//package org.jahia.services.content.interceptor;
//
//import org.apache.log4j.Logger;
//import org.apache.commons.lang.StringUtils;
//import org.jahia.services.content.JCRPropertyWrapper;
//import org.jahia.services.content.JCRNodeWrapper;
//import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
//import org.jahia.services.content.nodetypes.SelectorType;
//import org.jahia.services.sites.JahiaSite;
//import org.jahia.services.pages.ContentPage;
//import org.jahia.params.ProcessingContext;
//import org.jahia.exceptions.JahiaException;
//import org.jahia.exceptions.JahiaPageNotFoundException;
//import org.jahia.hibernate.manager.JahiaFieldXRefManager;
//import org.jahia.registries.ServicesRegistry;
//import net.htmlparser.jericho.*;
//
//import java.util.List;
//import java.util.regex.Matcher;
//import java.net.URLDecoder;
//import java.net.URL;
//import java.net.MalformedURLException;
//import java.io.UnsupportedEncodingException;
//
///**
// * Created by IntelliJ IDEA.
// * User: toto
// * Date: Nov 27, 2009
// * Time: 10:33:08 AM
// * To change this template use File | Settings | File Templates.
// */
//public class URLInterceptor22 {
//    private static Logger logger = Logger.getLogger(PropertyInterceptor.class);
//
//    public void post(JCRPropertyWrapper property) {
//        if (((ExtendedPropertyDefinition)property.getDefinition()).getSelector() == SelectorType.RICHTEXT) {
//
//
//
//
//        }
//    }
//
//
//    /**
//     * Replaces the URL marker in all the internal Jahia links and generates
//     * a valid and usable URL for each marked links
//     *
//     * @param content The content of the BigText
//     * @return a String with valid and usable URLs.
//     */
//    public static String rewriteURLs(String content, final ProcessingContext processingContext) {
//        if (processingContext.getContextPath() == null) return content;
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("rewriteURLs... " + content);
//        }
//
//        try {
//            if (content == null || content.length() == 0) {
//                return "";
//            }
//
//            if (content.indexOf(URL_MARKER) < 0) { // nothing to rewrite in terms of URLs...
//                return content;
//            }
//            Source source = new Source(content);
//            OutputDocument document = new OutputDocument(source);
//            List<StartTag> linkTags = source.getAllStartTags(HTMLElementName.A);
//            for (StartTag startTag : linkTags) {
//                final Attributes attributes = startTag.getAttributes();
//                final Attribute href = attributes.get("href");
//                restoreURL(processingContext, document, href);
//            }
//            linkTags = source.getAllStartTags(HTMLElementName.IMG);
//            for (StartTag startTag : linkTags) {
//                final Attributes attributes = startTag.getAttributes();
//                final Attribute href = attributes.get("src");
//                restoreURL(processingContext, document, href);
//            }
//            linkTags = source.getAllStartTags(HTMLElementName.PARAM);
//            for (StartTag startTag : linkTags) {
//                final Attributes attributes = startTag.getAttributes();
//                final Attribute href = attributes.get("value");
//                restoreURL(processingContext, document, href);
//            }
//            return document.toString();
//        } catch (Exception e) {
//            logger.error("Error while rewriting the URLs !", e);
//            return null;
//        }
//    }
//
//    /**
//     * @param content           The content of the BigText
//     * @param code              The current language code
//     * @param sessionAttribute  Name of session attribute to check for value
//     * @return an Array of String. Position[0] contains the RawValue and position[1] the
//     *         value that should be used when displaying the data
//     */
//    private String cleanUpHardCodedLinks(final String content) {
//        logger.debug("cleanUpHardCodedLinks...");
//
//        internalLinks.clear();
//        files.clear();
//        wrongURLKeys.clear();
//
//        Source source = new Source((new SourceFormatter(new Source(content))).toString());
//        OutputDocument document = new OutputDocument(source);
//        List<StartTag> linkTags = source.getAllStartTags(HTMLElementName.A);
//        for (StartTag startTag : linkTags) {
//            final Attributes attributes = startTag.getAttributes();
//            final Attribute href = attributes.get("href");
//            if(href!=null)
//            cleanURL(document, href);
//        }
//        linkTags = source.getAllStartTags(HTMLElementName.IMG);
//        for (StartTag startTag : linkTags) {
//            final Attributes attributes = startTag.getAttributes();
//            final Attribute href = attributes.get("src");
//            if(href != null)
//            cleanURL(document, href);
//        }
//        linkTags = source.getAllStartTags(HTMLElementName.PARAM);
//        for (StartTag startTag : linkTags) {
//            final Attributes attributes = startTag.getAttributes();
//            final Attribute href = attributes.get("value");
//            if(href!=null)
//            cleanURL(document, href);
//        }
//        return document.toString();
//    }
//
//    private static void restoreURL (ProcessingContext processingContext, OutputDocument document, Attribute href) throws JahiaException {
//        if (href == null) {
//            return;
//        }
//        String originalHrefValue = href.getValue();
//        String hrefValue = originalHrefValue;
//        if (hrefValue.contains(URL_MARKER)) {
//            if (hrefValue.startsWith(URL_MARKER + JahiaFieldXRefManager.PAGE)) {
//                String[] values = hrefValue.split("/");
//                String type = values[values.length-2];
//                String pageId = values[values.length-1];
//                if ("pid".equals(type)) {
//                    String suffix = null;
//                    if (pageId.contains("?")) {
//                        suffix = "?" + StringUtils.substringAfter(pageId, "?");
//                        pageId = StringUtils.substringBefore(pageId, "?");
//                    } else if (pageId.contains("#")) {
//                        suffix = "#" + StringUtils.substringAfter(pageId, "#");
//                        pageId = StringUtils.substringBefore(pageId, "#");
//                    }
//                    int pid = Integer.valueOf(pageId);
//                    // This is a url key is there a site in the url to force appending it ?
//                    String site = null;
//                    if (hrefValue.indexOf("/site/") > 0) {
//                        int i = hrefValue.indexOf("/site/") + 6;
//                        site = hrefValue.substring(i, hrefValue.indexOf("/", i));
//                        if (site.equals(processingContext.getSiteKey())) {
//                            site = null;
//                        }
//                    }
//                    JahiaSite jahiaSite = (site != null) ? ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(site) : processingContext.getSite();
//                    String language = null;
//                    if (hrefValue.indexOf("/lang/") > 0) {
//                        int i = hrefValue.indexOf("/lang/") + 6;
//                        language = hrefValue.substring(i, hrefValue.indexOf("/", i));
//                    }
//
//                    if (site == null) {
//                        // Check pid is on same site
//                        ContentPage page = null;
//                        try {
//                            page = ContentPage.getPage(pid);
//                        } catch (JahiaPageNotFoundException ex) {
//                            // non-existing page
//                        }
//                        if (page != null && page.getSiteID() != jahiaSite.getID()) {
//                            jahiaSite = page.getSite();
//                            site = jahiaSite.getSiteKey();
//                        }
//                    }
//                    hrefValue = getSiteURL(jahiaSite, pid, false, language, site != null, processingContext) + (suffix != null ? suffix : "");
//                } else if ("ref".equals(type)) {
//                    hrefValue = href.getValue();
//                } else {
//                    hrefValue = "";
//                }
//            } else if (hrefValue.startsWith(URL_MARKER + JahiaFieldXRefManager.FILE)) {
//                // This is a file
//                try {
//                    String path = URLDecoder.decode(hrefValue.substring((URL_MARKER + JahiaFieldXRefManager.FILE).length()), "UTF-8");
//                    final JCRNodeWrapper node = ServicesRegistry.getInstance().getJCRStoreService().getFileNode(path, processingContext.getUser());
//                    if (!node.isValid()) {
//                        logger.warn("Unable to retrieve a node for the path: " + path);
//                    }
//                    hrefValue = node.getUrl();
//                } catch (UnsupportedEncodingException e) {
//                    logger.error(e.getMessage(), e);
//                }
//            }
//            if (hrefValue != null && !originalHrefValue.equals(hrefValue)) {
//                document.replace(href.getValueSegment(), hrefValue);
//            }
//        }
//    }
//
//
//    private void cleanURL(OutputDocument document, Attribute href) {
//        String originalHrefValue = href.getValue();
//        String hrefValue = originalHrefValue;
//        final String hrefValueLowerCase = hrefValue.toLowerCase();
//        if (!hrefValueLowerCase.startsWith("http") && !hrefValueLowerCase.startsWith("javascript")
//            && !hrefValueLowerCase.startsWith("mailto") && !hrefValueLowerCase.startsWith("ftp")
//            && !hrefValueLowerCase.startsWith("news") && !hrefValueLowerCase.startsWith("wais")
//            && !hrefValueLowerCase.startsWith("gopher") && !hrefValueLowerCase.startsWith("#")) {
//            // This is an internal link
//            hrefValue = handleCurrentServerPath(hrefValue, null);
//        } else if (hrefValueLowerCase.startsWith("http")) {
//            // This is an absolute URL
//            try {
//                URL targetURL = new URL(hrefValue);
//                // First try to find if it is a current site url
//                String host = targetURL.getHost();
//                if (host.equals(processingContext.getSite().getServerName())) {
//                    // This is a local site URL
//                    hrefValue = handleCurrentServerPath(targetURL.getPath(), null);
//                } else {
//                    try {
//                        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByServerName(host);
//                        if (site != null) {
//                            // This is a same server site so handle it
//                            hrefValue = handleCurrentServerPath(targetURL.getPath(), site.getSiteKey());
//                        }
//                    } catch (JahiaException e) {
//                        logger.warn(e.getMessage(), e);
//                    }
//                }
//            } catch (MalformedURLException e) {
//                logger.warn(e.getMessage(), e);
//            }
//        }
//        if (hrefValue != null && !originalHrefValue.equals(hrefValue)) {
//            document.replace(href.getValueSegment(), hrefValue);
//        }
//    }
//
//    private String handleCurrentServerPath(String hrefValue, String siteKey) {
//        // Is it a file or is it a page ?
//        final Matcher matcher = FILE_PATTERN.matcher(hrefValue);
//        if (!matcher.matches()) {
//            // This is a page
//            String pageId = StringUtils.substringAfterLast(hrefValue, "/");
//            String suffix = null;
//            if (pageId.contains("?")) {
//                suffix = "?" + StringUtils.substringAfter(pageId, "?");
//                pageId = StringUtils.substringBefore(pageId, "?");
//            } else if (pageId.contains("#")) {
//                suffix = "#" + StringUtils.substringAfter(pageId, "#");
//                pageId = StringUtils.substringBefore(pageId, "#");
//            }
//
//
//            // This is a url key is there a site in the url to force appending it ?
//            String site = siteKey;
//            if (hrefValue.indexOf("/site/") > 0) {
//                int i = hrefValue.indexOf("/site/") + 6;
//                site = hrefValue.substring(i, hrefValue.indexOf("/", i));
//                if (site.equals(processingContext.getSiteKey())) {
//                    site = null;
//                }
//            }
//            String language = code.toString();
//            if (hrefValue.indexOf("/lang/") > 0) {
//                int i = hrefValue.indexOf("/lang/") + 6;
//                language = hrefValue.substring(i, hrefValue.indexOf("/", i));
//            }
//            try {
//                int pid = Integer.parseInt(pageId);
//                internalLinks.add(pid);
//                // This is a pid
//                hrefValue = URL_MARKER + JahiaFieldXRefManager.PAGE + "/lang/" + language + "/pid/" + pid + (suffix != null ? suffix : "");
//            } catch (NumberFormatException e) {
//                // This is not a pid this is a url key
//                try {
//                    JahiaSite jahiaSite = (site != null) ? ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey(site) : processingContext.getSite();
//                    int urlKey = getPidFromUrlKey(hrefValue, processingContext.getContextPath(), jahiaSite);
//                    if (urlKey > 0) {
//                        internalLinks.add(urlKey);
//                        hrefValue = URL_MARKER + JahiaFieldXRefManager.PAGE +(site != null ? "/site/" + site : "") + "/lang/" + language + "/pid/" + urlKey + (suffix != null ? suffix : "");
//                    } else {
//                        // Todo find a way of uses tuckey rewriter rules to decode URL
//                    }
//                } catch (JahiaException e1) {
//                    logger.warn(e.getMessage(), e);
//                }
//            }
//        } else {
//            try {
//                final String path = URLDecoder.decode(matcher.group(6), "UTF-8");
//                hrefValue = URL_MARKER + JahiaFieldXRefManager.FILE +path;
//                files.add(path);
//            } catch (UnsupportedEncodingException e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
//        return hrefValue;
//    }
//
//}
