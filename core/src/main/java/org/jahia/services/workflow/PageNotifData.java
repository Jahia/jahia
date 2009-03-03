/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
                String keyValue = JahiaResourceBundle.getMessageResource(message.getKey(), locale);
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
