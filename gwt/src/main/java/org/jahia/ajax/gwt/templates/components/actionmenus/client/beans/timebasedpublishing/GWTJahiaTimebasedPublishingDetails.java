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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.actionmenus.client.beans.timebasedpublishing;

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
