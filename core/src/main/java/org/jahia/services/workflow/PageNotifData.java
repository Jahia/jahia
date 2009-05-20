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
 package org.jahia.services.workflow;

import org.jahia.services.pages.ContentPage;
import org.jahia.services.version.IsValidForActivationResults;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.engines.EngineMessage;

import java.util.*;
import java.text.MessageFormat;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 janv. 2006
 * Time: 16:07:44
 * To change this template use File | Settings | File Templates.
 */
public class PageNotifData implements Comparable {
    private String pageURL;
    private String pageComment;
    private ContentPage contentPage;
    private Set<String> languageCodes;
    private boolean workflowSuccessful;
    private boolean partialSuccessful = false;
    private List<Object> errors;
    private List<Object> warnings;
    private boolean deleted = false;
    private boolean failed = false;

    public PageNotifData (String pageURL,
                          String pageComment,
                          ContentPage contentPage,
                          Set<String> languageCodes,
                          boolean workflowSuccessful,
                          List<Object> errors,
                          List<Object> warnings) {
        this.pageURL = pageURL;
        this.pageComment = pageComment;
        this.contentPage = contentPage;
        this.languageCodes = languageCodes;
        this.workflowSuccessful = workflowSuccessful;
        if (errors != null) {
            this.errors = errors;
        } else {
            this.errors = new ArrayList<Object>();
        }
        if (warnings != null) {
            this.warnings = warnings;
        } else {
            this.warnings = new ArrayList<Object>();
        }
    }

    public String getPageURL () {
        return pageURL;
    }

    public String getPageComment () {
        return pageComment;
    }

    public ContentPage getContentPage () {
        return contentPage;
    }

    public Set<String> getLanguageCodes() {
        return languageCodes;
    }

    public Map<String, String> getTitles() {
        Map<String, String> allTitles = contentPage.getTitles(true);
        Map<String, String> workflowTitles = new HashMap<String, String>();
        for (String curLanguageCode : languageCodes) {
            String curLanguageTitle = allTitles.get(curLanguageCode);
            workflowTitles.put(curLanguageCode, curLanguageTitle);
        }
        return workflowTitles;
    }

    public String getDisplayTitles() {
        Map<String, String> titles = getTitles();
        StringBuffer result = new StringBuffer();
        Iterator<Map.Entry<String, String>> entryIter = titles.entrySet().iterator();
        while (entryIter.hasNext()) {
            Map.Entry<String, String> curEntry = entryIter.next();
            result.append(curEntry.getKey());
            result.append("=");
            result.append(curEntry.getValue());
            if (entryIter.hasNext()) {
                result.append(", ");
            }
        }
        return result.toString();
    }

    public boolean isWorkflowSuccessful() {
        return workflowSuccessful;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public List<Object> getWarnings() {
        return warnings;
    }

    public String getDisplayErrors(Locale locale, String separator) {
        return getDisplayActivationResults(getErrors(), locale, separator);
    }

    private String getDisplayActivationResults(List<Object> activationResults, Locale locale, String separator) {
        StringBuffer result = new StringBuffer();
        Iterator<Object> activationIter = activationResults.iterator();
        while (activationIter.hasNext()) {
            Object curActivationResultObj = activationIter.next();
            if (!(curActivationResultObj instanceof IsValidForActivationResults)) {
                result.append(curActivationResultObj.toString());
            } else {
                IsValidForActivationResults validationResult = (
                    IsValidForActivationResults) curActivationResultObj;
                String resultFormat = JahiaResourceBundle.getMessageResource(
                    "org.jahia.engines.workflow.activationResultMessage",
                    locale);
                EngineMessage message = validationResult.getMsg();
                String keyValue = message.isResource() ? JahiaResourceBundle.getMessageResource(message.getKey(), locale) : message.getKey();
                String s = validationResult.getComment();
                if (keyValue != null) {
                    s = keyValue;
                    if (message.getValues() != null) {
                        MessageFormat msgFormat = new MessageFormat(keyValue);
                        msgFormat.setLocale(locale);
                        s = msgFormat.format(message.getValues());
                    }
                }
                Object[] arguments = {s, validationResult.getObjectType(), validationResult.getObjectID(), validationResult.getLanguageCode()};

                result.append(MessageFormat.format(resultFormat, arguments));
            }
             if (activationIter.hasNext()) {
                 result.append(separator);
             }
        }
        return result.toString();
    }

    public boolean equals (Object obj) {
        if (this == obj) return true;

        if (obj != null && this.getClass() == obj.getClass()) {
            final PageNotifData right = (PageNotifData) obj;
            if (pageURL == null) pageURL = "";
            if (pageComment == null) pageComment = "";
            return ((pageURL.equals (right.pageURL)) && (pageComment.equals (right.pageComment)));
        }
        return false;
    }

    public int hashCode () {
        return (pageURL + pageComment).hashCode ();
    }

    public int compareTo (Object o) throws ClassCastException {
        PageNotifData right = (PageNotifData) o;
        int pageURLComp = pageURL.compareTo (right.pageURL);
        if (pageURLComp != 0) {
            return pageURLComp;
        }
        return pageComment.compareTo (right.pageComment);
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public boolean isPartialSuccessful() {
        return partialSuccessful;
    }

    public void setPartialSuccessful(boolean partialSuccessful) {
        this.partialSuccessful = partialSuccessful;
    }
}
