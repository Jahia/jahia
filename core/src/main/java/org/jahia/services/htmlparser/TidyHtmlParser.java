/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.htmlparser;

import org.jahia.registries.ServicesRegistry;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.properties.PropertiesManager;
import org.w3c.dom.Document;
import org.w3c.tidy.Tidy;
import org.xml.sax.EntityResolver;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;

/**
 * <p>Title: Html Parser default implementation based on Tidy</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class TidyHtmlParser implements HtmlParser {

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(TidyHtmlParser.class);

    public static final String AMPERSAND = "$$$amp$$$";
    public static final String AMPERSAND_SECONDPASS = "$$$amp_secondpass$$$";
    public static final String TIDYERRORS_TAG = "TIDYERRORS";

    private static final List newInlineTags = new ArrayList();
    private static final List newBlockLevelTags = new ArrayList();
    private static final List unrecognizedTags = new ArrayList();

    private Properties config;

    public TidyHtmlParser() {
        config = new Properties();
    }

    public TidyHtmlParser(Properties config) {
        this.config = config;
        if (this.config == null) {
            this.config = new Properties();
        }
    }

    /**
     * @param htmlParserService HtmlParserService
     */
    public void init(HtmlParserService htmlParserService) {

        SettingsBean settings = htmlParserService.getSettingsBean();
        if (settings == null) {
            return;
        }
        String fileName = settings.getPropertiesFile().getProperty("tidyConfig");
        if (fileName == null || "".equals(fileName.trim())) {
            fileName = "tidy.properties";
        }
        StringBuffer buff = new StringBuffer(settings.getJahiaEtcDiskPath());
        buff.append(File.separator);
        buff.append("config");
        buff.append(File.separator);
        buff.append(fileName);
        try {
            PropertiesManager propManager = new PropertiesManager(buff.toString());
            this.config = propManager.getPropertiesObject();
        } catch (Exception t) {
            logger.debug("Error loading tidy config file, use default settings ", t);
        }
        if (this.config == null) {
            this.config = new Properties();
        }
    }

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using default settings
     *
     * @param inputString
     * @param DOMVisitors
     */
    public String parse(String inputString, List DOMVisitors) {
        return parse(inputString, -1, config, DOMVisitors);
    }

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using settings as defined for a given site
     *
     * @param inputString
     * @param DOMVisitors
     */
    public String parse(String inputString, List DOMVisitors,
                        int siteId) {
        if (inputString == null || inputString.trim().equals("")) {
            return inputString;
        }
        return parse(inputString, siteId, config, DOMVisitors);
    }

    /**
     * Parses and generates a clean html document, remove unwanted markups,..
     * Using settings as defined for a given site
     *
     * @param input
     * @param tidyConfig
     * @param DOMVisitors
     */
    public static String parse(final String input,
                               int siteId,
                               Properties tidyConfig,
                               List DOMVisitors) {

        if (input == null || "".equals(input.trim())) {
            return input;
        }
        String result = input;
        result = JahiaTools.replacePattern(result, "&", AMPERSAND);

        ByteArrayInputStream strIn;
        ByteArrayOutputStream strOut;
        Tidy tidy = new Tidy();

        Properties config = (Properties) tidyConfig.clone();
        String val = tidyConfig.getProperty(TidyConfig.NEW_BLOCK_LEVEL_TAGS);
        if (val == null) {
            val = "";
        }
        int size = newBlockLevelTags.size();
        for (int i = 0; i < size; i++) {
            final String tag = (String) newBlockLevelTags.get(i);
            if (val.length() == 0) {
                val = tag;
            } else {
                val += "," + tag;
            }
        }
        config.setProperty(TidyConfig.NEW_BLOCK_LEVEL_TAGS, val);

        val = config.getProperty(TidyConfig.NEW_INLINE_TAGS);
        if (val == null) {
            val = "";
        }
        size = newInlineTags.size();
        for (int i = 0; i < size; i++) {
            final String tag = (String) newInlineTags.get(i);
            if (val.length() == 0) {
                val = tag;
            } else {
                val += "," + tag;
            }
        }

        config.setProperty(TidyConfig.NEW_INLINE_TAGS, val);

        // charset
        byte[] strByte = null;
        String charSet = SettingsBean.getInstance().getDefaultResponseBodyEncoding();
        if ("UTF-8".equalsIgnoreCase(charSet) && config.getProperty(TidyConfig.CHAR_ENCODING) == null) {
            config.setProperty(TidyConfig.CHAR_ENCODING, "utf8");
        }

        tidy.setConfigurationFromProps(config);

        try {
            if (charSet == null) {
                strByte = result.getBytes();
            } else {
                strByte = result.getBytes(charSet);
            }
            strIn = new ByteArrayInputStream(strByte);
            strOut = new ByteArrayOutputStream();
            ByteArrayOutputStream strErr = new ByteArrayOutputStream();
            tidy.setErrout(new PrintWriter(strErr, true));
            tidy.setShowWarnings(false);
            tidy.parse(strIn, strOut);

            strIn.reset();
            String tmpValue = null;
            if (charSet == null) {
                tmpValue = strOut.toString();
            } else {
                tmpValue = strOut.toString(charSet);
            }
            tmpValue = JahiaTools.replacePattern(tmpValue, "&",
                    AMPERSAND_SECONDPASS);

            if (tmpValue == null) {
                tmpValue = "";
            }
            if (!"".equals(tmpValue.trim())) {
                if (charSet == null) {
                    strByte = tmpValue.getBytes();
                } else {
                    strByte = tmpValue.getBytes(charSet);
                }
                strIn = new ByteArrayInputStream(strByte);

                DocumentBuilderFactory dfactory = DocumentBuilderFactory.
                        newInstance();

                EntityResolver et = null;
                try {
                    et = ServicesRegistry.getInstance().
                            getJahiaWebAppsDeployerService().getDtdEntityResolver();
                }
                catch (Exception t) {
                }
                DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
                if (et != null) {
                    docBuilder.setEntityResolver(et);
                }

                Document doc;
                try {
                    synchronized (docBuilder) {
                        doc = docBuilder.parse(strIn);
                    }
                } catch (Exception e) {
                    logger.error("Unable to parse input", e);
                    return "";
                }

                TagRemover tagRemover = new TagRemover();

                synchronized (unrecognizedTags) {
                    size = unrecognizedTags.size();
                    for (int i = 0; i < size; i++) {
                        tagRemover.addTag((String) unrecognizedTags.get(i));
                    }
                }
                //tagRemover.addTag("o:p");
                doc = tagRemover.parseDOM(doc);

                size = DOMVisitors.size();
                for (int i = 0; i < size; i++) {
                    HtmlDOMVisitor visitor = (HtmlDOMVisitor) DOMVisitors.get(i);
                    doc = visitor.parseDOM(doc);
                }

                doc.normalize();
                TransformerFactory tfactory = TransformerFactory.newInstance();

                // This creates a transformer that does a simple identity transform,
                // and thus can be used for all intents and purposes as a serializer.
                Transformer serializer = tfactory.newTransformer();
                serializer.setOutputProperty(OutputKeys.METHOD, "xhtml");
                serializer.setOutputProperty(OutputKeys.INDENT, "yes");
                if (charSet != null) {
                    serializer.setOutputProperty(OutputKeys.ENCODING, charSet);
                }
                //serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                //serializer.setOutputProperty(OutputProperties.S_KEY_INDENT_AMOUNT, "4");
                strOut.reset();
                serializer.transform(new DOMSource(doc),
                        new StreamResult(strOut));


                if (tidy.getParseErrors() > 0) {
                    result = "<TIDYERRORS>\n" + strErr.toString() +
                            "</TIDYERRORS>";
                } else {
                    if (charSet == null) {
                        result = strOut.toString();
                    } else {
                        result = strOut.toString(charSet);
                    }
                }
                result = JahiaTools.replacePattern(result, AMPERSAND_SECONDPASS,
                        "&");
                result = JahiaTools.text2XMLEntityRef(result, 1);
                result = JahiaTools.replacePattern(result, AMPERSAND, "&");

            } else if (tidy.getParseErrors() > 0) {
                String err = strErr.toString();
                result = "<TIDYERRORS>\n" + err + "</TIDYERRORS>";
                if (err.indexOf("is not recognized!") != -1) {
                    err = JahiaTools.replacePatternIgnoreCase(err.toLowerCase(),
                            " - error: ", "@@@");
                    String[] errors = org.jahia.utils.JahiaTools.getTokens(err, "@@@");
                    if (errors.length > 0) {
                        String token = "";
                        String tag = null;
                        for (int i = 0; i < errors.length; i++) {
                            token = errors[i];
                            int pos = token.indexOf(" is not recognized!");
                            if (pos != -1) {
                                try {
                                    tag = token.substring(0, pos);
                                    if (!tag.startsWith("<")) {
                                        // we found an unknown empty tag
                                        synchronized (unrecognizedTags) {
                                            if (unrecognizedTags.contains(tag)) {
                                                continue;
                                            } else {
                                                unrecognizedTags.add(tag);
                                                newInlineTags.add(tag);
                                            }
                                        }
                                    } else {
                                        tag = tag.substring(1, tag.length() - 1);
                                        synchronized (unrecognizedTags) {
                                            if (unrecognizedTags.contains(tag)) {
                                                continue;
                                            } else {
                                                unrecognizedTags.add(tag);
                                                newBlockLevelTags.add(tag);
                                            }
                                        }
                                    }
                                } catch (Exception t) {
                                }
                            }
                        }
                        result = parse(input, siteId, tidyConfig, DOMVisitors);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e, e);
            return input;
        }
        return result;
    }

}
