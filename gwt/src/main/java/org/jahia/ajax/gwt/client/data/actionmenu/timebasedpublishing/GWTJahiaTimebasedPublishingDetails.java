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
package org.jahia.ajax.gwt.client.data.actionmenu.timebasedpublishing;

import java.io.Serializable;

/**
 * Details concerning timebased publishing for a given content object.
 *
 * @author rfelden
 * @version 26 f�vr. 2008 - 16:49:06
 */
public class GWTJahiaTimebasedPublishingDetails implements Serializable {

    String title;
    String currentStatusLabel ;
    String schedulingTypeLabel ;
    String publicationDateLabel ;
    String expirationDateLabel ;
    String publicationDateValue ;
    String expirationDateValue ;
    String currentStatusValue;
    String schedulingTypeValue;
    String url ;

    public GWTJahiaTimebasedPublishingDetails() {}

    public GWTJahiaTimebasedPublishingDetails(String title ,
                                      String currentStatusLabel ,
                                      String schedulingTypeLabel ,
                                      String publicationDateLabel ,
                                      String expirationDateLabel ,
                                      String publicationDateValue ,
                                      String expirationDateValue ,
                                      String currentStatusValue,
                                      String schedulingTypeValue,
                                      String url) {
        this.title = title ;
        this.currentStatusLabel = currentStatusLabel ;
        this.schedulingTypeLabel = schedulingTypeLabel ;
        this.publicationDateLabel = publicationDateLabel ;
        this.publicationDateValue = publicationDateValue ;
        this.expirationDateLabel = expirationDateLabel ;
        this.expirationDateValue = expirationDateValue ;
        this.currentStatusValue = currentStatusValue;
        this.schedulingTypeValue = schedulingTypeValue;
        this.url = url ;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCurrentStatusLabel() {
        return currentStatusLabel;
    }

    public void setCurrentStatusLabel(String currentStatusLabel) {
        this.currentStatusLabel = currentStatusLabel;
    }

    public String getSchedulingTypeLabel() {
        return schedulingTypeLabel;
    }

    public void setSchedulingTypeLabel(String schedulingTypeLabel) {
        this.schedulingTypeLabel = schedulingTypeLabel;
    }

    public String getPublicationDateLabel() {
        return publicationDateLabel;
    }

    public void setPublicationDateLabel(String publicationDateLabel) {
        this.publicationDateLabel = publicationDateLabel;
    }

    public String getExpirationDateLabel() {
        return expirationDateLabel;
    }

    public void setExpirationDateLabel(String expirationDateLabel) {
        this.expirationDateLabel = expirationDateLabel;
    }

    public String getPublicationDateValue() {
        return publicationDateValue;
    }

    public void setPublicationDateValue(String publicationDateValue) {
        this.publicationDateValue = publicationDateValue;
    }

    public String getExpirationDateValue() {
        return expirationDateValue;
    }

    public void setExpirationDateValue(String expirationDateValue) {
        this.expirationDateValue = expirationDateValue;
    }

    public String getCurrentStatusValue() {
        return currentStatusValue;
    }

    public void setCurrentStatusValue(String currentStatusValue) {
        this.currentStatusValue = currentStatusValue;
    }

    public String getSchedulingTypeValue() {
        return schedulingTypeValue;
    }

    public void setSchedulingTypeValue(String schedulingTypeValue) {
        this.schedulingTypeValue = schedulingTypeValue;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
