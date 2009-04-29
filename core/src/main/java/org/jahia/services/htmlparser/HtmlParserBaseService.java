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
//
package org.jahia.services.htmlparser;

import org.apache.commons.lang.StringUtils;
import org.jahia.bin.JahiaConfig;
import org.jahia.data.FormDataManager;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;

import java.util.*;


/**
 * <p>Title: Html parsing service , implementation based on Tidy </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class HtmlParserBaseService extends HtmlParserService {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(HtmlParserBaseService.class);

    static private HtmlParserBaseService instance = null;

    private HtmlParser parser = null;

    private JahiaConfig jahiaConfig;

    protected HtmlParserBaseService() {
    }

    public JahiaConfig getJahiaConfig() {
        return jahiaConfig;
    }

    public void setJahiaConfig(JahiaConfig jahiaConfig) {
        this.jahiaConfig = jahiaConfig;
    }

    public void start()
            throws JahiaInitializationException {

        this.parser = loadHtmlParser();
    }

    public void stop() {
    }

    public static HtmlParserBaseService getInstance() {
        if (instance == null) {
            synchronized (HtmlParserBaseService.class) {
                if (instance == null) {
                    instance = new HtmlParserBaseService();
                }
            }
        }
        return instance;
    }

    /**
     * Return a parser no regard to a specific site.
     */
    public HtmlParser getParser() {
        return parser;
    }

    /**
     * Return a parser for a given site.
     *
     * @param siteId
     */
    public HtmlParser getParser(int siteId) {
        return parser;
    }

    /**
     * Parse an input document, checkout html markups integrity,
     * remove unwanted markups.
     * <p/>
     * Using default parser and default setting
     *
     * @param inputString
     * @param DOMVisitors
     */
    public String parse(String inputString, List DOMVisitors) {
        if (parser == null) {
            return inputString;
        }
        return parser.parse(inputString, DOMVisitors);
    }

    /**
     * Parse an input document, checkout html markups integrity,
     * remove unwanted markups.
     * Using parser defined and configured for the given site
     *
     * @param inputString
     * @param DOMVisitors
     * @param siteId
     */
    public String parse(String inputString, List DOMVisitors, int siteId) {
        if (parser == null) {
            return inputString;
        }
        return parser.parse(inputString, DOMVisitors, siteId);
    }


    /**
     * Return a List of registered HtmlDOMVisitors
     *
     * @param siteId
     */
    public List getHtmlDOMVisitors(int siteId) throws JahiaException {
        List v = new LinkedList();
        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService()
                .getSite(siteId);
        if (site.isHtmlMarkupFilteringEnabled()) {
            String tags = site.getHtmlMarkupFilteringTags();
            if (StringUtils.isNotEmpty(tags)) {
                RemoveUnwantedMarkupVisitor visitor = new RemoveUnwantedMarkupVisitor(
                        StringUtils.split(tags, ", "));
                visitor.init(siteId);
                v.add(visitor);
            }
        }
        return v;
    }

    /**
     * Check if there is any html parsing errors
     * If there is any error, store an MessageError with property key = "htmlParser"
     *
     * @param value String, the result of a previous Html parsing
     * @return EngineMessages
     */
    public EngineMessages checkTextParsingErrors(String value) {
        final EngineMessages resultMessages = new EngineMessages();
        if (value != null && value.indexOf(TidyHtmlParser.TIDYERRORS_TAG) != -1) {
            String errorMsg = value.substring(value.indexOf(TidyHtmlParser.TIDYERRORS_TAG)
                    + TidyHtmlParser.TIDYERRORS_TAG.length() + 1,
                    value.length() - TidyHtmlParser.TIDYERRORS_TAG.length() - 3);
            EngineMessage errorMessage = new EngineMessage("org.jahia.data.HtmlParser.htmlParserError",
                    FormDataManager.formEncode(errorMsg));
            resultMessages.add("htmlParser", errorMessage);
        }
        return resultMessages;
    }

    /**
     * @return HtmlParser
     */
    private HtmlParser loadHtmlParser() {
        final String className = jahiaConfig.getProperty("org.jahia.services.htmlparser.HtmlParser");
        if (className != null) {
            try {
                final Class c = Class.forName(className);
                final HtmlParser parser = (HtmlParser) c.newInstance();
                if (parser != null) {
                    parser.init(this);
                }
                return parser;
            } catch (ClassNotFoundException cnfe) {
                logger.error(cnfe);
            } catch (InstantiationException ie) {
                logger.error(ie);
            } catch (IllegalAccessException iae) {
                logger.error(iae);
            }
        }
        return null;
    }

}

