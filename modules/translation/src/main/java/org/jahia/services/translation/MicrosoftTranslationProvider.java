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

package org.jahia.services.translation;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.notification.HttpClientService;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.w3c.dom.Document;

import javax.jcr.RepositoryException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.IOException;

public class MicrosoftTranslationProvider implements TranslationProvider {

    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(MicrosoftTranslationProvider.class);
    private static MicrosoftTranslationProvider instance;

    private String name;
    private TranslationService translationService;
    private HttpClientService httpClientService;
//    private String clientId = "jahia-translation-test";
//    private String clientSecret = "J5VZSNEf6LvaoTOF+hoB9Zoy1zDA38dZyvXyGu2kWGM=";

    private long accesExpiration = System.currentTimeMillis();
    private String accessToken;

    public static MicrosoftTranslationProvider getInstance() {
        if (instance == null) {
            instance = new MicrosoftTranslationProvider();
        }
        return instance;
    }

    public void start() {
        translationService.addProvider(this);
    }

    private boolean authenticate(JCRSiteNode site) {
        if (accesExpiration < System.currentTimeMillis()) {
            String clientId = null;
            String clientSecret = null;
            try {
                if (site.isNodeType("jmix:microsoftTranslatorSettings")) {
                    clientId = site.getPropertyAsString("j:microsoftClientId");
                    clientSecret = site.getPropertyAsString("j:microsoftClientSecret");
                }
            } catch (RepositoryException e) {
                logger.error("Failed to get Microsoft Translator credentials", e);
                return false;
            }
            PostMethod method = new PostMethod("https://datamarket.accesscontrol.windows.net/v2/OAuth2-13");
            method.addParameter("grant_type", "client_credentials");
            method.addParameter("client_id", clientId);
            method.addParameter("client_secret", clientSecret);
            method.addParameter("scope", "http://api.microsofttranslator.com");
            int returnCode;
            String bodyAsString;
            long callTime = System.currentTimeMillis();
            try {
                returnCode = httpClientService.getHttpClient().executeMethod(method);
                bodyAsString = method.getResponseBodyAsString();
            } catch (IOException e) {
                logger.error("Failed to call token service", e);
                return false;
            }
            if (returnCode != HttpStatus.SC_OK) {
                return false;
            }
            try {
                JSONObject jsonObject = new JSONObject(bodyAsString);
                accessToken = jsonObject.getString("access_token");
                accesExpiration = callTime + jsonObject.getLong("expires_in") * 1000;
            } catch (JSONException e) {
                logger.error("Failed to parse access token", e);
                return false;
            }
        }
        return true;
    }

    public String translate(String text, String srcLanguage, String destLanguage, boolean isHtml, JCRSiteNode site) {
        String translatedText = text;
        if (authenticate(site)) {
            GetMethod method = new GetMethod("http://api.microsofttranslator.com/v2/Http.svc/Translate");
            method.setRequestHeader("Authorization", "Bearer " + accessToken);
            method.setQueryString(new NameValuePair[]{
                    new NameValuePair("text", text),
                    new NameValuePair("from", srcLanguage),
                    new NameValuePair("to", destLanguage),
                    new NameValuePair("contentType", isHtml ? "text/html" : "text/plain")
            });
            try {
                int returnCode = httpClientService.getHttpClient().executeMethod(method);
                if (returnCode == HttpStatus.SC_OK) {
                    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document document = builder.parse(method.getResponseBodyAsStream());
                    translatedText = document.getElementsByTagName("string").item(0).getTextContent();
                }
            } catch (Exception e) {
                logger.error("Failed to get translation", e);
            }
        }
        return translatedText;
    }

    public boolean isEnabled(JCRSiteNode site) {
        try {
            return site.isNodeType("jmix:microsoftTranslatorSettings") && site.hasProperty("j:microsoftTranslationActivated") && site.getProperty("j:microsoftTranslationActivated").getBoolean();
        } catch (RepositoryException e) {
            logger.error("Failed to check if Microsoft Translator provider is enabled", e);
        }
        return false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTranslationService(TranslationService translationService) {
        this.translationService = translationService;
    }

    public void setHttpClientService(HttpClientService httpClientService) {
        this.httpClientService = httpClientService;
    }

}
